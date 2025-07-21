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

package org.secretflow.secretpad.service.graph;

import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Graph builder
 *
 * @author yansi
 * @date 2023/6/4
 */
@Slf4j
public class GraphBuilder {
    private final Map<String, List<String>> dependencies = new HashMap<>();
    private final Map<String, String> output2node = new HashMap<>();
    private final Map<String, String> input2node = new HashMap<>();
    private final Map<String, GraphNodeInfo> nodeMap = new HashMap<>();

    /**
     * Build graph builder via graph node information list
     *
     * @param nodes graph node information list
     */
    public GraphBuilder(List<GraphNodeInfo> nodes) {
        Map<String, List<String>> node2inputs = new HashMap<>();
        if (!CollectionUtils.isEmpty(nodes)) {
            for (GraphNodeInfo node : nodes) {
                String nodeId = node.getGraphNodeId();
                List<String> outputs = node.getOutputs();
                if (!CollectionUtils.isEmpty(outputs)) {
                    outputs.forEach(out -> {
                        output2node.put(out, nodeId);
                    });
                }
                nodeMap.put(nodeId, node);
            }

            for (GraphNodeInfo node : nodes) {
                String nodeId = node.getGraphNodeId();
                List<String> inputs = !CollectionUtils.isEmpty(node.getInputs()) ? node.getInputs() : new ArrayList<>();
                node2inputs.put(nodeId, inputs);
                for (String input : inputs) {
                    input2node.put(input, output2node.get(input));
                }
            }
        }
        node2inputs.forEach((taskId, inputs) -> {
            if (!CollectionUtils.isEmpty(inputs)) {
                dependencies.put(taskId, inputs.stream().filter(output2node::containsKey).map(output2node::get).collect(Collectors.toList()));
            }
        });
    }

    /**
     * Get graph node information by inputId
     *
     * @param inputId target inputId
     * @return graph node information
     */
    public GraphNodeInfo getNodeByInputId(String inputId) {
        if (input2node.containsKey(inputId)) {
            String nodeId = input2node.get(inputId);
            if (nodeMap.containsKey(nodeId)) {
                return nodeMap.get(nodeId);
            }
        }
        log.warn("Can't find node by input id {} input2node {}  nodeMap {}", inputId, input2node, nodeMap);
        throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_NOT_EXISTS, inputId);
    }
}
