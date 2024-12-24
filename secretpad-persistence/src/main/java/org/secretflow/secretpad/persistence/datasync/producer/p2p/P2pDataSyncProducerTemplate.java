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

package org.secretflow.secretpad.persistence.datasync.producer.p2p;

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.producer.AbstractDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.datasync.producer.PaddingNodeService;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.persistence.model.DbChangeAction;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;

import java.util.*;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
public class P2pDataSyncProducerTemplate extends AbstractDataSyncProducerTemplate {
    public static final Map<String, String> ownerId_cache = new HashMap<>();
    public static Set<String> nodeIds = new HashSet<>();
    public static String instId;

    public P2pDataSyncProducerTemplate(DataSyncConfig dataSyncConfig, DataSyncDataBufferTemplate dataSyncDataBufferTemplate, PaddingNodeService p2pPaddingNodeServiceImpl) {
        super(dataSyncConfig, dataSyncDataBufferTemplate, p2pPaddingNodeServiceImpl);
    }


    @Override
    public boolean filter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectNodesInfo source = event.getSource();
        return switch (source.getClass().getSimpleName()) {
            case "ProjectDO" -> filterProject(event);
            case "ProjectGraphDO" -> filterProjectGraph(event);
            case "ProjectGraphNodeDO" -> filterProjectGraphNode(event);
            case "ProjectJobDO" -> filterProjectJobDO(event);
            case "ProjectDatatableDO" -> filterProjectDatatableDO(event);
            case "VoteRequestDO" -> filterVoteRequestDO(event);
            case "VoteInviteDO" -> filterVoteInviteDO(event);
            case "ProjectApprovalConfigDO" -> filterProjectApprovalConfigDO(event);
            case "ProjectNodeDO" -> filterProjectNodeDO(event);
            case "ProjectGraphNodeKusciaParamsDO" -> filterProjectGraphNodeKusciaParamsDO(event);
            case "ProjectModelServingDO" -> filterProjectModelServingDO(event);
            case "ProjectModelPackDO" -> filterProjectModelPackDO(event);
            case "ProjectFeatureTableDO" -> filterProjectFeatureTableDO(event);
            case "ProjectGraphDomainDatasourceDO" -> filterProjectGraphDomainDatasourceDO(event);
            case "ProjectInstDO" -> filterProjectInstDO(event);
            case "ProjectScheduleDO" -> filterProjectScheduleDO(event);
            case "ProjectScheduleJobDO" -> filterProjectScheduleJobDO(event);
            case "ProjectScheduleTaskDO" -> filterProjectScheduleTaskDO(event);
            default -> false;
        };
    }

    @Async
    @Override
    public void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        try {
            List<String> sync = dataSyncConfig.getSync();
            String dType = event.getDType();
            if (!sync.contains(dType)) {
                return;
            }
            log.debug("before paddingNodes data:{} action:{} projectId: {} nodeIds: {}", event.getDType(), event.getAction(), event.getProjectId(), event.getNodeIds());
            p2pPaddingNodeServiceImpl.paddingNodes(event);
            log.debug("after paddingNodes data:{} action:{} projectId: {} nodeIds: {}", event.getDType(), event.getAction(), event.getProjectId(), event.getNodeIds());
            if (PlatformTypeEnum.valueOf(platformType).equals(PlatformTypeEnum.AUTONOMY) && !filter(event)) {
                log.debug("start to push");
                dataSyncDataBufferTemplate.push(event);
            }
        } catch (Exception e) {
            log.error("P2pDataSyncProducerTemplate push error", e);
        }
    }

    private boolean filterProjectGraphDomainDatasourceDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphDomainDatasourceDO source = (ProjectGraphDomainDatasourceDO) event.getSource();
        String nodeId = source.getUpk().getDomainId();
        if (!nodeIds.contains(nodeId)) {
            log.debug("ProjectGraphDomainDatasourceDO local node not initiator,stop sync {} {}", nodeId, nodeIds);
            return true;
        }
        return false;
    }


    private boolean filterProjectNodeDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectNodeDO source = (ProjectNodeDO) event.getSource();
        String projectId = source.getProjectId();
        String ownerId = ownerId_cache.get(projectId);
        if (!StringUtils.equals(instId, ownerId)) {
            log.debug("ProjectNodeDO local node not initiator,stop sync {}", instId);
            return true;
        }
        return false;
    }

    private boolean filterProjectModelPackDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectModelPackDO source = (ProjectModelPackDO) event.getSource();
        String initiator = source.getInitiator();
        if (!nodeIds.contains(initiator)) {
            log.debug("ProjectModelPackDO local node not initiator,stop sync {} {}", initiator, nodeIds);
            return true;
        }
        return false;
    }


    private boolean filterProjectFeatureTableDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectFeatureTableDO source = (ProjectFeatureTableDO) event.getSource();
        String initiator = source.getUpk().getNodeId();
        if (!nodeIds.contains(initiator)) {
            log.debug("ProjectFeatureTableDO local node not initiator,stop sync {} {}", initiator, nodeIds);
            return true;
        }
        return false;
    }

    private boolean filterProjectModelServingDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectModelServingDO source = (ProjectModelServingDO) event.getSource();
        String initiator = source.getInitiator();
        if (!nodeIds.contains(initiator)) {
            log.debug("ProjectModelServingDO local node not initiator,stop sync {} {}", initiator, nodeIds);
            return true;
        }
        return false;
    }


    private boolean filterProject(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectDO source = (ProjectDO) event.getSource();
        String ownerId = source.getOwnerId();
        ownerId_cache.put(source.getProjectId(), ownerId);
        return !StringUtils.equals(ownerId, instId);
    }

    private boolean filterProjectGraphNodeKusciaParamsDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphNodeKusciaParamsDO source = (ProjectGraphNodeKusciaParamsDO) event.getSource();
        return !instId.equals(ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getUpk().getGraphId()));
    }

    private boolean filterProjectGraph(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphDO source = (ProjectGraphDO) event.getSource();
        ownerId_cache.put(source.getUpk().getProjectId() + "_" + source.getUpk().getGraphId(), source.getOwnerId());
        return !instId.equals(source.getOwnerId());
    }

    private boolean filterProjectGraphNode(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphNodeDO source = (ProjectGraphNodeDO) event.getSource();
        String ownerId = ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getUpk().getGraphId());
        return !instId.equals(ownerId);
    }

    private boolean filterProjectJobDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectJobDO source = (ProjectJobDO) event.getSource();
        String ownerId = ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getGraphId());
        log.debug("filterProjectJobDO ownerId:{} localNodeId:{} event:{}", ownerId, instId, event);
        if (StringUtils.equalsIgnoreCase(DbChangeAction.UPDATE.getVal(), event.getAction())) {
            if (source.getStatus() == GraphJobStatus.STOPPED) {
                return !StringUtils.equals(instId, ownerId);
            }
            return true;
        }
        return !instId.equals(ownerId);
    }

    private boolean filterProjectDatatableDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectDatatableDO source = (ProjectDatatableDO) event.getSource();
        String nodeId = source.getNodeId();
        return !nodeIds.contains(nodeId);
    }

    private boolean filterVoteRequestDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        VoteRequestDO source = (VoteRequestDO) event.getSource();
        String initiator = source.getInitiator();
        return !StringUtils.equals(initiator, instId);
    }

    private boolean filterVoteInviteDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        VoteInviteDO source = (VoteInviteDO) event.getSource();
        String initiator = source.getInitiator();
        String action = event.getAction();
        if (StringUtils.equals(initiator, instId)) {
            p2pPaddingNodeServiceImpl.compensate(event);
            return !DbChangeAction.CREATE.getVal().equals(action);
        }
        if (!StringUtils.equals(initiator, instId)) {
            if (DbChangeAction.UPDATE.getVal().equals(action)) {
                event.setNodeIds(List.of(initiator));
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean filterProjectApprovalConfigDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectApprovalConfigDO source = (ProjectApprovalConfigDO) event.getSource();
        String initiator = source.getInitiator();
        return !StringUtils.equals(initiator, instId);
    }

    private boolean filterProjectInstDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectInstDO source = (ProjectInstDO) event.getSource();
        String projectId = source.getProjectId();
        String ownerId = ownerId_cache.get(projectId);
        if (!StringUtils.equals(instId, ownerId)) {
            log.debug("ProjectInstDO local node not initiator,stop sync {}", instId);
            return true;
        }
        return false;
    }

    private boolean filterProjectScheduleTaskDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectScheduleTaskDO source = (ProjectScheduleTaskDO) event.getSource();
        String owner = source.getOwner();
        return !StringUtils.equals(owner, instId);
    }

    private boolean filterProjectScheduleJobDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectScheduleJobDO source = (ProjectScheduleJobDO) event.getSource();
        String owner = source.getOwner();
        return !StringUtils.equals(owner, instId);
    }

    private boolean filterProjectScheduleDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectScheduleDO source = (ProjectScheduleDO) event.getSource();
        String owner = source.getOwner();
        return !StringUtils.equals(owner, instId);
    }


    @Override
    public void pushIgnoreFilter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        p2pPaddingNodeServiceImpl.paddingNodes(event);
        if (PlatformTypeEnum.valueOf(platformType).equals(PlatformTypeEnum.AUTONOMY)) {
            dataSyncDataBufferTemplate.push(event);
        }
    }
}