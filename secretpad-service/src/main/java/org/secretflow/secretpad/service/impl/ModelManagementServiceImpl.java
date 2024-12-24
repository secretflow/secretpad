/*
 * Copyright 2024 Ant Group Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.constant.ServingConstants;
import org.secretflow.secretpad.common.enums.ModelStatsEnum;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.data.DataManager;
import org.secretflow.secretpad.manager.integration.model.PartyDTO;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.PartyDataSource;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.ModelManagementService;
import org.secretflow.secretpad.service.constant.JobConstants;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.model.*;
import org.secretflow.secretpad.service.model.serving.ResourceVO;
import org.secretflow.secretpad.service.model.serving.ServingDetailVO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.IndividualTable;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import secretflow.serving.ClusterConfigOuterClass;
import secretflow.serving.FeatureConfig;
import secretflow.serving.ModelConfigOuterClass;
import secretflow.serving.ServerConfigOuterClass;
import secretflow.serving.kuscia.ServingConfig;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.common.constant.ServingConstants.DEFAULT_MEMORY_UNIT;
import static org.secretflow.secretpad.service.util.JobUtils.parseMemorySize;

/**
 * @author chenmingliang
 * @date 2024/01/18
 */
@Service
@Slf4j
public class ModelManagementServiceImpl implements ModelManagementService {

    @Resource
    private ProjectModelPackRepository projectModelPackRepository;

    @Resource
    private DataManager dataManager;

    @Resource
    private ProjectJobTaskRepository taskRepository;

    @Resource
    private NodeRepository nodeRepository;

    @Resource
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Resource
    private ProjectModelServiceRepository projectModelServiceRepository;

    @Resource
    private FeatureTableRepository featureTableRepository;
    @Resource
    private EnvService envService;

