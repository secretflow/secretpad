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

package org.secretflow.secretpad.persistence.datasync.rest.p2p;

import org.secretflow.secretpad.common.dto.SecretPadResponse;
import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.rest.DataSyncRestTemplate;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
@RequiredArgsConstructor
public class P2pDataSyncRestTemplate extends DataSyncRestTemplate {
    @Override
    public EntityChangeListener.DbChangeEvent<BaseAggregationRoot> send(String node) throws InterruptedException {
        int size = dataSyncDataBufferTemplate.size(node);
        EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event = null;
        while (size > 0) {
            log.debug("data sync start to send {}, now size {}", node, size);
            event = dataSyncDataBufferTemplate.peek(node);
            if (!ObjectUtils.isEmpty(event)) {
                SecretPadResponse<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>> syncResp;
                try {
                    SyncDataDTO<Object> syncDataDTO = SyncDataDTO.builder()
                            .tableName(event.getDType())
                            .action(event.getAction())
                            .data(event.getSource()).build();
                    syncResp = p2pDataSyncRestService.sync(node, "secretpad." + node + ".svc", syncDataDTO.toJson()).block(Duration.ofSeconds(5));
                    if (0 == syncResp.getStatus().getCode()) {
                        onSuccess(node, event);
                    } else {
                        log.error("P2pDataSyncRestTemplate send error,{} {}"
                                , syncResp.getStatus().getCode()
                                , syncResp.getStatus().getMsg());
                        onError(node, event);
                    }
                } catch (Exception e) {
                    log.error("P2pDataSyncRestTemplate send error", e);
                    onError(node, event);
                }
                size = dataSyncDataBufferTemplate.size(node);
                log.debug("data sync end to send {}, now size {}", node, size);
            } else {
                log.warn("data sync end to send {}, now size {} event is {}", node, size, event);
                return event;
            }
        }
        return event;
    }

    @Override
    public void onError(String node, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        int retry = dataSyncRetryTemplate.retry(node, event);
        if (retry == -1) {
            dataSyncDataBufferTemplate.commit(node, event);
        }
    }

    @Override
    public void onSuccess(String node, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        dataSyncDataBufferTemplate.commit(node, event);
    }
}