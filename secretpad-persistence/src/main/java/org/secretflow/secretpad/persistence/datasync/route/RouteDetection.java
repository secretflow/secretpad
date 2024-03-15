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

package org.secretflow.secretpad.persistence.datasync.route;

import org.secretflow.secretpad.persistence.datasync.event.P2pDataSyncSendEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author yutu
 * @date 2023/12/13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteDetection {
    private static final Set<String> AvailableNode = new CopyOnWriteArraySet<>();
    private final ApplicationEventPublisher applicationEventPublisher;

    public void addAvailableNode(String node) {
        log.debug("routeDetection addAvailableNode {}", node);
        if (!AvailableNode.contains(node)) {
            AvailableNode.add(node);
            applicationEventPublisher.publishEvent(new P2pDataSyncSendEvent(this, node));
        }
    }

    public void removeAvailableNode(String node) {
        log.debug("routeDetection removeAvailableNode {}", node);
        AvailableNode.remove(node);
    }

    public void clearAvailableNodes() {
        log.debug("routeDetection clearAvailableNodes");
        AvailableNode.clear();
    }

    public boolean checkNode(String node) {
        log.debug("routeDetection checkNode {}", node);
        return AvailableNode.contains(node);
    }

    public Set<String> getAvailableNodes() {
        log.debug("routeDetection getAvailableNodes ");
        return AvailableNode;
    }

}