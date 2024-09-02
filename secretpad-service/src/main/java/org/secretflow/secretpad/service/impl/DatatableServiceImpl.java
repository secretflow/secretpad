/*
 * Copyright 2023 Ant Group Co., Ltd.
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
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.enums.DataTableTypeEnum;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.ConcurrentErrorCode;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.FeatureTableErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.PageUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.data.AbstractDataManager;
import org.secretflow.secretpad.manager.integration.datasource.AbstractDatasourceManager;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.datatablegrant.AbstractDatatableGrantManager;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.*;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.persistence.model.TeeJobStatus;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.OssService;
import org.secretflow.secretpad.service.decorator.awsoss.AwsOssConfig;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.graph.converter.KusciaTeeDataManagerConverter;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.datasync.vote.TeeNodeDatatableManagementSyncRequest;
import org.secretflow.secretpad.service.model.datatable.*;
import org.secretflow.secretpad.service.util.DbSyncUtil;
import org.secretflow.secretpad.service.util.HttpUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.common.constant.Constants.PUSH_TO_TEE_JOB_ID;
import static org.secretflow.secretpad.common.constant.DomainDatasourceConstants.DEFAULT_DATASOURCE;
import static org.secretflow.secretpad.service.constant.TeeJobConstants.MOCK_VOTE_RESULT;

/**
 * Datatable service implementation class
 *
 * @author xiaonan
 * @date 2023/6/7
 */
