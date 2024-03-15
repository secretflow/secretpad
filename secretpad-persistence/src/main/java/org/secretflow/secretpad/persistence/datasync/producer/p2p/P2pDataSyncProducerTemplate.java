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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
public class P2pDataSyncProducerTemplate extends AbstractDataSyncProducerTemplate {
    private static final Map<String, String> ownerId_cache = new HashMap<>();

    @Value("${secretpad.node-id}")
    private String localNodeId;

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
            case "ProjectTaskDO" -> filterProjectTaskDO(event);
            case "ProjectDatatableDO" -> filterProjectDatatableDO(event);
            case "VoteRequestDO" -> filterVoteRequestDO(event);
            case "VoteInviteDO" -> filterVoteInviteDO(event);
            case "ProjectApprovalConfigDO" -> filterProjectApprovalConfigDO(event);
            case "ProjectNodeDO" -> filterProjectNodeDO(event);
            case "ProjectGraphNodeKusciaParamsDO" -> filterProjectGraphNodeKusciaParamsDO(event);
            case "ProjectModelServingDO" -> filterProjectModelServingDO(event);
            case "ProjectModelPackDO" -> filterProjectModelPackDO(event);
            case "ProjectFeatureTableDO" -> filterProjectFeatureTableDO(event);
            default -> false;
        };
    }


    private boolean filterProjectNodeDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectNodeDO source = (ProjectNodeDO) event.getSource();
        String projectId = source.getProjectId();
        String ownerId = ownerId_cache.get(projectId);
        if (!StringUtils.equals(localNodeId, ownerId)) {
            log.debug("local node not initiator,stop sync");
            return true;
        }
        return false;
    }

    private boolean filterProjectModelPackDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectModelPackDO source = (ProjectModelPackDO) event.getSource();
        String initiator = source.getInitiator();
        if (!StringUtils.equals(localNodeId, initiator)) {
            log.debug("local node not initiator,stop sync");
            return true;
        }
        return false;
    }


    private boolean filterProjectFeatureTableDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectFeatureTableDO source = (ProjectFeatureTableDO) event.getSource();
        String initiator = source.getUpk().getNodeId();
        if (!StringUtils.equals(localNodeId, initiator)) {
            log.debug("local node not initiator,stop sync");
            return true;
        }
        return false;
    }

    private boolean filterProjectModelServingDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectModelServingDO source = (ProjectModelServingDO) event.getSource();
        String initiator = source.getInitiator();
        if (!StringUtils.equals(localNodeId, initiator)) {
            log.debug("local node not initiator,stop sync");
            return true;
        }
        return false;
    }

    @Async
    @Override
    public void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        List<String> sync = dataSyncConfig.getSync();
        String dType = event.getDType();
        if (!sync.contains(dType)) {
            return;
        }
        log.debug("------- data:{} action:{} projectId: {} nodeIds: {}", event.getDType(), event.getAction(), event.getProjectId(), event.getNodeIds());
        p2pPaddingNodeServiceImpl.paddingNodes(event);
        log.debug("-------after paddingNodes data:{} action:{} projectId: {} nodeIds: {}", event.getDType(), event.getAction(), event.getProjectId(), event.getNodeIds());
        if (PlatformTypeEnum.valueOf(platformType).equals(PlatformTypeEnum.AUTONOMY) && !filter(event)) {
            log.debug("start to push");
            dataSyncDataBufferTemplate.push(event);
        }
    }

    private boolean filterProject(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectDO source = (ProjectDO) event.getSource();
        String ownerId = source.getOwnerId();
        ownerId_cache.put(source.getProjectId(), ownerId);
        return !StringUtils.equals(ownerId, localNodeId);
    }

    private boolean filterProjectGraphNodeKusciaParamsDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphNodeKusciaParamsDO source = (ProjectGraphNodeKusciaParamsDO) event.getSource();
        return !StringUtils.equals(localNodeId, ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getUpk().getGraphId()));
    }

    private boolean filterProjectGraph(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphDO source = (ProjectGraphDO) event.getSource();
        ownerId_cache.put(source.getUpk().getProjectId() + "_" + source.getUpk().getGraphId(), source.getOwnerId());
        return !StringUtils.equals(localNodeId, source.getOwnerId());
    }

    private boolean filterProjectGraphNode(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectGraphNodeDO source = (ProjectGraphNodeDO) event.getSource();
        String ownerId = ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getUpk().getGraphId());
        return !StringUtils.equals(localNodeId, ownerId);
    }

    private boolean filterProjectJobDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectJobDO source = (ProjectJobDO) event.getSource();
        String ownerId = ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getGraphId());
        return !StringUtils.equals(localNodeId, ownerId) && StringUtils.equalsIgnoreCase(DbChangeAction.UPDATE.getVal(), event.getDType());
    }

    private boolean filterProjectDatatableDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectDatatableDO source = (ProjectDatatableDO) event.getSource();
        String nodeId = source.getNodeId();
        return !StringUtils.equals(localNodeId, nodeId);
    }

    private boolean filterProjectTaskDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        ProjectTaskDO source = (ProjectTaskDO) event.getSource();
        String ownerId = ownerId_cache.get(source.getUpk().getProjectId() + "_" + source.getGraphNode().getUpk().getGraphId());
        return !StringUtils.equals(localNodeId, ownerId) && StringUtils.equalsIgnoreCase(DbChangeAction.UPDATE.getVal(), event.getDType());
    }

    private boolean filterVoteRequestDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        VoteRequestDO source = (VoteRequestDO) event.getSource();
        String initiator = source.getInitiator();
        return !StringUtils.equals(initiator, localNodeId);
    }

    private boolean filterVoteInviteDO(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        VoteInviteDO source = (VoteInviteDO) event.getSource();
        String initiator = source.getInitiator();
        String action = event.getAction();
        if (StringUtils.equals(initiator, localNodeId)) {
            p2pPaddingNodeServiceImpl.compensate(event);
            return !DbChangeAction.CREATE.getVal().equals(action);
        }
        if (!StringUtils.equals(initiator, localNodeId)) {
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
        return !StringUtils.equals(initiator, localNodeId);
    }


    @Override
    public void pushIgnoreFilter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        p2pPaddingNodeServiceImpl.paddingNodes(event);
        if (PlatformTypeEnum.valueOf(platformType).equals(PlatformTypeEnum.AUTONOMY)) {
            dataSyncDataBufferTemplate.push(event);
        }
    }
}