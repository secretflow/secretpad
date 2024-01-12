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

import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.service.sync.JpaSyncDataService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
@Service
public class DataSyncConsumerTemplate {
    @Resource
    private JpaSyncDataService jpaSyncDataService;
    @Resource
    private JobManager jobManager;

    public SyncDataDTO consumer(String nodeId, SyncDataDTO syncDataDTO) {
        checkSourceNodeId(nodeId, syncDataDTO);
        UserContext.setBaseUser(UserContextDTO.builder().name("admin").build());
        jpaSyncDataService.syncDataP2p(syncDataDTO);
        Object data = syncDataDTO.getData();
        if (data instanceof BaseAggregationRoot) {
            log.debug("consumer data instanceof BaseAggregationRoot");
        }
        if (data instanceof ProjectTaskDO) {
            ProjectTaskDO taskDO = (ProjectTaskDO) data;
            GraphNodeTaskStatus status = taskDO.getStatus();
            if (status == GraphNodeTaskStatus.SUCCEED) {
                jobManager.syncResult(taskDO);
            }
        }
        UserContext.remove();
        return syncDataDTO;
    }

    private void checkSourceNodeId(String nodeId, SyncDataDTO syncDataDTO) {
        log.info("dataSyncConsumer consumer {} {}", nodeId, syncDataDTO);
    }

}