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

package org.secretflow.secretpad.service.handler.datatable;

import org.secretflow.secretpad.common.dto.KusciaResponse;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.ConcurrentErrorCode;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;
import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.persistence.model.TeeJobStatus;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectDatatableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.TeeNodeDatatableManagementRepository;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.graph.converter.KusciaTeeDataManagerConverter;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.datasync.vote.TeeNodeDatatableManagementSyncRequest;
import org.secretflow.secretpad.service.model.datatable.*;
import org.secretflow.secretpad.service.util.DbSyncUtil;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.common.constant.Constants.PUSH_TO_TEE_JOB_ID;
import static org.secretflow.secretpad.common.constant.DomainDatasourceConstants.DEFAULT_DATASOURCE;

/**
 * @author lufeng
 * @date 2024/8/19
 */
@Slf4j
public abstract class AbstractDatatableHandler implements DatatableHandler {

    @Value("${tee.domain-id:tee}")
    private String teeNodeId;
    private final ProjectRepository projectRepository;
    private final ProjectDatatableRepository projectDatatableRepository;
    private final KusciaTeeDataManagerConverter teeJobConverter;
    private final TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository;

    protected final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    private final AbstractJobManager jobManager;
    private final AbstractDatatableManager datatableManager;

    private final NodeRepository nodeRepository;

    @Value("${secretpad.platform-type}")
    private String plaformType;

    protected AbstractDatatableHandler(ProjectRepository projectRepository, ProjectDatatableRepository projectDatatableRepository,
                                       KusciaTeeDataManagerConverter teeJobConverter, TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository,
                                       KusciaGrpcClientAdapter kusciaGrpcClientAdapter, AbstractJobManager jobManager, AbstractDatatableManager datatableManager, NodeRepository nodeRepository) {
        this.projectRepository = projectRepository;
        this.projectDatatableRepository = projectDatatableRepository;
        this.teeJobConverter = teeJobConverter;
        this.teeNodeDatatableManagementRepository = teeNodeDatatableManagementRepository;
        this.kusciaGrpcClientAdapter = kusciaGrpcClientAdapter;
        this.jobManager = jobManager;
        this.datatableManager = datatableManager;
        this.nodeRepository = nodeRepository;
    }


    @Override
    public DatatableNodeVO queryDatatable(GetDatatableRequest request) {
        Optional<DatatableDTO> datatableOpt = datatableManager.findById(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDatatableId()));
        if (datatableOpt.isEmpty()) {
            log.error("Datatable not exists when get datatable detail.");
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
        }
        DatatableDTO dto = datatableOpt.get();
        queryDatatable(dto);
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
    public void deleteDatatable(DeleteDatatableRequest request) {
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
        deleteDatatable(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDatatableId()));
    }

    protected void queryDatatable(DatatableDTO datatableOpt) {

    }

    private void deleteDatatable(DatatableDTO.NodeDatatableId nodeDatatableId) {
        Domaindata.DeleteDomainDataRequest.Builder builder = Domaindata.DeleteDomainDataRequest.newBuilder()
                .setDomainId(nodeDatatableId.getNodeId())
                .setDomaindataId(nodeDatatableId.getDatatableId());
        Domaindata.DeleteDomainDataResponse response;
        if (PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(plaformType))) {
            response = kusciaGrpcClientAdapter.deleteDomainData(builder.build(), nodeDatatableId.getNodeId());
        } else {
            response = kusciaGrpcClientAdapter.deleteDomainData(builder.build());
        }

        if (response.getStatus().getCode() != 0) {
            log.error("delete datatable failed: code={}, message={}, nodeId={}, datatableId={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), nodeDatatableId.getNodeId(), nodeDatatableId.getDatatableId());
            throw SecretpadException.of(DatatableErrorCode.DELETE_DATATABLE_FAILED);
        }
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


    /**
     * Get the key-value pairs of the authorized project data table and project list
     *
     * @param nodeId       nodeID
     * @param datatableIds dataTableIDList
     * @return Key-value pairs of authorized project data table and project list
     */
    public Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getAuthProjectPairs(String nodeId, List<String> datatableIds) {
        List<ProjectDatatableDO> authProjectDatatables = projectDatatableRepository.authProjectDatatablesByDatatableIds(nodeId, datatableIds);
        return getStringListMap(authProjectDatatables);
    }

    /**
     * Combine the project data table and project information into a Map
     *
     * @param authProjectDatatables List of project data tables that have been authorized for verification
     * @return Returns a Map, the key is the data table ID, and the value is the Pair list containing the data table and project information.
     */
    public Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getStringListMap(List<ProjectDatatableDO> authProjectDatatables) {
        List<String> projectIds = authProjectDatatables.stream().map(it -> it.getUpk().getProjectId()).collect(Collectors.toList());
        Map<String, ProjectDO> projectMap = projectRepository.findAllById(projectIds).stream().collect(Collectors.toMap(ProjectDO::getProjectId, Function.identity()));
        return authProjectDatatables.stream().map(
                        // List<Pair>
                        it -> new Pair<>(it, projectMap.getOrDefault(it.getUpk().getProjectId(), null))).filter(it -> it.getValue1() != null)
                // Map<datatable, List<Pair>>
                .collect(Collectors.groupingBy(it -> it.getValue0().getUpk().getDatatableId()));
    }

    public void logFailedDatatable(Map<String, String> failedDatatable, int totalSize) {
        if (!failedDatatable.isEmpty()) {
            log.error("Some nodes failed to create datatable: {}", JsonUtils.toJSONString(failedDatatable));
            if (failedDatatable.size() == totalSize) {
                log.error("All nodes failed to create datatable: {}", JsonUtils.toJSONString(failedDatatable));
                throw SecretpadException.of(DatatableErrorCode.DATATABLE_CREATE_FAILED, "All nodes create datatable failed");
            }
        }
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

    public String genDomainDataId() {
        return UUIDUtils.random(8);
    }


    public void fetchResult(Map<String, String> failedDatatable, List<CompletableFuture<KusciaResponse<Domaindata.CreateDomainDataResponse>>> completableFutures, List<CreateDatatableVO.DataTableNodeInfo> dataTableNodeInfos) {
        try {
            CompletableFuture
                    .allOf(completableFutures.toArray(new CompletableFuture[0]))
                    .get(5000, TimeUnit.MILLISECONDS);
            for (CompletableFuture<KusciaResponse<Domaindata.CreateDomainDataResponse>> task : completableFutures) {

                KusciaResponse<Domaindata.CreateDomainDataResponse> taskNow = task.get();
                if (taskNow.getData() == null && !failedDatatable.containsKey(taskNow.getNodeId())) {
                    failedDatatable.put(task.get().getNodeId(), "task failed or timeout");
                }
                //success to the async operation,but kuscia status is not success
                if (taskNow.getData().getStatus().getCode() != 0) {
                    failedDatatable.put(taskNow.getNodeId(), taskNow.getData().getStatus().getMessage());
                } else {
                    dataTableNodeInfos.add(CreateDatatableVO.DataTableNodeInfo.builder().nodeId(taskNow.getNodeId()).domainDataId(taskNow.getData().getData().getDomaindataId()).build());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
        } catch (ExecutionException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
        } catch (TimeoutException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
        }
    }

}
