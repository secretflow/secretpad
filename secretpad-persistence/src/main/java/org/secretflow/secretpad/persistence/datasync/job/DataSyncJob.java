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

package org.secretflow.secretpad.persistence.datasync.job;

import org.secretflow.secretpad.persistence.datasync.event.P2pDataSyncSendEvent;
import org.secretflow.secretpad.persistence.datasync.rest.DataSyncRestTemplate;
import org.secretflow.secretpad.persistence.datasync.route.RouteDetection;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * data sync job
 *
 * @author yutu
 * @date 2023/12/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSyncJob implements ApplicationListener<P2pDataSyncSendEvent> {

    private final DataSyncRestTemplate dataSyncRestTemplate;
    private final RouteDetection routeDetection;

    private static final Map<String, String> NODE_WORK_THREAD_NAME = new ConcurrentHashMap<>();


    public void work(String node) throws InterruptedException {
        Set<String> availableNodes = routeDetection.getAvailableNodes();
        String threadName = Thread.currentThread().getName();
        if (availableNodes.contains(node)) {
            log.debug("{} start", threadName);
            dataSyncRestTemplate.send(node);
        } else {
            log.warn("{} dataSyncJob work ignore , because {} is not alive", threadName, node);
        }

    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Async("dataSyncThreadPool")
    @Override
    public void onApplicationEvent(P2pDataSyncSendEvent event) {
        String node = event.getNode();
        try {
            log.debug("start data sync to {}", node);
            if (NODE_WORK_THREAD_NAME.containsKey(node)) {
                log.info("{} is working now {}, skip it", node, NODE_WORK_THREAD_NAME.get(node));
                return;
            }
            NODE_WORK_THREAD_NAME.put(node, Thread.currentThread().getName());
            work(node);
        } catch (Exception e) {
            log.error("dataSyncJob work error", e);
        } finally {
            NODE_WORK_THREAD_NAME.remove(node);
        }
    }
}