@Service
public class DatatableServiceImpl implements DatatableService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatatableServiceImpl.class);

    private static final String DOMAIN_DATA_GRANT_ID = "domaindatagrant_id";
    private final Cache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    @Autowired
    @Qualifier("kusciaApiFutureTaskThreadPool")
    private Executor kusciaApiFutureThreadPool;
    @Autowired
    private AbstractDatatableManager datatableManager;
    @Autowired
    private AbstractDataManager dataManager;
    @Autowired
    private NodeManager nodeManager;
    @Autowired
    private OssService ossService;
    @Autowired
    private AbstractDatasourceManager datasourceManager;
    @Autowired
    private AbstractDatatableGrantManager datatableGrantManager;
    @Autowired
    private AbstractNodeRouteManager nodeRouteManager;
    @Autowired
    private AbstractJobManager jobManager;
    @Autowired
    private KusciaTeeDataManagerConverter teeJobConverter;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectDatatableRepository projectDatatableRepository;
    @Autowired
    private TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository;
    @Autowired
    private FeatureTableRepository featureTableRepository;
    @Autowired
    private ProjectFeatureTableRepository projectFeatureTableRepository;
    @Autowired
    private NodeRepository nodeRepository;
    @Resource
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Resource
    private EnvService envService;
    @Value("${tee.domain-id:tee}")
    private String teeNodeId;
    @Value("${secretpad.platform-type}")
    private String plaformType;


    private List<String> getNodeIds(String instId, List<String> nodeNameFilter) {
        List<NodeDTO> nodeDTOS = nodeManager.listReadyNodeByNames(instId, nodeNameFilter);
        return nodeDTOS.stream().map(NodeDTO::getNodeId).toList();
    }


    @Override
    public AllDatatableListVO listDatatablesByOwnerId(ListDatatableRequest request) {
        List<String> nodeIds = new ArrayList<>();
        if (envService.isAutonomy()) {
            nodeIds.addAll(getNodeIds(InstServiceImpl.INST_ID, request.getNodeNamesFilter()));
        } else {
            nodeIds.add(request.getOwnerId());
        }

        List<DatatableNodeVO> datatableNodeVOList = new CopyOnWriteArrayList<>();
        AtomicInteger totalDatatableNum = new AtomicInteger();
        List<CompletableFuture<Void>> futures = nodeIds.stream().map(nodeId -> CompletableFuture.supplyAsync(() -> {
            ListDatatableRequest nodeRequest = createNodeRequest(request, nodeId);
            return listDatatablesByNodeId(nodeRequest);
        }, kusciaApiFutureThreadPool).handle((datatableListVO, ex) -> {
            if (ex != null) {
                LOGGER.error("Error processing nodeId {}: {}", nodeId, ex.getMessage(), ex);
                return null;
            } else {
                return datatableListVO;
            }
        }).thenAccept(datatableListVO -> {
            NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
            if (ObjectUtils.isEmpty(nodeDO)) {
                LOGGER.error("getNodeToken Cannot find node by nodeId {}.", nodeId);
                return;
            }

            totalDatatableNum.addAndGet(datatableListVO.getTotalDatatableNums());
            datatableListVO.getDatatableVOList().forEach(datatableVO -> {
                datatableNodeVOList.add(DatatableNodeVO.builder()
                        .datatableVO(datatableVO)
                        .nodeId(nodeId)
                        .nodeName(nodeDO.getName())
                        .build());
            });
        })).toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            Throwable actualException = e.getCause();
            if (actualException instanceof SecretpadException) {
                throw (SecretpadException) actualException;
            } else {
                throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
        } catch (TimeoutException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
        }

        List<DatatableNodeVO> rangeVOList = PageUtils.rangeList(datatableNodeVOList, request.getPageSize(), request.getPageNumber());

        return AllDatatableListVO.builder()
                .datatableNodeVOList(rangeVOList)
                .totalDatatableNums(totalDatatableNum.get())
                .build();
    }

    @Override
    public DatatableListVO listDatatablesByNodeId(ListDatatableRequest request) {
        LOGGER.info("List data table by nodeId = {}", request.getOwnerId());
        DatatableListDTO dataTableListDTO = datatableManager.findByNodeId(request.getOwnerId(), request.getPageSize(), request.getPageNumber(), request.getStatusFilter(), request.getDatatableNameFilter(), request.getTypes());
        LOGGER.info("Try get a map with datatableId: DatatableDTO");
        Map<Object, DatatableDTO> datatables = dataTableListDTO.getDatatableDTOList().stream().collect(Collectors.toMap(DatatableDTO::getDatatableId, Function.identity()));
        LOGGER.info("Try get auth project pairs with Map<DatatableID, List<Pair<ProjectDatatableDO, ProjectDO>>>");
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> datatableAuthPairs = getAuthProjectPairs(request.getOwnerId(), Lists.newArrayList(datatables.values().stream().filter(e -> !StringUtils.equals(e.getType(), DataTableTypeEnum.HTTP.name())).map(DatatableDTO::getDatatableId).collect(Collectors.toList())));
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> featureAuthProjectPairs = getHttpFeatureAuthProjectPairs(request.getOwnerId(), Lists.newArrayList(datatables.values().stream().filter(e -> StringUtils.equals(e.getType(), DataTableTypeEnum.HTTP.name())).map(DatatableDTO::getDatatableId).collect(Collectors.toList())));
        //merge data table and feature table
        datatableAuthPairs.putAll(featureAuthProjectPairs);
        LOGGER.info("get datatable VO list from datatableListDTO and with datatable auth pairs.");
        // teeNodeId maybe blank
        String teeDomainId = StringUtils.isBlank(request.getTeeNodeId()) ? teeNodeId : request.getTeeNodeId();
        // query push to tee map
        Map<String, List<TeeNodeDatatableManagementDO>> pushToTeeInfoMap = getPushToTeeInfos(request.getOwnerId(), teeDomainId, Lists.newArrayList(datatables.values().stream().map(DatatableDTO::getDatatableId).collect(Collectors.toList())));

        List<DatatableVO> datatableVOList = dataTableListDTO.getDatatableDTOList().stream().map(it -> {
            List<Pair<ProjectDatatableDO, ProjectDO>> pairs = datatableAuthPairs.get(it.getDatatableId());
            List<AuthProjectVO> authProjectVOList = null;
            if (pairs != null) {
                authProjectVOList = AuthProjectVO.fromPairs(pairs);
            }
            // query management data object
            List<TeeNodeDatatableManagementDO> pushToTeeInfos = pushToTeeInfoMap.get(teeJobConverter.buildTeeDatatableId(teeDomainId, it.getDatatableId()));
            TeeNodeDatatableManagementDO managementDO = CollectionUtils.isEmpty(pushToTeeInfos) ? null : pushToTeeInfos.stream().sorted(Comparator.comparing(TeeNodeDatatableManagementDO::getGmtCreate).reversed()).toList().get(0);
            DatatableDTO datatableDTO = datatables.get(it.getDatatableId());
            return DatatableVO.from(datatableDTO, authProjectVOList, managementDO);
        }).collect(Collectors.toList());
        return DatatableListVO.builder().datatableVOList(datatableVOList).totalDatatableNums(dataTableListDTO.getTotalDatatableNums()).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DatatableNodeVO getDatatable(GetDatatableRequest request) {
        LOGGER.info("Get datatable detail with nodeID = {}, datatable id = {}", request.getNodeId(), request.getDatatableId());
        if (DataTableTypeEnum.HTTP.name().equals(request.getType())) {
            Optional<FeatureTableDO> featureTableDOOptional = featureTableRepository.findById(new FeatureTableDO.UPK(request.getDatatableId(), request.getNodeId(), DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
            if (featureTableDOOptional.isEmpty()) {
                throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_NOT_EXIST);
            }
            FeatureTableDO featureTableDO = featureTableDOOptional.get();
            Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> datatableAuthPairs = getHttpFeatureAuthProjectPairs(request.getNodeId(), Lists.newArrayList(featureTableDO.getUpk().getFeatureTableId()));
            boolean success = HttpUtils.detection(featureTableDO.getUrl());
            String status = success ? Constants.STATUS_AVAILABLE : Constants.STATUS_UNAVAILABLE;
            featureTableDO.setStatus(status);
            featureTableRepository.save(featureTableDO);
            DatatableDTO datatableDTO = DatatableDTO.builder().datatableId(featureTableDO.getUpk().getFeatureTableId()).datatableName(featureTableDO.getFeatureTableName()).nodeId(featureTableDO.getNodeId()).relativeUri(featureTableDO.getUrl()).datasourceId(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID).status(status).datasourceType(DataSourceTypeEnum.HTTP.name()).type(DataTableTypeEnum.HTTP.name()).schema(featureTableDO.getColumns().stream().map(it -> new DatatableDTO.TableColumnDTO(it.getColName(), it.getColType(), it.getColComment())).collect(Collectors.toList())).build();
            DatatableVO datatableVO = DatatableVO.from(datatableDTO, datatableAuthPairs.containsKey(datatableDTO.getDatatableId()) ? AuthProjectVO.fromPairs(datatableAuthPairs.get(datatableDTO.getDatatableId())) : null, null);
            return DatatableNodeVO.builder().datatableVO(datatableVO).nodeId(request.getNodeId()).nodeName(nodeRepository.findByNodeId(request.getNodeId()).getName()).build();
        }
        Optional<DatatableDTO> datatableOpt = datatableManager.findById(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDatatableId()));
        if (datatableOpt.isEmpty()) {
            LOGGER.error("Datatable not exists when get datatable detail.");
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
        }
        if (DataSourceTypeEnum.OSS.name().equals(datatableOpt.get().getDatasourceType())) {
            Optional<DatasourceDTO> datasourceOpt;
            if (envService.isCenter() && envService.isEmbeddedNode(datatableOpt.get().getNodeId())) {
                Domaindatasource.QueryDomainDataSourceResponse response = kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder().setDomainId(datatableOpt.get().getNodeId()).setDatasourceId(datatableOpt.get().getDatasourceId()).build(), datatableOpt.get().getNodeId());
                datasourceOpt = Optional.of(DatasourceDTO.fromDomainDatasource(response.getData()));
            } else {
                datasourceOpt = datasourceManager.findById(DatasourceDTO.NodeDatasourceId.from(datatableOpt.get().getNodeId(), datatableOpt.get().getDatasourceId()));
            }
            DatasourceDTO.OssDataSourceInfoDTO ossDataSourceInfoDTO = datasourceOpt.get().getOssDataSourceInfoDTO();
            AwsOssConfig awsOssConfig = AwsOssConfig.builder().accessKeyId(ossDataSourceInfoDTO.getAccessKeyId()).secretAccessKey(ossDataSourceInfoDTO.getSecretAccessKey()).endpoint(ossDataSourceInfoDTO.getEndpoint()).build();
            if (!ossService.checkObjectExists(awsOssConfig, ossDataSourceInfoDTO.getBucket(), datatableOpt.get().getRelativeUri())) {
                datatableOpt.get().setStatus(Constants.STATUS_UNAVAILABLE);
            }
        }
        DatatableDTO dto = datatableOpt.get();

        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> datatableAuthPairs = getAuthProjectPairs(request.getNodeId(), Lists.newArrayList(dto.getDatatableId()));
        // teeNodeId maybe blank
        String teeDomainId = StringUtils.isBlank(request.getTeeNodeId()) ? teeNodeId : request.getTeeNodeId();
        // query push to tee map
        Map<String, List<TeeNodeDatatableManagementDO>> pushToTeeInfoMap = getPushToTeeInfos(request.getNodeId(), teeDomainId, Lists.newArrayList(dto.getDatatableId()));
        // query management data object
        List<TeeNodeDatatableManagementDO> pushToTeeInfos = pushToTeeInfoMap.get(teeJobConverter.buildTeeDatatableId(teeDomainId, dto.getDatatableId()));
        TeeNodeDatatableManagementDO managementDO = CollectionUtils.isEmpty(pushToTeeInfos) ? null : pushToTeeInfos.stream().sorted(Comparator.comparing(TeeNodeDatatableManagementDO::getGmtCreate).reversed()).toList().get(0);
        DatatableVO datatableVO = DatatableVO.from(dto, datatableAuthPairs.containsKey(dto.getDatatableId()) ? AuthProjectVO.fromPairs(datatableAuthPairs.get(dto.getDatatableId())) : null, managementDO);
        return DatatableNodeVO.builder()
                .datatableVO(datatableVO)
                .nodeId(request.getNodeId())
                .nodeName(nodeRepository.findByNodeId(request.getNodeId()).getName())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDatatable(DeleteDatatableRequest request) {
        LOGGER.info("Delete datatable with node id = {}, datatable id = {}", request.getNodeId(), request.getDatatableId());
        if (DataTableTypeEnum.HTTP.name().equals(request.getType())) {
            Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> featureAuthProjectPairs = getHttpFeatureAuthProjectPairs(request.getNodeId(), Collections.singletonList(request.getDatatableId()));
            if (!CollectionUtils.isEmpty(featureAuthProjectPairs)) {
                throw SecretpadException.of(DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED);
            }
            featureTableRepository.deleteById(new FeatureTableDO.UPK(request.getDatatableId(), request.getNodeId(), DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
            return;
        }
        // check if it has auth projects
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> authProjectPairs = getAuthProjectPairs(request.getNodeId(), Collections.singletonList(request.getDatatableId()));
        if (!CollectionUtils.isEmpty(authProjectPairs)) {
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED);
        }
        // teeNodeId maybe blank
        String teeDomainId = StringUtils.isBlank(request.getTeeNodeId()) ? teeNodeId : request.getTeeNodeId();
        String datatableId = teeJobConverter.buildTeeDatatableId(teeNodeId, request.getDatatableId());
        // query last push to tee job
        Optional<TeeNodeDatatableManagementDO> pushOptional = teeNodeDatatableManagementRepository.findFirstByNodeIdAndTeeNodeIdAndDatatableIdAndKind(request.getNodeId(), teeDomainId, datatableId, TeeJobKind.Push);
        // delete tee node datatable if status is success
        if (pushOptional.isPresent() && pushOptional.get().getStatus().equals(TeeJobStatus.SUCCESS)) {
            // datasourceId and relativeUri maybe blank
            String datasourceId = StringUtils.isBlank(request.getDatasourceId()) ? DEFAULT_DATASOURCE : request.getDatasourceId();
            String relativeUri = StringUtils.isBlank(request.getRelativeUri()) ? "" : request.getRelativeUri();
            Map<String, String> deleteFromTeeMap = new HashMap<>(2);
            deleteFromTeeMap.put(PUSH_TO_TEE_JOB_ID, pushOptional.get().getUpk().getJobId());
            deleteFromTeeMap.put(TeeJob.RELATIVE_URI, relativeUri);
            // save delete datatable from Tee node job
            TeeNodeDatatableManagementDO deleteFromDO = TeeNodeDatatableManagementDO.builder().upk(TeeNodeDatatableManagementDO.UPK.builder().nodeId(request.getNodeId()).datatableId(datatableId).teeNodeId(teeDomainId).jobId(UUIDUtils.random(4)).build()).datasourceId(datasourceId).status(TeeJobStatus.RUNNING).kind(TeeJobKind.Delete).operateInfo(JsonUtils.toJSONString(deleteFromTeeMap)).build();
            saveTeeNodeDatatableManagementOrPush(deleteFromDO);
            // build tee job model
            TeeJob teeJob = TeeJob.genTeeJob(deleteFromDO, List.of(request.getNodeId()), "", Collections.emptyList(), Collections.emptyList());
            // build push datatable to Tee node input config
            Job.CreateJobRequest createJobRequest = teeJobConverter.converter(teeJob);
            // create job
            jobManager.createJob(createJobRequest);
        }
        datatableManager.deleteDataTable(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDatatableId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pushDatatableToTeeNode(PushDatatableToTeeRequest request) {
        LOGGER.info("Push datatable to teeNode with node id = {}, datatable id = {}", request.getNodeId(), request.getDatatableId());
        boolean pushAuth = false;
        String domainDataGrantId = "";
        // teeNodeId and datasourceId maybe blank
        String teeDomainId = StringUtils.isBlank(request.getTeeNodeId()) ? teeNodeId : request.getTeeNodeId();
        String datasourceId = StringUtils.isBlank(request.getDatasourceId()) ? DEFAULT_DATASOURCE : request.getDatasourceId();
        String datatableId = teeJobConverter.buildTeeDatatableId(teeNodeId, request.getDatatableId());
        String relativeUri = StringUtils.isBlank(request.getRelativeUri()) ? datatableId : request.getRelativeUri();
        // check node route
        nodeRouteManager.checkRouteNotExistInDB(request.getNodeId(), teeDomainId);
        nodeRouteManager.checkRouteNotExistInDB(teeDomainId, request.getNodeId());
        // query domain data grant id from database
        Optional<TeeNodeDatatableManagementDO> pushAuthOptional = teeNodeDatatableManagementRepository.findFirstByNodeIdAndTeeNodeIdAndDatatableIdAndKind(request.getNodeId(), teeDomainId, request.getDatatableId(), TeeJobKind.PushAuth);
        if (pushAuthOptional.isPresent()) {
            Map<String, Object> operateInfoMap = TeeJob.getOperateInfoMap(pushAuthOptional.get().getOperateInfo());
            if (!CollectionUtils.isEmpty(operateInfoMap) && operateInfoMap.containsKey(DOMAIN_DATA_GRANT_ID)) {
                pushAuth = true;
                domainDataGrantId = operateInfoMap.get(DOMAIN_DATA_GRANT_ID).toString();
            }
        }
        // query push auth from tee node for pushing datatable if pushAuth tag is true
        if (pushAuth) {
            try {
                datatableGrantManager.queryDomainGrant(request.getNodeId(), domainDataGrantId);
            } catch (Exception ex) {
                LOGGER.info("Datatable grant is empty, node id = {}, datatable id = {}", request.getNodeId(), request.getDatatableId());
                pushAuth = false;
            }
        }
        // create push auth from tee node for pushing datatable if pushAuth tag is false
        if (!pushAuth) {
            String domainGrantId = datatableGrantManager.createDomainGrant(request.getNodeId(), teeDomainId, request.getDatatableId(), "");
            // save domain grant id
            Map<String, String> domainGrantIdMap = new HashMap<>(1);
            domainGrantIdMap.put(DOMAIN_DATA_GRANT_ID, domainGrantId);
            TeeNodeDatatableManagementDO pushAuthDO = TeeNodeDatatableManagementDO.builder().upk(TeeNodeDatatableManagementDO.UPK.builder().nodeId(request.getNodeId()).datatableId(request.getDatatableId()).teeNodeId(teeDomainId).jobId(UUIDUtils.random(4)).build()).datasourceId(datasourceId).status(TeeJobStatus.SUCCESS).kind(TeeJobKind.PushAuth).operateInfo(JsonUtils.toJSONString(domainGrantIdMap)).build();
//            teeNodeDatatableManagementRepository.save(pushAuthDO);
            saveTeeNodeDatatableManagementOrPush(pushAuthDO);
        }
        Map<String, String> pushToTeeMap = new HashMap<>(1);
        pushToTeeMap.put(TeeJob.RELATIVE_URI, relativeUri);
        // save push datatable to Tee node job
        TeeNodeDatatableManagementDO pushToTeeDO = TeeNodeDatatableManagementDO.builder().upk(TeeNodeDatatableManagementDO.UPK.builder().nodeId(request.getNodeId()).datatableId(datatableId).teeNodeId(teeDomainId).jobId(UUIDUtils.random(4)).build()).datasourceId(datasourceId).status(TeeJobStatus.RUNNING).kind(TeeJobKind.Push).operateInfo(JsonUtils.toJSONString(pushToTeeMap)).build();
//        teeNodeDatatableManagementRepository.save(pushToTeeDO);
        saveTeeNodeDatatableManagementOrPush(pushToTeeDO);
        // build tee job model
        TeeJob teeJob = TeeJob.genTeeJob(pushToTeeDO, List.of(request.getNodeId(), teeDomainId), "", Collections.emptyList(), Collections.emptyList());
        // build push datatable to Tee node input config
        Job.CreateJobRequest createJobRequest = teeJobConverter.converter(teeJob);
        // create job
        jobManager.createJob(createJobRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullResultFromTeeNode(String nodeId, String datatableId, String targetTeeNodeId, String datasourceId, String relativeUri, String voteResult, String projectId, String projectJobId, String projectJobTaskId, String resultType) {
        LOGGER.info("Pull result from teeNode with node id = {}, datatable id = {}", nodeId, datatableId);
        // teeNodeId and datasourceId maybe blank
        String teeDomainId = StringUtils.isBlank(targetTeeNodeId) ? teeNodeId : targetTeeNodeId;
        datasourceId = StringUtils.isBlank(datasourceId) ? DEFAULT_DATASOURCE : datasourceId;
        datatableId = teeJobConverter.buildTeeDatatableId(nodeId, datatableId);
        relativeUri = StringUtils.isBlank(relativeUri) ? datatableId : relativeUri;
        Map<String, String> pullFromTeeMap = new HashMap<>(5);
        pullFromTeeMap.put(TeeJob.RELATIVE_URI, relativeUri);
        // mock vote result
        // String voteResult = VOTE_RESULT;
        pullFromTeeMap.put(TeeJob.VOTE_RESULT, StringUtils.isNotBlank(voteResult) ? voteResult : MOCK_VOTE_RESULT);
        pullFromTeeMap.put(TeeJob.PROJECT_ID, projectId);
        pullFromTeeMap.put(TeeJob.PROJECT_JOB_ID, projectJobId);
        pullFromTeeMap.put(TeeJob.PROJECT_JOB_TASK_ID, projectJobTaskId);
        pullFromTeeMap.put(TeeJob.RESULT_TYPE, resultType);
        // save pull result from Tee node job
        TeeNodeDatatableManagementDO pullFromTeeDO = TeeNodeDatatableManagementDO.builder().upk(TeeNodeDatatableManagementDO.UPK.builder().nodeId(nodeId).datatableId(datatableId).teeNodeId(teeDomainId).jobId(UUIDUtils.random(4)).build()).datasourceId(datasourceId).status(TeeJobStatus.RUNNING).kind(TeeJobKind.Pull).operateInfo(JsonUtils.toJSONString(pullFromTeeMap)).build();
//        teeNodeDatatableManagementRepository.save(pullFromTeeDO);
        saveTeeNodeDatatableManagementOrPush(pullFromTeeDO);
        // build tee job model
        TeeJob teeJob = TeeJob.genTeeJob(pullFromTeeDO, List.of(nodeId, teeDomainId), "", Collections.emptyList(), Collections.emptyList());
        // build push datatable to Tee node input config
        Job.CreateJobRequest createJobRequest = teeJobConverter.converter(teeJob);
        // create job
        jobManager.createJob(createJobRequest);
    }

    @Override
    public OssDatatableVO createDataTable(CreateDatatableRequest createDatatableRequest) {
        verifyRate();
        String domainDataId = genDomainDataId();
        Map<String, String> failedCreatedNodes = new ConcurrentHashMap<>();

        if (envService.isAutonomy()) {
            createDatatableRequest.getNodeIds().forEach(nodeId -> {
                datasourceManager.findById(
                        DatasourceDTO.NodeDatasourceId.builder()
                                .nodeId(nodeId)
                                .datasourceId(createDatatableRequest.getDatasourceId())
                                .build()
                );
            });
            List<CompletableFuture<Void>> futures = createDatatableRequest.getNodeIds().stream()
                    .map(nodeId -> CompletableFuture.supplyAsync(() -> dataManager.createDatatable(
                                    genDomainDataId(),
                                    nodeId,
                                    createDatatableRequest.getDatatableName(),
                                    createDatatableRequest.getRelativeUri(),
                                    createDatatableRequest.getDatasourceId(),
                                    createDatatableRequest.getDesc(),
                                    createDatatableRequest.getDatasourceType(),
                                    createDatatableRequest.getDatasourceName(),
                                    createDatatableRequest.getNullStrs(),
                                    createDatatableRequest.getColumns().stream().map(column -> {
                                        DatatableSchema schema = new DatatableSchema();
                                        schema.setFeatureName(column.getColName());
                                        schema.setFeatureType(column.getColType());
                                        schema.setFeatureDescription(StringUtils.isNotEmpty(column.getColComment()) ? column.getColComment() : "");
                                        return schema;
                                    }).toList(),
                                    ObjectUtils.isEmpty(createDatatableRequest.getPartition()) ? null : createDatatableRequest.getPartition().toPartition()
                            ), kusciaApiFutureThreadPool)
                            .handle((result, ex) -> {
                                if (ex != null) {
                                    LOGGER.error("Failed to create data for nodeId {}", nodeId, ex);
                                    failedCreatedNodes.put(nodeId, ex.getMessage());
                                } else {
                                    LOGGER.info("record domainDataId for nodeId {}: result={}", nodeId, result);
                                }
                                return null;
                            }).thenAccept(result -> {
                            })).toList();

            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
            } catch (TimeoutException e) {
                throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
            } catch (ExecutionException e) {
                throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
            }

            return OssDatatableVO.builder()
                    .domainDataId(domainDataId)
                    .failedCreatedNodes(failedCreatedNodes)
                    .build();
        }
        datasourceManager.findById(
                DatasourceDTO.NodeDatasourceId.builder()
                        .nodeId(createDatatableRequest.getOwnerId())
                        .datasourceId(createDatatableRequest.getDatasourceId())
                        .build()
        );
        String result = dataManager.createDatatable(
                domainDataId,
                createDatatableRequest.getOwnerId(),
                createDatatableRequest.getDatatableName(),
                createDatatableRequest.getRelativeUri(),
                createDatatableRequest.getDatasourceId(),
                createDatatableRequest.getDesc(),
                createDatatableRequest.getDatasourceType(),
                createDatatableRequest.getDatasourceName(),
                createDatatableRequest.getNullStrs(),
                createDatatableRequest.getColumns().stream().map(column -> {
                    DatatableSchema schema = new DatatableSchema();
                    schema.setFeatureName(column.getColName());
                    schema.setFeatureType(column.getColType());
                    schema.setFeatureDescription(column.getColComment());
                    return schema;
                }).collect(Collectors.toList()),
                ObjectUtils.isEmpty(createDatatableRequest.getPartition()) ? null : createDatatableRequest.getPartition().toPartition()
        );

        if (!domainDataId.equals(result)) {
            LOGGER.warn("Inconsistent domainDataId for ownerId {}: Expected {}, Got {}", createDatatableRequest.getOwnerId(), domainDataId, result);
            failedCreatedNodes.put(createDatatableRequest.getOwnerId(), "Inconsistent domainDataId");
        }

        return OssDatatableVO.builder()
                .domainDataId(domainDataId)
                .failedCreatedNodes(failedCreatedNodes)
                .build();
    }


    @Override
    public List<DatatableVO> findDatatableByNodeId(String nodeId) {
        List<DatatableDTO> datatableDTOS = datatableManager.findAllDatatableByNodeId(nodeId);
        if (CollectionUtils.isEmpty(datatableDTOS)) {
            return Collections.EMPTY_LIST;
        }
        return datatableDTOS.stream().map(e -> DatatableVO.builder().datatableId(e.getDatatableId()).datatableName(e.getDatatableName()).type(e.getType()).datasourceId(e.getDatasourceId()).nodeId(e.getNodeId()).build()).collect(Collectors.toList());
    }

    /**
     * Query auth project pairs by nodeId and datatableIds then collect to Map
     *
     * @param nodeId       target nodeId
     * @param datatableIds target datatableId list
     * @return Map of datatableId and auth project pair list
     */
    private Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getAuthProjectPairs(String nodeId, List<String> datatableIds) {
        List<ProjectDatatableDO> authProjectDatatables = projectDatatableRepository.authProjectDatatablesByDatatableIds(nodeId, datatableIds);
        return getStringListMap(authProjectDatatables);
    }

    private Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getHttpFeatureAuthProjectPairs(String nodeId, List<String> featureTableIds) {
        List<ProjectFeatureTableDO> featureTableDOS = projectFeatureTableRepository.findByNodeIdAndFeatureTableIds(nodeId, featureTableIds);
        List<ProjectDatatableDO> authProjectDatatables = featureTableDOS.stream().map(e -> ProjectDatatableDO.builder().tableConfig(e.getTableConfig()).source(e.getSource()).upk(new ProjectDatatableDO.UPK(e.getUpk().getProjectId(), e.getUpk().getNodeId(), e.getUpk().getFeatureTableId())).build()).collect(Collectors.toList());
        return getStringListMap(authProjectDatatables);
    }

    private Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getStringListMap(List<ProjectDatatableDO> authProjectDatatables) {
        List<String> projectIds = authProjectDatatables.stream().map(it -> it.getUpk().getProjectId()).collect(Collectors.toList());
        Map<String, ProjectDO> projectMap = projectRepository.findAllById(projectIds).stream().collect(Collectors.toMap(ProjectDO::getProjectId, Function.identity()));
        return authProjectDatatables.stream().map(
                        // List<Pair>
                        it -> new Pair<>(it, projectMap.getOrDefault(it.getUpk().getProjectId(), null))).filter(it -> it.getValue1() != null)
                // Map<datatable, List<Pair>>
                .collect(Collectors.groupingBy(it -> it.getValue0().getUpk().getDatatableId()));
    }

    /**
     * Query tee node datatable management data object list by nodeId, teeNodeId and datatableIds then collect to Map
     *
     * @param nodeId       target nodeId
     * @param teeNodeId    target teeNodeId
     * @param datatableIds target datatableId list
     * @return Map of datatableId and tee node datatable management data object list
     */
    private Map<String, List<TeeNodeDatatableManagementDO>> getPushToTeeInfos(String nodeId, String teeNodeId, List<String> datatableIds) {
        List<String> teeDatatables = datatableIds.stream().map(datatableId -> teeJobConverter.buildTeeDatatableId(teeNodeId, datatableId)).toList();
        // batch query push to tee job list by datatableIds
        List<TeeNodeDatatableManagementDO> managementList = teeNodeDatatableManagementRepository.findAllByNodeIdAndTeeNodeIdAndDatatableIdsAndKind(nodeId, teeNodeId, teeDatatables, TeeJobKind.Push);
        if (CollectionUtils.isEmpty(managementList)) {
            return Collections.emptyMap();
        }
        // collect by datatable id
        return managementList.stream().collect(Collectors.groupingBy(it -> it.getUpk().getDatatableId()));
    }

    private void saveTeeNodeDatatableManagementOrPush(TeeNodeDatatableManagementDO saveDO) {
        if (PlatformTypeEnum.EDGE.equals(PlatformTypeEnum.valueOf(plaformType))) {
            TeeNodeDatatableManagementSyncRequest request = TeeNodeDatatableManagementSyncRequest.parse2VO(saveDO);
            DbSyncRequest dbSyncRequest = DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.TEE_NODE_DATATABLE_MANAGEMENT.name()).projectNodesInfo(request).build();
            DbSyncUtil.dbDataSyncToCenter(dbSyncRequest);
        } else {
            teeNodeDatatableManagementRepository.save(saveDO);
        }
    }

    private void verifyRate() {
        try {
            RateLimiter rateLimiter = getRateLimiter(UserContext.getUserName());
            if (!rateLimiter.tryAcquire()) {
                throw SecretpadException.of(SystemErrorCode.REQUEST_FREQUENCY_ERROR);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private RateLimiter getRateLimiter(String userName) throws ExecutionException {
        return rateLimiters.get(userName, () -> RateLimiter.create(5.0 / 60));
    }

    private String genDomainDataId() {
        return UUIDUtils.random(8);
    }

    /**
     * query all then page
     */
    private ListDatatableRequest createNodeRequest(ListDatatableRequest request, String nodeId) {
        return ListDatatableRequest.builder().statusFilter(request.getStatusFilter()).datatableNameFilter(request.getDatatableNameFilter()).types(request.getTypes()).ownerId(nodeId).teeNodeId(request.getTeeNodeId()).build();
    }


}
