/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.persistence.model.GraphEdgeDO;

import java.util.*;

/**
 * @author chenmingliang
 * @date 2024/03/27
 */
public final class GraphUtils {

    private GraphUtils() {
    }

    /**
     * @param edges  all edges of graph
     * @param nodeId the node you want to find its top nodes
     * @return top nodes
     */
    public static Set<String> findTopNodes(List<GraphEdgeDO> edges, String nodeId) {
        Map<String, List<String>> incomingEdges = new HashMap<>();
        for (GraphEdgeDO edgeDO : edges) {
            incomingEdges.computeIfAbsent(edgeDO.getTarget(), k -> new ArrayList<>()).add(edgeDO.getSource());
        }
        Deque<String> container = new ArrayDeque<>();
        container.push(nodeId);
        Set<String> result = new HashSet<>();
        while (!container.isEmpty()) {
            String currentId = container.pop();
            if (!incomingEdges.containsKey(currentId)) {
                result.add(currentId);
                continue;
            }
            for (String sourceId : incomingEdges.get(currentId)) {
                container.push(sourceId);
            }
        }
        return result;
    }
}
