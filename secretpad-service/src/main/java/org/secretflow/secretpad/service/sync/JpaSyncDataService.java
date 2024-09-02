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

package org.secretflow.secretpad.service.sync;

import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.DataSyncConsumerContext;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.DbChangeAction;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.enums.VoteExecuteEnum;
import org.secretflow.secretpad.service.sync.center.SseSession;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yutu
 * @date 2023/10/25
 */
@Slf4j
@Service
@EnableAsync
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class JpaSyncDataService {
    @SuppressWarnings(value = {"rawtypes"})
    private static Map<String, BaseRepository> doAndRepository;
    private final EdgeDataSyncLogRepository edgeDataSyncLogRepository;
    private final InstRepository instRepository;
    private final NodeRepository nodeRepository;
    private final NodeRouteRepository nodeRouteRepository;
    private final ProjectDatatableRepository projectDatatableRepository;
    private final ProjectFedTableRepository projectFedTableRepository;
    private final ProjectGraphNodeRepository projectGraphNodeRepository;
    private final ProjectGraphRepository projectGraphRepository;
    private final ProjectInstRepository projectInstRepository;
    private final ProjectJobRepository projectJobRepository;
    private final ProjectJobTaskLogRepository projectJobTaskLogRepository;
    private final ProjectJobTaskRepository projectJobTaskRepository;
    private final ProjectModelRepository projectModelRepository;
    private final ProjectNodeRepository projectNodeRepository;
    private final ProjectReportRepository projectReportRepository;
    private final ProjectRepository projectRepository;
    private final ProjectResultRepository projectResultRepository;
    private final ProjectRuleRepository projectRuleRepository;
    private final UserAccountsRepository userAccountsRepository;
    private final UserTokensRepository userTokensRepository;
    private final TeeDownLoadAuditConfigRepository teeDownLoadAuditConfigRepository;
    private final TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository;
    private final VoteInviteRepository voteInviteRepository;
    private final VoteRequestRepository voteRequestRepository;
    private final NodeRouteAuditConfigRepository nodeRouteAuditConfigRepository;
    private final ProjectApprovalConfigRepository projectApprovalConfigRepository;
    private final ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;
    private final ProjectModelServiceRepository projectModelServiceRepository;
    private final ProjectModelPackRepository projectModelPackRepository;
    private final ProjectFeatureTableRepository projectFeatureTableRepository;
    private final FeatureTableRepository featureTableRepository;
    private final ProjectGraphDomainDatasourceRepository projectGraphDomainDatasourceRepository;
    @PersistenceContext
    private final EntityManager entityManager;

    @PostConstruct
    public void init() {
        doAndRepository = new ConcurrentHashMap<>();
        doAndRepository.put(InstDO.class.getTypeName(), instRepository);
        doAndRepository.put(NodeDO.class.getTypeName(), nodeRepository);
        doAndRepository.put(NodeRouteDO.class.getTypeName(), nodeRouteRepository);
        doAndRepository.put(ProjectDatatableDO.class.getTypeName(), projectDatatableRepository);
        doAndRepository.put(ProjectFedTableDO.class.getTypeName(), projectFedTableRepository);
        doAndRepository.put(ProjectGraphNodeDO.class.getTypeName(), projectGraphNodeRepository);
        doAndRepository.put(ProjectGraphDO.class.getTypeName(), projectGraphRepository);
        doAndRepository.put(ProjectInstDO.class.getTypeName(), projectInstRepository);
        doAndRepository.put(ProjectJobDO.class.getTypeName(), projectJobRepository);
        doAndRepository.put(ProjectJobTaskLogDO.class.getTypeName(), projectJobTaskLogRepository);
        doAndRepository.put(ProjectTaskDO.class.getTypeName(), projectJobTaskRepository);
        doAndRepository.put(ProjectModelDO.class.getTypeName(), projectModelRepository);
        doAndRepository.put(ProjectNodeDO.class.getTypeName(), projectNodeRepository);
        doAndRepository.put(ProjectReportDO.class.getTypeName(), projectReportRepository);
        doAndRepository.put(ProjectDO.class.getTypeName(), projectRepository);
        doAndRepository.put(ProjectResultDO.class.getTypeName(), projectResultRepository);
        doAndRepository.put(ProjectRuleDO.class.getTypeName(), projectRuleRepository);
        doAndRepository.put(AccountsDO.class.getTypeName(), userAccountsRepository);
        doAndRepository.put(TokensDO.class.getTypeName(), userTokensRepository);
        doAndRepository.put(TeeDownLoadAuditConfigDO.class.getTypeName(), teeDownLoadAuditConfigRepository);
        doAndRepository.put(TeeNodeDatatableManagementDO.class.getTypeName(), teeNodeDatatableManagementRepository);
        doAndRepository.put(VoteInviteDO.class.getTypeName(), voteInviteRepository);
        doAndRepository.put(VoteRequestDO.class.getTypeName(), voteRequestRepository);
        doAndRepository.put(NodeRouteApprovalConfigDO.class.getTypeName(), nodeRouteAuditConfigRepository);
        doAndRepository.put(ProjectApprovalConfigDO.class.getTypeName(), projectApprovalConfigRepository);
        doAndRepository.put(ProjectGraphNodeKusciaParamsDO.class.getTypeName(), projectGraphNodeKusciaParamsRepository);
        doAndRepository.put(ProjectModelServingDO.class.getTypeName(), projectModelServiceRepository);
        doAndRepository.put(ProjectModelPackDO.class.getTypeName(), projectModelPackRepository);
        doAndRepository.put(ProjectFeatureTableDO.class.getTypeName(), projectFeatureTableRepository);
        doAndRepository.put(FeatureTableDO.class.getTypeName(), featureTableRepository);
        doAndRepository.put(ProjectGraphDomainDatasourceDO.class.getTypeName(), projectGraphDomainDatasourceRepository);
    }

    @SuppressWarnings(value = {"rawtypes"})
    public synchronized void syncData(SyncDataDTO dto) {
        if (ignore(dto)) {
            log.info(" ****** sync ignore dto {}", dto);
            return;
        }
        UserContext.setBaseUser(UserContextDTO.builder().name("admin").build());
        String action = dto.getAction();
        Object data = dto.getData();
        BaseRepository baseRepository = doAndRepository.get(dto.getTableName());
        switch (action) {
            case "create", "update" -> baseRepository.save(data);
            case "remove" -> baseRepository.delete(data);
            default -> log.error("can not find action:{}", action);
        }
        UserContext.remove();
    }

    @SuppressWarnings(value = {"rawtypes"})
    public synchronized void syncDataP2p(SyncDataDTO dto) {
        UserContext.setBaseUser(UserContextDTO.builder().name("admin").build());
        String action = dto.getAction();
        Object data = dto.getData();
        BaseRepository baseRepository = doAndRepository.get(dto.getTableName());
        // todo check last update version
        if (!(data instanceof VoteRequestDO || data instanceof VoteInviteDO || data instanceof ProjectApprovalConfigDO)) {
            DataSyncConsumerContext.setConsumerSync();
        }
        if (data instanceof ProjectJobDO projectJobDO) {
            Optional<ProjectJobDO> byJobId = projectJobRepository.findByJobId(projectJobDO.getUpk().getJobId());
            if (byJobId.isPresent()) {
                if (byJobId.get().isFinished()) {
                    log.info("ignore sync by local job is finished {}", byJobId.get().getUpk().getJobId());
                    return;
                }
            }
        }
        switch (action) {
            case "create", "update" -> baseRepository.save(data);
            case "remove" -> baseRepository.delete(data);
            default -> log.warn("can not find action:{}", action);
        }
        UserContext.remove();
        DataSyncConsumerContext.remove();
    }

    private boolean ignore(@SuppressWarnings(value = {"rawtypes"}) SyncDataDTO dto) {
        String tableName = dto.getTableName();
        if (VoteRequestDO.class.getTypeName().equals(tableName)) {
            Object data = dto.getData();
            VoteRequestDO voteRequestDO = (VoteRequestDO) data;
            return VoteExecuteEnum.SUCCESS.name().equals(voteRequestDO.getExecuteStatus()) || VoteExecuteEnum.OBSERVER.name().equals(voteRequestDO.getExecuteStatus());
        }
        return false;
    }

    public Object logTableLastUpdateTime(String tableName) {
        String name = getRealTableName(tableName);
        if ("node".equals(name)) {
            edgeDataSyncLogRepository.save(EdgeDataSyncLogDO.builder().lastUpdateTime("0").tableName(name).build());
            return "0";
        }
        Query nativeQuery = entityManager.createNativeQuery("select max(gmt_create) from " + name, String.class);
        @SuppressWarnings(value = {"rawtypes"})
        List resultList = nativeQuery.getResultList();
        Object lastUpdateTime = "0";
        if (!CollectionUtils.isEmpty(resultList)) {
            Object s = resultList.get(0);
            if (ObjectUtils.isNotEmpty(s)) {
                lastUpdateTime = s;
            }
        }
        edgeDataSyncLogRepository.save(EdgeDataSyncLogDO.builder().lastUpdateTime(lastUpdateTime.toString()).tableName(name).build());
        return lastUpdateTime;
    }

    @Async
    public void syncByLastUpdateTime(String nodeId) {
        @SuppressWarnings(value = {"rawtypes"})
        List<SyncDataDTO> syncDataDTOList = SseSession.sessionTableMap.get(nodeId);
        if (!CollectionUtils.isEmpty(syncDataDTOList)) {
            syncDataDTOList.forEach(s -> {
                String tableName = s.getTableName();
                String lastUpdateTime = s.getLastUpdateTime();
                List<ProjectNodesInfo> resultList = findDoByTime(s);
                resultList.forEach(r -> {
                    try {
                        if (r instanceof ProjectFeatureTableDO) {
                            Optional<FeatureTableDO> featureTableDOOptional = featureTableRepository.findById(new FeatureTableDO.UPK(((ProjectFeatureTableDO) r).getUpk().getFeatureTableId(), ((ProjectFeatureTableDO) r).getUpk().getNodeId(), ((ProjectFeatureTableDO) r).getUpk().getDatasourceId()));
                            if (featureTableDOOptional.isEmpty()) {
                                log.warn("featureTableDOOptional is empty");
                            }
                            ((ProjectFeatureTableDO) r).setFeatureTable(featureTableDOOptional.get());
                        }
                        if (SseSendFlag(r, nodeId)) {
                            SseSession.send(nodeId, SyncDataDTO.builder()
                                    .data(r)
                                    .action(DbChangeAction.CREATE.val)
                                    .tableName(tableName)
                                    .lastUpdateTime(lastUpdateTime)
                                    .build());
                        }
                    } catch (IOException e) {
                        log.error("sse sync error ", e);
                    }
                });
            });
        }
    }

    @SuppressWarnings(value = {"rawtypes"})
    public List<ProjectNodesInfo> findDoByTime(SyncDataDTO s) {
        String tableName = s.getTableName();
        String lastUpdateTime = s.getLastUpdateTime();
        BaseRepository baseRepository = doAndRepository.get(tableName);
        Specification<BaseAggregationRoot> gmtModified = null;
        if (!"0".equals(lastUpdateTime)) {
            LocalDateTime parse = LocalDateTime.parse(s.getLastUpdateTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            gmtModified = (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get("gmtModified"), parse);
        }
        List<ProjectNodesInfo> resultList = ObjectUtils.isEmpty(gmtModified) ? baseRepository.findAll() : baseRepository.findAll(gmtModified);
        log.info("data sync start table: {} ,lastUpdateTime:{}, num: {}", tableName, lastUpdateTime, resultList.size());
        return resultList;
    }

    public boolean SseSendFlag(ProjectNodesInfo p, String nodeId) {
        if (p instanceof NodeDO) {
            return true;
        }
        List<String> nodeIds = p.getNodeIds();
        String projectId = p.getProjectId();
        if (CollectionUtils.isEmpty(nodeIds)) {
            nodeIds = new ArrayList<>();
            List<ProjectNodeDO> byProjectId = projectNodeRepository.findByProjectId(projectId);
            if (!CollectionUtils.isEmpty(byProjectId)) {
                for (ProjectNodeDO b : byProjectId) {
                    nodeIds.add(b.getNodeId());
                }
            }
        }
        return nodeIds.contains(nodeId);
    }

    private String getRealTableName(String tableName) {
        Class<?> aClass;
        try {
            aClass = Class.forName(tableName);
        } catch (ClassNotFoundException e) {
            throw SecretpadException.of(SystemErrorCode.SSE_ERROR, "no such table " + tableName);
        }
        Table annotation = aClass.getAnnotation(Table.class);
        return annotation.name();
    }
}