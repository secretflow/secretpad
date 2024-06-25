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

package org.secretflow.secretpad.service.listener;

import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.service.sync.center.SseSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Database change event listener
 *
 * @author zhiyin
 * @date 2023/10/19
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings(value = {"rawtypes"})
public class DbChangeEventListener {
    private final ProjectNodeRepository projectNodeRepository;
    private final FeatureTableRepository featureTableRepository;
    private final DataSyncDataBufferTemplate dataSyncDataBufferTemplate;

    private void sync(EntityChangeListener.DbChangeEvent event) {
        if (event.getSource() instanceof ProjectFeatureTableDO) {
            ProjectNodesInfo r = event.getSource();
            Optional<FeatureTableDO> featureTableDOOptional = featureTableRepository.findById(new FeatureTableDO.UPK(((ProjectFeatureTableDO) r).getUpk().getFeatureTableId(), ((ProjectFeatureTableDO) r).getUpk().getNodeId(), ((ProjectFeatureTableDO) r).getUpk().getDatasourceId()));
            if (featureTableDOOptional.isEmpty()) {
                log.warn("featureTableDOOptional is empty");
            }
            ((ProjectFeatureTableDO) r).setFeatureTable(featureTableDOOptional.get());
        }
        if (event.getSource() instanceof NodeDO) {
            log.info("*** get data sync , filter {} will be send", SseSession.sessionMap.keySet());
            SseSession.sendAll(SyncDataDTO.builder()
                    .tableName(event.getDType())
                    .action(event.getAction())
                    .data(event.getSource()).build());
            return;
        }
        List<String> nodeIds = event.getNodeIds();
        log.info("*** before {} will be send to {}", event, nodeIds);
        if (CollectionUtils.isEmpty(nodeIds)) {
            nodeIds = new ArrayList<>();
            String projectId = event.getProjectId();
            if (StringUtils.isNotEmpty(projectId)) {
                List<String> byProjectId = projectNodeRepository.findProjectNodesByProjectId(projectId);
                if (!CollectionUtils.isEmpty(byProjectId)) {
                    nodeIds.addAll(byProjectId);
                }
            }
        }
        log.info("*** get data sync , filter {} will be send", nodeIds);
        nodeIds.forEach(n -> {
            List<SyncDataDTO> syncDataDTOList = SseSession.sessionTableMap.get(n);
            if (!CollectionUtils.isEmpty(syncDataDTOList)) {
                syncDataDTOList.forEach(s -> {
                    if (s.getTableName().equals(event.getDType())) {
                        try {
                            SseSession.send(n, SyncDataDTO.builder()
                                    .tableName(event.getDType())
                                    .action(event.getAction())
                                    .data(event.getSource()).build());
                        } catch (IOException e) {
                            log.error("data sync error ", e);
                            // if push failed, retry
                            dataSyncDataBufferTemplate.push(event);
                        }
                    }
                });
            }
        });
    }

    @Scheduled(initialDelay = 1000, fixedRate = 3000)
    public void sync() throws InterruptedException {
        //noinspection InfiniteLoopStatement
        while (true) {
            EntityChangeListener.DbChangeEvent e = dataSyncDataBufferTemplate.peek(null);
            sync(e);
            log.info("*** get data sync , start to send *** {} , {} wait to sync", e, dataSyncDataBufferTemplate.size(null));
        }
    }
}