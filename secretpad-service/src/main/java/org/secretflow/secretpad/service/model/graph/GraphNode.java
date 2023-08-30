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

package org.secretflow.secretpad.service.model.graph;

import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;

import lombok.Data;

/**
 * Graph node
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
public class GraphNode {
    /**
     * Graph code name
     */
    private String codeName;
    /**
     * Graph node id
     */
    private String graphNodeId;
    /**
     * Label column
     */
    private String label;
    /**
     * X value
     */
    private Integer x;
    /**
     * Y value
     */
    private Integer y;
    /**
     * Graph node task status
     */
    private GraphNodeTaskStatus status;

    /**
     * Build project graph node data object via projectId, graphId and graph node
     *
     * @param projectId target projectId
     * @param graphId   target graphId
     * @param node      graph node
     * @return project graph node data object
     */
    public static ProjectGraphNodeDO toDO(String projectId, String graphId, GraphNode node) {
        return ProjectGraphNodeDO.builder()
                .upk(new ProjectGraphNodeDO.UPK(projectId, graphId, node.getGraphNodeId()))
                .codeName(node.getCodeName())
                .label(node.getLabel())
                .x(node.getX())
                .y(node.getY())
                .build();
    }
}
