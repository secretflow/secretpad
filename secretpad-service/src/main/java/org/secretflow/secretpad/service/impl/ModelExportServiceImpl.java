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

import org.secretflow.secretpad.common.constant.CacheConstants;
import org.secretflow.secretpad.common.constant.ComponentConstants;
import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.ModelExportErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.ModelExportDTO;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.PartyDataSource;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.ModelExportService;
import org.secretflow.secretpad.service.constant.JobConstants;
import org.secretflow.secretpad.service.graph.ComponentTools;
import org.secretflow.secretpad.service.graph.converter.KusciaJobConverter;
import org.secretflow.secretpad.service.model.graph.GetGraphRequest;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.model.export.*;
import org.secretflow.secretpad.service.model.project.ProjectGraphDomainDataSourceVO;
import org.secretflow.secretpad.service.util.GraphUtils;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.IndividualTable;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.proto.pipeline.Pipeline;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yutu
 * @date 2024/01/29
 */
@Slf4j
@Service
@Setter
public class ModelExportServiceImpl implements ModelExportService {

    @Resource
    private ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;
    @Resource
    private KusciaJobConverter kusciaJobConverter;
    @Autowired
    @Qualifier("jobManager")
    private AbstractJobManager jobManager;
    @Resource
    private CacheManager cacheManager;
    @Resource
    private NodeRepository nodeRepository;
    @Resource
    private ProjectJobTaskRepository taskRepository;
    @Resource
    private EnvService envService;
    @Resource
    private ProjectModelPackRepository projectModelPackRepository;
    @Resource
    private ProjectGraphNodeRepository projectGraphNodeRepository;
    @Resource
    private ProjectDatatableRepository projectDatatableRepository;
    @Resource
    private GraphService graphService;
    @Resource
    private ProjectGraphRepository graphRepository;
    @Resource
    private ProjectDatatableRepository datatableRepository;
    @Resource
    private ProjectGraphDomainDatasourceServiceImpl projectGraphDomainDatasourceService;
    @Resource
    private ProjectJobRepository projectJobRepository;