    public static String transferKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return "";
        }
        if (keyword.contains("%") || keyword.contains("_")) {
            keyword = keyword.replaceAll("%", "!%").replaceAll("_", "!_");
        }
        return keyword;
    }

    @Override
    public ModelPackListVO modelPackPage(QueryModelPageRequest queryModelPageRequest) {
        Pageable page = queryModelPageRequest.of();
        Specification<ProjectModelPackDO> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> andCondition = new ArrayList<>();
            List<Predicate> orCondition = new ArrayList<>();
            andCondition.add(criteriaBuilder.equal(root.get("projectId"), queryModelPageRequest.getProjectId()));
            if (StringUtils.isNotBlank(queryModelPageRequest.getModelStats())) {
                Path<Object> modelStats = root.get("modelStats");
                Predicate pModelStats = criteriaBuilder.equal(modelStats, ModelStatsEnum.valueOf(queryModelPageRequest.getModelStats()).getCode());
                andCondition.add(pModelStats);
            }
            if (StringUtils.isNotBlank(queryModelPageRequest.getSearchKey())) {
                String keyword = transferKeyword(queryModelPageRequest.getSearchKey());
                String pattern = "%" + keyword + "%";
                Predicate pModelName, pModelId;
                if (StringUtils.equals(keyword, queryModelPageRequest.getSearchKey())) {
                    pModelName = criteriaBuilder.like(root.get("modelName").as(String.class), pattern);
                    pModelId = criteriaBuilder.like(root.get("modelId").as(String.class), pattern);
                } else {
                    pModelName = criteriaBuilder.like(root.get("modelName").as(String.class), pattern, '!');
                    pModelId = criteriaBuilder.like(root.get("modelId").as(String.class), pattern, '!');
                }
                orCondition.add(pModelName);
                orCondition.add(pModelId);
            }
            CriteriaQuery<?> where;
            if (!CollectionUtils.isEmpty(orCondition)) {
                Predicate or = criteriaBuilder.or(orCondition.toArray(new Predicate[orCondition.size()]));
                Predicate and = criteriaBuilder.and(andCondition.toArray(new Predicate[andCondition.size()]));
                where = criteriaQuery.where(or, and);
            } else {
                Predicate and = criteriaBuilder.and(andCondition.toArray(new Predicate[andCondition.size()]));
                where = criteriaQuery.where(and);
            }
            return where.getRestriction();

        };
        Page<ProjectModelPackDO>
                modelPackPage = projectModelPackRepository.findAll(specification, page);
        return ModelPackListVO.instance(PageUtils.convert(modelPackPage, this::convert), page.getPageNumber() + 1, page.getPageSize(), modelPackPage.getTotalElements());
    }

    private String getTargetNode(Set<String> nodeIdSet) {
        if (envService.isAutonomy()) {
            return nodeIdSet.stream().filter(party -> envService.isNodeInCurrentInst(party)).findFirst().get();
        }
        return null;
    }

    @Override
    public ModelPackDetailVO modelPackDetail(String modelId, String projectId) {
        Optional<ProjectModelPackDO> projectModelPackDOOptional = projectModelPackRepository.findById(modelId);
        if (projectModelPackDOOptional.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
        }
        ProjectModelPackDO projectModelPackDO = projectModelPackDOOptional.get();
        String sampleTables = projectModelPackDO.getSampleTables();
        Map<String, String> partyTableMap = JsonUtils.toJavaMap(sampleTables, String.class);
        List<NodeDO> nodeDOList = nodeRepository.findByNodeIdIn(partyTableMap.keySet());
        Map<String, String> nodeMap = nodeDOList.stream().collect(Collectors.toMap(NodeDO::getNodeId, NodeDO::getName));

        String targetNodeId = getTargetNode(partyTableMap.keySet());
        //model train.by this version single component
        List<ModelPackDetailVO.Parties> partiesListVO = new ArrayList<>();
        partyTableMap.forEach((key, value) -> {
            Domaindata.DomainData modelDomainData = dataManager.queryDomainData(key, projectModelPackDO.getModelReportId(), targetNodeId);
            //attributes contains both party of schema.
            Map<String, String> attributes = modelDomainData.getAttributesMap();
            //get schema from attributes
            List<String> columns = explainColumnFromDomainDataDistData(attributes);
            //get its onw raw sample table, prepare to filter its own schema
            Domaindata.DomainData rawTabbleDomainData = dataManager.queryDomainData(key, value, targetNodeId);
            //do filter
            List<String> ownSchema = filterOwnSchema(columns, rawTabbleDomainData.getColumnsList());
            ModelPackDetailVO.Parties partiesVO = new ModelPackDetailVO.Parties();
            partiesVO.setNodeId(key);
            partiesVO.setNodeName(nodeMap.get(key));
            partiesVO.setColumns(ownSchema);
            partiesListVO.add(partiesVO);
        });
        return ModelPackDetailVO.builder().parties(partiesListVO).build();
    }

    @Override
    public ModelPackInfoVO modelPackInfo(String modelId, String projectId) {
        Optional<ProjectModelPackDO> projectModelPackDOOptional = projectModelPackRepository.findById(modelId);
        if (projectModelPackDOOptional.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
        }
        ProjectModelPackDO projectModelPackDO = projectModelPackDOOptional.get();
        List<String> modelList = projectModelPackDO.getModelList();
        String graphDetail = projectModelPackDO.getGraphDetail();
        GraphDetailVO graphDetailVO = null;
        try {
            graphDetailVO = JsonUtils.toJavaObject(graphDetail, GraphDetailVO.class);
        } catch (Exception e) {
            log.error("JsonUtils.toJavaObject({}, GraphDetailVO.class) error", graphDetail, e);
        }
        String sampleTables = projectModelPackDO.getSampleTables();
        Map<String, String> partyTableMap = JsonUtils.toJavaMap(sampleTables, String.class);
        List<NodeDO> nodeDOList = nodeRepository.findByNodeIdIn(partyTableMap.keySet());
        Map<String, String> nodeMap = nodeDOList.stream().collect(Collectors.toMap(NodeDO::getNodeId, NodeDO::getName));
        List<ServingDetailVO.ServingDetail> servingDetails = new ArrayList<>();
        List<PartyDataSource> partyDataSources = projectModelPackDO.getPartyDataSources();
        Map<String, String> partyDatasourceMap = partyDataSources.stream().collect(Collectors.toMap(PartyDataSource::getPartyId, PartyDataSource::getDatasource));
        partyTableMap.forEach((k, v) -> {
            ServingDetailVO.ServingDetail servingDetail = ServingDetailVO.ServingDetail.builder()
                    .nodeId(k)
                    .nodeName(nodeMap.get(k))
                    .sourcePath(partyDatasourceMap.get(k))
                    .build();
            servingDetails.add(servingDetail);
        });
        return ModelPackInfoVO.builder().modelGraphDetail(modelList).graphDetailVO(graphDetailVO).modelStats(ModelStatsEnum.parse(projectModelPackDO.getModelStats())).servingDetails(servingDetails).build();
    }

    private List<String> filterOwnSchema(List<String> columns, List<Common.DataColumn> columnsList) {
        if (CollectionUtils.isEmpty(columns)) {
            return columns;
        }
        return columns.stream().filter(col -> columnsList.stream().map(Common.DataColumn::getName).toList().contains(col)).collect(Collectors.toList());
    }

    @Override
    public ModelPartiesVO modelParties(String projectId, String outputId, String graphNodeId) {
        Optional<ProjectTaskDO> taskDOOptional = taskRepository.findLatestTasks(projectId, graphNodeId);
        if (taskDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
        }
        List<String> nodeIds = taskDOOptional.get().getParties();
        List<NodeDO> nodeDOList = nodeRepository.findByNodeIdIn(nodeIds);
        return ModelPartiesVO.builder()
                .parties(nodeDOList.stream().map(e -> ModelPartiesVO.Party.builder()
                        .nodeId(e.getNodeId())
                        .nodeName(e.getName())
                        .build()).collect(Collectors.toList())).build();
    }

    @Override
    @Transactional
    public void createModelServing(String projectId, String modelId, List<CreateModelServingRequest.PartyConfig> partyConfigs) {
        Optional<ProjectModelPackDO> projectModelPackDOOptional = projectModelPackRepository.findById(modelId);
        if (projectModelPackDOOptional.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
        }
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(IndividualTable.getDescriptor()).build();
        ProjectModelPackDO projectModelPackDO = projectModelPackDOOptional.get();
        List<Serving.ServingParty> servingParties = new ArrayList<>();
        List<String> servingPartiesStr = new ArrayList<>();
        AtomicReference<ServingConfig.KusciaServingConfig> kusciaServingConfig = new AtomicReference<>(ServingConfig.KusciaServingConfig.newBuilder()
                .build());
        if (!CollectionUtils.isEmpty(partyConfigs)) {
            String targetNode = getTargetNode(partyConfigs.stream().map(CreateModelServingRequest.PartyConfig::getNodeId).collect(Collectors.toSet()));

            partyConfigs.forEach(party -> {
                Map<String, String> featureMapping = new HashMap<>();
                ServingConfig.KusciaServingConfig.PartyConfig kusciaServingConfigPartyConfig = ServingConfig.KusciaServingConfig.PartyConfig.newBuilder()
                        .build();
                String domainId = party.getNodeId();
                List<CreateModelServingRequest.PartyConfig.Feature> features = party.getFeatures();
                features.forEach(e -> featureMapping.put(e.getOnlineName(), e.getOfflineName()));
                //serving config
                ServerConfigOuterClass.ServerConfig serverConfig = ServerConfigOuterClass.ServerConfig.newBuilder()
                        .putAllFeatureMapping(featureMapping)
                        .build();

                //model config
                Domaindata.DomainData domainData = dataManager.queryDomainData(domainId, modelId, targetNode);
                ModelConfigOuterClass.ModelConfig servingModelConfig = ModelConfigOuterClass.ModelConfig.newBuilder()
                        .setModelId(modelId)
                        .setBasePath("/")
                        .setSourcePath(domainData.getDomaindataId())
                        .setSourceType(ModelConfigOuterClass.SourceType.ST_DP)
                        .build();
                //featureSourceConfig
                String endpoint = "mock";
                if (!StringUtils.equalsIgnoreCase(party.getFeatureTableId(), "mock")) {
                    Optional<FeatureTableDO> featureTableDOOptional = featureTableRepository.findById(new FeatureTableDO.UPK(party.getFeatureTableId(), party.getNodeId(), DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
                    Assert.isTrue(featureTableDOOptional.isPresent(), party.getFeatureTableId() + "not exist");
                    endpoint = featureTableDOOptional.get().getUrl();
                }

                FeatureConfig.FeatureSourceConfig servingFeatureSourceConfig = FeatureConfig.FeatureSourceConfig.newBuilder()
                        .build();
                if (party.getIsMock()) {
                    servingFeatureSourceConfig = servingFeatureSourceConfig.toBuilder().setMockOpts(
                            FeatureConfig.MockOptions.newBuilder().build()).build();
                } else {
                    servingFeatureSourceConfig = servingFeatureSourceConfig.toBuilder()
                            .setHttpOpts(FeatureConfig.HttpOptions.newBuilder().setEndpoint(endpoint).build())
                            .build();
                }
                //channel_desc
                ClusterConfigOuterClass.ChannelDesc servingChannelDesc = ClusterConfigOuterClass.ChannelDesc.newBuilder()
                        .setProtocol("http")
                        .build();
                kusciaServingConfigPartyConfig = kusciaServingConfigPartyConfig.toBuilder()
                        .setServerConfig(serverConfig)
                        .setModelConfig(servingModelConfig)
                        .setFeatureSourceConfig(servingFeatureSourceConfig)
                        .setChannelDesc(servingChannelDesc)
                        .build();
                kusciaServingConfig.set(kusciaServingConfig.get().toBuilder()
                        .putPartyConfigs(domainId, kusciaServingConfigPartyConfig)
                        .build());
                Serving.ServingParty.Builder servingPartyBuilder = Serving.ServingParty.newBuilder()
                        .setDomainId(domainId)
                        .setAppImage(JobConstants.SECRETFLOW_SERVING_IMAGE);
                for (ResourceVO resource : party.getResources()) {
                    validateResourceConfig(resource, party.getNodeId());

                    Serving.Resource.Builder resourceBuilder = Serving.Resource.newBuilder()
                            .setMinCpu(resource.getMinCPU())
                            .setMaxCpu(resource.getMaxCPU())
                            .setMinMemory(resource.getMinMemory())
                            .setMaxMemory(resource.getMaxMemory());
                    servingPartyBuilder.addResources(resourceBuilder);
                }
                Serving.ServingParty servingParty = servingPartyBuilder.build();
                servingParties.add(servingParty);
                servingPartiesStr.add(ProtoUtils.toJsonString(servingParty, typeRegistry));

            });

            String servingInputConfig = ProtoUtils.toJsonString(kusciaServingConfig.get(), typeRegistry);
            String servingId = UUIDUtils.random(8);
            if (StringUtils.isNotEmpty(servingId) && Objects.equals(projectModelPackDO.getModelStats(), ModelStatsEnum.OFFLINE.getCode())) {
                servingId = projectModelPackDO.getServingId();
            }

            // Filter out non-positive minimum resource values
            List<Serving.ServingParty> filteredServingParties = servingParties.stream()
                    .map(originalParty -> {
                        Serving.ServingParty.Builder filteredServingPartyBuilder = originalParty.toBuilder();
                        filteredServingPartyBuilder.clearResources();
                        for (Serving.Resource resource : originalParty.getResourcesList()) {
                            double minCPU = stringToDouble(resource.getMinCpu(), originalParty.getDomainId());
                            double maxCPU = stringToDouble(resource.getMaxCpu(), originalParty.getDomainId());
                            double minMemory = parseMemorySize(resource.getMinMemory(), originalParty.getDomainId());
                            double maxMemory = parseMemorySize(resource.getMaxMemory(), originalParty.getDomainId());
                            if (maxCPU == 0 && minCPU == maxCPU && maxMemory == 0 && minMemory == maxMemory) {
                                log.warn("Invalid resource configuration for node: {}, skipping it", originalParty.getDomainId());
                                continue;
                            }
                            minCPU = buildMinCpuValue(stringToDouble(resource.getMinCpu(), originalParty.getDomainId()));
                            minMemory = buildMinMemoryValue(parseMemorySize(resource.getMinMemory(), originalParty.getDomainId()));
                            maxCPU = buildMaxCpuValue(stringToDouble(resource.getMaxCpu(), originalParty.getDomainId()), minCPU);
                            maxMemory = buildMaxMemoryValue(parseMemorySize(resource.getMaxMemory(), originalParty.getDomainId()), minMemory);

                            Serving.Resource.Builder resourceBuilder = Serving.Resource.newBuilder()
                                    .setMinCpu(String.valueOf(minCPU))
                                    .setMaxCpu(String.valueOf(maxCPU))
                                    .setMinMemory(String.valueOf(minMemory).concat(DEFAULT_MEMORY_UNIT))
                                    .setMaxMemory(String.valueOf(maxMemory).concat(DEFAULT_MEMORY_UNIT));
                            log.info("Filtered  node:{}, resources: {}  ", originalParty.getDomainId(), resourceBuilder);
                            filteredServingPartyBuilder.addResources(resourceBuilder);
                        }
                        return filteredServingPartyBuilder.build();
                    }).collect(Collectors.toList());


            String initiator = servingParties.stream().findAny().get().getDomainId();
            Serving.CreateServingResponse servingResponse;
            if (envService.isAutonomy()) {
                initiator = servingParties.stream().filter(party -> envService.isNodeInCurrentInst(party.getDomainId())).findFirst().get().getDomainId();
                Serving.CreateServingRequest createServingRequest = Serving.CreateServingRequest.newBuilder()
                        .setInitiator(initiator)
                        .setServingId(servingId)
                        .setServingInputConfig(servingInputConfig)
                        .addAllParties(filteredServingParties)
                        .build();

                servingResponse = kusciaGrpcClientAdapter.createServing(createServingRequest, initiator);
            } else {
                Serving.CreateServingRequest createServingRequest = Serving.CreateServingRequest.newBuilder()
                        .setInitiator(initiator)
                        .setServingId(servingId)
                        .setServingInputConfig(servingInputConfig)
                        .addAllParties(filteredServingParties)
                        .build();

                servingResponse = kusciaGrpcClientAdapter.createServing(createServingRequest);
            }
            log.info("createServing status = {}", servingResponse.getStatus());

            ProjectModelServingDO projectModelServingDO = ProjectModelServingDO.builder()
                    .servingId(servingId)
                    .projectId(projectId)
                    .servingStats("init")
                    .parties(JsonUtils.toJSONString(servingPartiesStr))
                    .servingInputConfig(servingInputConfig)
                    .initiator(initiator)
                    .build();
            projectModelServiceRepository.save(projectModelServingDO);

            projectModelPackDO.setServingId(servingId);
            projectModelPackDO.setModelStats(ModelStatsEnum.PUBLISHING.getCode());
            projectModelPackRepository.save(projectModelPackDO);
        }

    }

    @Override
    public ServingDetailVO queryModelServingDetail(String servingId) {
        Optional<ProjectModelServingDO> projectModelServingDOOptional = projectModelServiceRepository.findById(servingId);
        if (projectModelServingDOOptional.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_SERVING_NOT_FOUND);
        }
        ProjectModelServingDO projectModelServingDO = projectModelServingDOOptional.get();

        String partiesJsonString = projectModelServingDO.getParties();
        Gson gson = new Gson();
        List<String> partyStringList = gson.fromJson(partiesJsonString, new TypeToken<List<String>>() {
        }.getType());
        JsonArray partiesArray = new JsonArray();
        partyStringList.forEach(partyJsonStr -> partiesArray.add(JsonParser.parseString(partyJsonStr)));
        List<PartyDTO> partyDTOList = JsonUtils.toJavaList(partiesArray.toString(), PartyDTO.class);

        List<ProjectModelServingDO.PartyEndpoints> partyEndpoints = projectModelServingDO.getPartyEndpoints();
        AtomicReference<String> endpoints = new AtomicReference<>("");
        partyEndpoints.forEach(e -> {
            if (StringUtils.isNotEmpty(e.getEndpoints())) {
                endpoints.set(e.getEndpoints().replaceAll("(-service\\.).*(\\.svc)", "$1#$2"));
            }
        });
        Map<String, String> endpointsMap = partyEndpoints.stream().collect(Collectors.toMap(ProjectModelServingDO.PartyEndpoints::getNodeId, ProjectModelServingDO.PartyEndpoints::getEndpoints));
        String servingInputConfig = projectModelServingDO.getServingInputConfig();
        ServingConfig.KusciaServingConfig kusciaServingConfig = (ServingConfig.KusciaServingConfig) ProtoUtils.fromJsonString(servingInputConfig, ServingConfig.KusciaServingConfig.newBuilder());
        Map<String, ServingConfig.KusciaServingConfig.PartyConfig> servingPartyConfigs = kusciaServingConfig.getPartyConfigsMap();
        List<ServingDetailVO.ServingDetail> servingDetails = new ArrayList<>();
        String modelId = "";
        for (Map.Entry<String, ServingConfig.KusciaServingConfig.PartyConfig> partyConfigEntry : servingPartyConfigs.entrySet()) {
            FeatureConfig.FeatureSourceConfig featureSourceConfig = partyConfigEntry.getValue().getFeatureSourceConfig();
            PartyDTO partyDTO = partyDTOList.stream()
                    .filter(p -> partyConfigEntry.getKey().equals(p.getDomainId()))
                    .findFirst()
                    .orElse(null);
            List<ResourceVO> resourcesList = new ArrayList<>();
            if (partyDTO != null && partyDTO.getResources() != null) {
                for (PartyDTO.Resource resource : partyDTO.getResources()) {
                    ResourceVO resourceVO = ResourceVO.builder()
                            .minCPU(resource.getMinCPU())
                            .maxCPU(resource.getMaxCPU())
                            .minMemory(resource.getMinMemory())
                            .maxMemory(resource.getMaxMemory())
                            .build();
                    resourcesList.add(resourceVO);
                }
            }
            ServingDetailVO.ServingDetail servingDetail = ServingDetailVO.ServingDetail.builder()
                    .featureMappings(partyConfigEntry.getValue().getServerConfig().getFeatureMappingMap())
                    .nodeId(partyConfigEntry.getKey())
                    .nodeName(nodeRepository.findByNodeId(partyConfigEntry.getKey()).getName())
                    .endpoints(StringUtils.isEmpty(endpointsMap.get(partyConfigEntry.getKey())) ?
                            endpoints.get().replaceAll("#", partyConfigEntry.getKey()) : endpointsMap.get(partyConfigEntry.getKey()))
                    .sourcePath(partyConfigEntry.getValue().getModelConfig().getSourcePath())
                    .resources(resourcesList)
                    .build();
            modelId = partyConfigEntry.getValue().getModelConfig().getModelId();
            FeatureConfig.HttpOptions httpOpts = featureSourceConfig.getHttpOpts();
            servingDetail.setIsMock(StringUtils.isEmpty(httpOpts.getEndpoint()));
            servingDetail.setFeatureHttp(httpOpts.getEndpoint());
            servingDetails.add(servingDetail);
        }
        return ServingDetailVO.builder().servingDetails(servingDetails).modelId(modelId).servingId(servingId).build();
    }

    @Override
    @Transactional
    public void deleteModelServing(String servingId) {
        Optional<ProjectModelServingDO> projectModelServingDOOptional = projectModelServiceRepository.findById(servingId);
        if (projectModelServingDOOptional.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_SERVING_NOT_FOUND);
        }
        Optional<ProjectModelPackDO> optionalProjectModelPackDO = projectModelPackRepository.findByServingId(servingId);
        if (optionalProjectModelPackDO.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
        }
        ProjectModelPackDO projectModelPackDO = optionalProjectModelPackDO.get();

        kusciaGrpcClientAdapter.deleteServing(Serving.DeleteServingRequest.newBuilder().setServingId(servingId).build());
        projectModelPackDO.setModelStats(ModelStatsEnum.OFFLINE.getCode());
        projectModelPackRepository.save(projectModelPackDO);
    }

    @Override
    @Transactional
    public void discardModelPack(String modelId) {
        Optional<ProjectModelPackDO> optionalProjectModelPackDO = projectModelPackRepository.findById(modelId);
        if (optionalProjectModelPackDO.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
        }
        ProjectModelPackDO projectModelPackDO = optionalProjectModelPackDO.get();
        if (!ModelStatsEnum.OFFLINE.getCode().equals(projectModelPackDO.getModelStats()) && !ModelStatsEnum.INIT.getCode().equals(projectModelPackDO.getModelStats())) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_SERVING_NOT_OFFLINE);
        }

        projectModelPackDO.setModelStats(ModelStatsEnum.DISCARDED.getCode());
        projectModelPackRepository.save(projectModelPackDO);
    }

    @Override
    @Transactional
    public void deleteModelPack(String nodeId, String modelId) {
        Optional<ProjectModelPackDO> optionalProjectModelPackDO = projectModelPackRepository.findById(modelId);
        if (optionalProjectModelPackDO.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_MODEL_NOT_FOUND);
        }
        ProjectModelPackDO projectModelPackDO = optionalProjectModelPackDO.get();
        if (!ModelStatsEnum.DISCARDED.getCode().equals(projectModelPackDO.getModelStats())) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_SERVING_NOT_OFFLINE);
        }
        projectModelPackDO.setIsDeleted(true);
        projectModelPackRepository.save(projectModelPackDO);
    }

    private ModelPackVO convert(ProjectModelPackDO projectModelPackDO) {
        String servingId = projectModelPackDO.getServingId();
        Integer stats = projectModelPackDO.getModelStats();
        String modelStats = ModelStatsEnum.parse(stats);
        if (StringUtils.isNotBlank(servingId) && ModelStatsEnum.PUBLISHING.name().equals(modelStats)) {
            Serving.QueryServingResponse queryServingResponse = null;
            try {
                String execNodeId = getInitiator(projectModelPackDO);
                log.info("query serving execNodeId: {}", execNodeId);
                queryServingResponse = kusciaGrpcClientAdapter.queryServing(Serving.QueryServingRequest.newBuilder().setServingId(servingId).build(), execNodeId);
            } catch (Exception e) {
                log.error("queryServing error!", e);
            }
            if (ObjectUtils.isNotEmpty(queryServingResponse)) {
                Serving.ServingStatusDetail status = queryServingResponse.getData().getStatus();
                String state = status.getState();
                Optional<ProjectModelServingDO> projectModelServingDOOptional = projectModelServiceRepository.findById(servingId);
                ProjectModelServingDO projectModelServingDO = projectModelServingDOOptional.get();
                if (Constants.STATUS_AVAILABLE.equals(state)) {
                    modelStats = ModelStatsEnum.PUBLISHED.name();
                    stats = ModelStatsEnum.PUBLISHED.getCode();
                    List<Serving.PartyServingStatus> partyStatusesList = queryServingResponse.getData().getStatus().getPartyStatusesList();
                    List<ProjectModelServingDO.PartyEndpoints> partyEndpointsList = partyStatusesList.stream().filter(c -> {
                        if (envService.isAutonomy()) {
                            return StringUtils.equals(projectModelServingDO.getInitiator(), c.getDomainId());
                        }
                        return true;
                    }).map(e -> {
                        String domainId = e.getDomainId();
                        String endpoint = e.getEndpointsList().stream().filter(f -> StringUtils.equals(f.getPortName(), "service")).findFirst().get().getEndpoint();
                        ProjectModelServingDO.PartyEndpoints partyEndpoints = new ProjectModelServingDO.PartyEndpoints();
                        partyEndpoints.setEndpoints(endpoint);
                        partyEndpoints.setNodeId(domainId);
                        return partyEndpoints;

                    }).collect(Collectors.toList());
                    projectModelServingDO.setPartyEndpoints(partyEndpointsList);
                    projectModelServiceRepository.save(projectModelServingDO);
                    projectModelServingDO.setServingStats("success");
                    log.info("save endpoints success!");
                } else if ("Failed".equals(state)) {
                    modelStats = ModelStatsEnum.PUBLISH_FAIL.name();
                    stats = ModelStatsEnum.PUBLISH_FAIL.getCode();
                    projectModelServingDO.setServingStats("failed");
                }
                projectModelPackDO.setModelStats(stats);
                projectModelPackRepository.save(projectModelPackDO);
                log.info("project model serving stats changed to {}", modelStats);
            }
        }
        String ownerId = projectModelPackDO.getInitiator();
        if (envService.isAutonomy() && envService.isNodeInCurrentInst(projectModelPackDO.getInitiator())) {
            ownerId = InstServiceImpl.INST_ID;
        }
        return ModelPackVO.builder()
                .modelId(projectModelPackDO.getModelId())
                .servingId(projectModelPackDO.getServingId())
                .modelDesc(projectModelPackDO.getModelDesc())
                .modelName(projectModelPackDO.getModelName())
                .modelStats(modelStats)
                .ownerId(ownerId)
                .gmtCreate(DateTimes.toRfc3339(projectModelPackDO.getGmtCreate()))
                .build();
    }

    private List<String> parse(String value) {
        log.info("parse root {}", value);
        JsonObject jsonObject = JsonParser.parseString(value).getAsJsonObject();
        String desc = jsonObject.getAsJsonObject("meta").get("desc").getAsString();
        if (StringUtils.isNotEmpty(desc)) {
            return List.of(desc.split(","));
        }
        return new ArrayList<>();
    }

    private List<String> explainColumnFromDomainDataDistData(Map<String, String> kusciaAttributes) {
        String value = kusciaAttributes.get("dist_data");
        return parse(value);
    }

    private void validateResourceConfig(ResourceVO resource, String nodeId) {
        double minCPU = stringToDouble(resource.getMinCPU(), nodeId);
        double maxCPU = stringToDouble(resource.getMaxCPU(), nodeId);
        if (minCPU > maxCPU || maxCPU < 0) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "CPU value setting error for nodeId: " + nodeId);
        }

        double minMemory = parseMemorySize(resource.getMinMemory(), nodeId);
        double maxMemory = parseMemorySize(resource.getMaxMemory(), nodeId);
        if (minMemory > maxMemory || maxMemory < 0) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "Memory value setting error for nodeId: " + nodeId);
        }
    }

    private double stringToDouble(String str, String nodeId) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, e, "Invalid resource value for nodeId: " + nodeId);
        }
    }

    private double buildMinCpuValue(double minCpu) {
        return Math.max(minCpu, ServingConstants.DEFAULT_CPU);
    }

    private double buildMinMemoryValue(double minMemory) {
        return Math.max(minMemory, ServingConstants.DEFAULT_MEMORY);
    }

    private double buildMaxCpuValue(double maxCpu, double minCpu) {
        maxCpu = Math.max(maxCpu, ServingConstants.DEFAULT_CPU);
        return Math.max(maxCpu, minCpu);
    }

    private double buildMaxMemoryValue(double maxMemory, double minMemory) {
        maxMemory = Math.max(maxMemory, ServingConstants.DEFAULT_MEMORY);
        return Math.max(maxMemory, minMemory);
    }

    /**
     * compensate for old data
     **/
    private String getInitiator(ProjectModelPackDO projectModelPackDO) {
        String initiator = projectModelPackDO.getInitiator();
        log.info("initiator {} getInitiator projectModelPackDO:{}", initiator, projectModelPackDO.getPartyDataSources());
        if (StringUtils.equals(initiator, envService.getPlatformNodeId())) {
            List<PartyDataSource> partyDataSources = projectModelPackDO.getPartyDataSources();
            Set<String> parties = partyDataSources.stream().map(PartyDataSource::getPartyId).collect(Collectors.toSet());
            if (envService.isAutonomy()) {
                return parties.stream().filter(party -> envService.isNodeInCurrentInst(party)).findFirst().get();
            }
            return envService.getPlatformNodeId();
        }
        return initiator;
    }
}
