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

package org.secretflow.secretpad.service.sync.p2p;

import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;
import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.entity.ProjectGraphDomainDatasourceDO;
import org.secretflow.secretpad.persistence.model.DbChangeAction;
import org.secretflow.secretpad.service.impl.ProjectGraphDomainDatasourceServiceImpl;
import org.secretflow.secretpad.service.sync.JpaSyncDataService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
@Service
public class DataSyncConsumerTemplate {

    @Value("${secretpad.node-id}")
    private String nodeId;

    @Value("${secretpad.platform-type}")
    private String platformType;

    @Resource
    private JpaSyncDataService jpaSyncDataService;

    @Resource
    private ProjectGraphDomainDatasourceServiceImpl projectGraphDomainDatasourceService;

    @Resource
    private AbstractNodeManager nodeManager;

    public SyncDataDTO consumer(String nodeId, SyncDataDTO syncDataDTO) {
        checkSourceNodeId(nodeId, syncDataDTO);
        UserContext.setBaseUser(UserContextDTO.builder()
                .name(SystemConstants.USER_ADMIN)
                .ownerId(nodeId)
                .platformType(PlatformTypeEnum.AUTONOMY)
                .ownerType(UserOwnerTypeEnum.P2P)
                .build());
        jpaSyncDataService.syncDataP2p(syncDataDTO);
        Object data = syncDataDTO.getData();
        if (data instanceof BaseAggregationRoot) {
            log.debug("consumer data instanceof BaseAggregationRoot");
            if (data instanceof ProjectGraphDomainDatasourceDO datasourceDO && syncDataDTO.getAction().equalsIgnoreCase(DbChangeAction.CREATE.getVal())) {
                log.info("consumer data instanceof ProjectGraphDomainDatasourceDO {}", datasourceDO);
                Set<String> nodeIdSet = new HashSet<>();
                nodeIdSet.add(this.nodeId);
                if (PlatformTypeEnum.AUTONOMY.name().equals(this.platformType)){
                    Set<String> targetNodeIds = nodeManager.getTargetNodeIds(datasourceDO.getNodeId(), datasourceDO.getProjectId());
                    nodeIdSet.addAll(targetNodeIds);
                }
                nodeIdSet.forEach(targetNodeId -> {
                    ProjectGraphDomainDatasourceDO newDatasourceDO = new ProjectGraphDomainDatasourceDO();
                    newDatasourceDO.setUpk(ProjectGraphDomainDatasourceDO.UPK.builder()
                            .graphId(datasourceDO.getUpk().getGraphId())
                            .projectId(datasourceDO.getUpk().getProjectId())
                            .domainId(targetNodeId)
                            .build());
                    newDatasourceDO.setDataSourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE);
                    newDatasourceDO.setDataSourceName(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE);
                    newDatasourceDO.setEditEnable(Boolean.TRUE);
                    log.info("consumer data ProjectGraphDomainDatasourceDO event handle {}", newDatasourceDO);
                    projectGraphDomainDatasourceService.save(newDatasourceDO);
                });
            }
        }
        UserContext.remove();
        return syncDataDTO;
    }

    private void checkSourceNodeId(String nodeId, SyncDataDTO syncDataDTO) {
        log.info("dataSyncConsumer consumer {} {}", nodeId, syncDataDTO);
    }

}