    @Override
    public ModelExportPackageResponse exportModel(ModelExportPackageRequest request) throws InvalidProtocolBufferException {
        log.debug("export model {}", request);
        UserContext.getUser().getOwnerId();
        Set<String> partyIds = new HashSet<>();
        request.getModelPartyConfig().forEach(m -> partyIds.add(m.getModelParty()));
        String modelDataName = UUIDUtils.random(2) + "_" + Instant.now().toEpochMilli();
        request.getModelPartyConfig().forEach(m -> m.setModelDataName(modelDataName));
        if (envService.isAutonomy() && StringUtils.isEmpty(getInitiator(partyIds))) {
            throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "initiator must be one of party");
        }
        List<String> cList = new LinkedList<>();
        request.getModelComponent().forEach(r -> cList.add(r.getGraphNodeId()));
        Optional<ProjectTaskDO> taskOptional = taskRepository.findLatestTasks(request.getProjectId(), request.getTrainId());
        if (taskOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
        }
        String trainId = taskOptional.get().getUpk().getJobId().concat("-").concat(request.getGraphNodeOutPutId());
        GetGraphRequest getGraphRequest = new GetGraphRequest();
        getGraphRequest.setGraphId(request.getGraphId());
        getGraphRequest.setProjectId(request.getProjectId());
        GraphDetailVO graphDetail = graphService.getGraphDetail(getGraphRequest);
        ModelExportDTO modelExportDTO = ModelExportDTO.builder()
                .projectId(request.getProjectId())
                .graphId(request.getGraphId())
                .modelName(request.getModelName())
                .modelDesc(request.getModelDesc())
                .graphDetail(JsonUtils.toJSONString(graphDetail))
                .partyDataSources(request.getModelPartyConfig().stream().map(e -> PartyDataSource.builder().partyId(e.getModelParty()).datasource(e.getModelDataSource() + "/" + modelDataName).build()).collect(Collectors.toList()))
                .modelList(cList)
                .trainId(trainId)
                .initiator(getInitiator(partyIds))
                .build();
        String job = createJob(request, modelExportDTO);
        Objects.requireNonNull(cacheManager.getCache(CacheConstants.MODEL_EXPORT_CACHE)).putIfAbsent(job, JsonUtils.toString(modelExportDTO));
        return ModelExportPackageResponse.builder().jobId(job).build();
    }

    @Override
    public ModelExportDTO queryModel(ModelExportStatusRequest request) {
        String jobId = request.getJobId();
        Cache cache = Objects.requireNonNull(cacheManager.getCache(CacheConstants.MODEL_EXPORT_CACHE));
        if (ObjectUtils.isEmpty(cache.get(jobId))) {
            throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "job not found.");
        }
        Object o = Objects.requireNonNull(cache.get(jobId)).get();
        if (ObjectUtils.isEmpty(o)) {
            throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "job not found");
        }
        ModelExportDTO modelExportDTO = JsonUtils.toJavaObject(String.valueOf(o), ModelExportDTO.class);
        switch (modelExportDTO.getStatus()) {
            case SUCCEED:
                projectModelPackRepository.save(ModelExportDTO.of(modelExportDTO));
                cache.evictIfPresent(jobId);
                break;
            case FAILED:
                cache.evictIfPresent(jobId);
                break;
            default:
                break;
        }
        return modelExportDTO;
    }

    @Override
    public List<ModelPartyPathResponse> modelPartyPath(ModelPartyPathRequest request) {
        Optional<ProjectTaskDO> taskOptional = taskRepository.findLatestTasks(request.getProjectId(), request.getGraphNodeId());
        if (taskOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
        }
        List<String> parties = taskOptional.get().getParties();
        if (CollectionUtils.isEmpty(parties)) {
            throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "modelPartyPath result not found");
        }
        Optional<ProjectJobDO> byJobId = projectJobRepository.findByJobId(taskOptional.get().getUpk().getJobId());
        if (byJobId.isEmpty()) {
            throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "job not found");
        }
        String graphId = byJobId.get().getGraphId();
        List<ModelPartyPathResponse> responses = new LinkedList<>();
        parties.forEach(p -> {
            NodeDO node = nodeRepository.findByNodeId(p);
            if (ObjectUtils.isEmpty(node)) {
                throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "node not found");
            }
            ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = projectGraphDomainDatasourceService.getById(request.getProjectId(), graphId, p);
            String dataSourceId = KusciaDataSourceConstants.DEFAULT_DATA_SOURCE;
            String dataSourceName = KusciaDataSourceConstants.DEFAULT_DATA_SOURCE;
            if (!ObjectUtils.isEmpty(projectGraphDomainDatasourceDO)) {
                dataSourceId = projectGraphDomainDatasourceDO.getDataSourceId();
                dataSourceName = projectGraphDomainDatasourceDO.getDataSourceName();
            }
            responses.add(ModelPartyPathResponse.builder()
                    .nodeId(p)
                    .nodeName(node.getName())
                    // serving not only support local data source
                    // However, when serving, you need to know the data source information of the project participants, and the node can only see its own in the current P2P mode, so a data synchronization may be required here
                    .dataSources(Set.of(ProjectGraphDomainDataSourceVO.DataSource.builder()
                            .dataSourceName(dataSourceName)
                            .nodeId(p)
                            .type(DataSourceTypeEnum.LOCAL.name())
                            .dataSourceId(dataSourceId)
                            .build()
                    ))
                    .build()
            );
        });
        return responses;
    }

    private String createJob(ModelExportPackageRequest request, ModelExportDTO modelExportDTO) throws InvalidProtocolBufferException {
        Job.CreateJobRequest createJobRequest = buildKusciaParams(request, modelExportDTO);
        jobManager.createJob(createJobRequest);
        return createJobRequest.getJobId();
    }


    private Job.CreateJobRequest buildKusciaParams(ModelExportPackageRequest request, ModelExportDTO modelExportDTO) throws InvalidProtocolBufferException {
        String taskId = UUIDUtils.random(4).concat("-").concat("model-export");
        String jobId = UUIDUtils.random(4);
        List<Job.Party> taskParties = request.getModelPartyConfig().stream().map(config -> Job.Party.newBuilder().setDomainId(config.getModelParty()).build()).toList();
        List<String> parties = request.getModelPartyConfig().stream().map(ModelPartyConfig::getModelParty).toList();
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(IndividualTable.getDescriptor()).build();
        String id = jobId.concat("-").concat(taskId);
        List<String> outputs = List.of(id, id.concat("-report"));
        List<String> outputsUrls = List.of(request.getModelPartyConfig().get(0).getModelDataName(), id.concat("-report"));
        TaskConfig.TaskInputConfig taskInputConfig = buildTaskInputConfig(request, parties, outputs, outputsUrls);
        List<Descriptors.FieldDescriptor> fields = taskInputConfig.getDescriptorForType().getFields();
        Set<Descriptors.FieldDescriptor> fieldSet = new TreeSet<>();
        for (Descriptors.FieldDescriptor field : fields) {
            if ("sf_input_ids".equals(field.getName())) {
                fieldSet.add(field);
            }
        }
        String json = JsonFormat.printer()
                .usingTypeRegistry(typeRegistry)
                .preservingProtoFieldNames()
                .includingDefaultValueFields(fieldSet)
                .print(taskInputConfig);

        Job.Task task = Job.Task.newBuilder()
                .setTaskId(taskId)
                .setAlias(taskId)
                .setAppImage(JobConstants.APP_IMAGE)
                .addAllParties(taskParties)
                .setTaskInputConfig(json)
                .build();
        List<ProjectGraphNodeDO> readTableByProjectIdAndGraphId = projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(request.getProjectId(), request.getGraphId());
        Optional<ProjectGraphDO> graphDOOptional = graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()));
        if (graphDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        ProjectGraphDO graphDO = graphDOOptional.get();
        Set<String> topNodes = GraphUtils.findTopNodes(graphDO.getEdges(), request.getTrainId());
        Set<String> topParties = findTopParties(topNodes, graphDO.getNodes(), request.getProjectId());
        log.info("model {}  topParties = {}", request.getTrainId(), topParties);
        if (readTableByProjectIdAndGraphId == null || readTableByProjectIdAndGraphId.isEmpty()) {
            throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "read table not found");
        }
        List<String> readTables = new LinkedList<>();
        readTableByProjectIdAndGraphId.forEach(table -> {
            Pipeline.NodeDef pipelineNodeDef = ComponentTools.getNodeDef(table.getNodeDef());
            if (topNodes.contains(table.getUpk().getGraphNodeId())) {
                readTables.add(pipelineNodeDef.getAttrs(0).getFieldsOrThrow("s").getStringValue());
            }
        });
        Map<String, String> simpleTable = new HashMap<>();
        List<ProjectDatatableDO> projectDatatableDOList = projectDatatableRepository.findByProjectId(request.getProjectId());
        projectDatatableDOList.forEach(p -> {
            if (readTables.contains(p.getUpk().getDatatableId()) && topParties.contains(p.getUpk().getNodeId())) {
                simpleTable.put(p.getUpk().getNodeId(), p.getUpk().getDatatableId());
            }
        });
        String sampleTables = JsonUtils.toJSONString(simpleTable);
        log.info("model {}  sample tables = {}", request.getTrainId(), sampleTables);
        modelExportDTO.setSampleTables(sampleTables);
        modelExportDTO.setJobId(jobId);
        modelExportDTO.setTaskId(taskId);
        modelExportDTO.setModelId(jobId.concat("-").concat(taskId));
        modelExportDTO.setModelReportId(getModelExportReport(modelExportDTO));
        return Job.CreateJobRequest.newBuilder()
                .setJobId(jobId)
                .setInitiator(envService.isCenter() ? parties.get(0) : getInitiator(parties))
                .setMaxParallelism(kusciaJobConverter.getMaxParallelism())
                .addAllTasks(List.of(task))
                .build();
    }

    private Set<String> findTopParties(Set<String> topNodes, List<ProjectGraphNodeDO> nodes, @NotBlank String projectId) {
        Map<String, ProjectGraphNodeDO> nodeDOMap = nodes.stream().collect(Collectors.toMap(e -> (e.getUpk()).getGraphNodeId(), Function.identity()));
        Set<String> partySet = new HashSet<>();
        for (String topNode : topNodes) {
            ProjectGraphNodeDO projectGraphNodeDO = nodeDOMap.get(topNode);
            GraphNodeInfo graphNodeInfo = GraphNodeInfo.fromDO(projectGraphNodeDO);
            String datatableId = ComponentTools.getDataTableId(graphNodeInfo);
            if (StringUtils.isNotBlank(datatableId)) {
                List<ProjectDatatableDO> datatableDOS = datatableRepository.findByDatableId(projectId, datatableId);
                if (!CollectionUtils.isEmpty(datatableDOS)) {
                    partySet.addAll(datatableDOS.stream().map(datatableDO -> datatableDO.getUpk().getNodeId()).toList());
                }
            }
        }
        return partySet;
    }

    /**
     * check parites in local inst
     **/
    private String getInitiator(Collection<String> parties) {
        log.info("kuscia job parties {}", parties);
        if (envService.isAutonomy()) {
            return parties.stream().filter(party -> envService.isNodeInCurrentInst(party)).findFirst().get();
        }
        return envService.getPlatformNodeId();
    }

    private String getModelExportReport(ModelExportDTO modelExportDTO) {
        return modelExportDTO.getJobId().concat("-").concat(modelExportDTO.getTaskId()).concat("-report");
    }


    private TaskConfig.TaskInputConfig buildTaskInputConfig(ModelExportPackageRequest request, List<String> parties, List<String> outputs, List<String> outputsUrls) {
        return TaskConfig.TaskInputConfig.newBuilder()
                .putAllSfDatasourceConfig(kusciaJobConverter.modelExportDatasourceConfig(parties, request.getProjectId(), request.getGraphId(), request))
                .addAllSfOutputIds(outputs)
                .addAllSfOutputUris(outputsUrls)
                .setSfClusterDesc(kusciaJobConverter.buildSfClusterDesc(parties))
                .setSfNodeEvalParam(buildNodeEvalParam(request))
                .build();
    }

    public Pipeline.NodeDef buildNodeEvalParam(ModelExportPackageRequest request) {
        List<ModelComponent> modelComponent = request.getModelComponent();
        List<String> inputs = new LinkedList<>();
        List<String> outputs = new LinkedList<>();
        List<String> params = new LinkedList<>();
        modelComponent.forEach(component -> {
            log.debug("model component {}", modelComponent);
            Optional<ProjectGraphNodeKusciaParamsDO> graphNodeKusciaParamsDO = projectGraphNodeKusciaParamsRepository.findByUpk(request.getProjectId(), request.getGraphId(), component.getGraphNodeId());
            if (graphNodeKusciaParamsDO.isEmpty()) {
                throw SecretpadException.of(ModelExportErrorCode.MODEL_EXPORT_FAILED, "component needs to have been successfully run: " + component.getGraphNodeId());
            }
            String nodeEvalParam = graphNodeKusciaParamsDO.get().getNodeEvalParam();
            String name = JsonUtils.parseObject(nodeEvalParam).path("name").asText();
            if (!StringUtils.equals(ComponentConstants.IO_READ_DATA, name) && !StringUtils.equals(ComponentConstants.IO_IDENTITY, name) && !StringUtils.equals(ComponentConstants.IO_WRITE_DATA, name)) {
                inputs.addAll(JsonUtils.toJavaList(graphNodeKusciaParamsDO.get().getInputs(), String.class));
                outputs.addAll(JsonUtils.toJavaList(graphNodeKusciaParamsDO.get().getOutputs(), String.class));
                params.add(Base64Utils.encode(nodeEvalParam.getBytes()));
            }

        });
        Map<String, Object> nodeEvalParam = Map.of(
                "domain", "model",
                "name", "model_export",
                "version", "1.0.0",
                "attr_paths", List.of("model_name", "model_desc", "input_datasets", "output_datasets", "component_eval_params", "he_mode"),
                "attrs", List.of(
                        Map.of("s", request.getModelName()),
                        Map.of("s", StringUtils.isEmpty(request.getModelDesc()) ? "" : request.getModelDesc()),
                        Map.of("ss", inputs),
                        Map.of("ss", outputs),
                        Map.of("ss", params),
                        Map.of("b", false)
                )
        );
        Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
        ProtoUtils.fromObject(nodeEvalParam, nodeDefBuilder);
        return nodeDefBuilder.build();
    }
}