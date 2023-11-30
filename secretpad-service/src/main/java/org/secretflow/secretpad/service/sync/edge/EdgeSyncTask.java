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

package org.secretflow.secretpad.service.sync.edge;

import org.secretflow.secretpad.common.constant.SystemConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author yutu
 * @date 2023/10/27
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "edge.sync.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Profile(value = {SystemConstants.EDGE})
public class EdgeSyncTask {
    private final EdgeDataSyncService edgeDataSyncService;

    @Scheduled(fixedRate = 5000)
    public void startSse() {
        if (-1 == EdgeDataSyncServiceImpl.sseSate.get()) {
            edgeDataSyncService.start();
        }
    }
}
