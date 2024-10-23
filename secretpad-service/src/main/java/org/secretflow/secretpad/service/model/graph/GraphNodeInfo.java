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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Graph node information
 *
 * @author yansi
 * @date 2023/6/9
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GraphNodeInfo implements Serializable {
    /**
     * Graph code name
     */
    public String codeName;
    /**
     * Graph node id
     */
    public String graphNodeId;
    /**
     * Label column
     */
    public String label;
    /**
     * X value
     */
    public Integer x;
    /**
     * Y value
     */
    public Integer y;
    /**
     * Project graph input list
     */
    public List<String> inputs;
    /**
     * Project graph output list
     */
    public List<String> outputs;
    /**
     * Project graph nodeDef metadata model
     */
    public Object nodeDef;

    /**
     * Build a new project graph node data object via projectId, graphId and graph node information
     *
     * @param projectId target projectId
     * @param graphId   target graphId
     * @param nodeInfo  graph node information
     * @return a new project graph node data object
     */
    public static ProjectGraphNodeDO toDO(String projectId, String graphId, GraphNodeInfo nodeInfo) {
        return ProjectGraphNodeDO.builder()
                .upk(new ProjectGraphNodeDO.UPK(projectId, graphId, nodeInfo.getGraphNodeId()))
                .codeName(nodeInfo.getCodeName())
                .label(nodeInfo.getLabel())
                .x(nodeInfo.getX())
                .y(nodeInfo.getY())
                .inputs(nodeInfo.getInputs())
                .outputs(nodeInfo.getOutputs())
                .nodeDef(nodeInfo.getNodeDef())
                .build();
    }

    /**
     * Build a new graph node information via project graph node data object
     *
     * @param graphNodeDO project graph node data object
     * @return a new graph node information
     */
    public static GraphNodeInfo fromDO(ProjectGraphNodeDO graphNodeDO) {
        return GraphNodeInfo.builder()
                .codeName(graphNodeDO.getCodeName())
                .graphNodeId(graphNodeDO.getUpk().getGraphNodeId())
                .label(graphNodeDO.getLabel())
                .x(graphNodeDO.getX())
                .y(graphNodeDO.getY())
                .inputs(graphNodeDO.getInputs())
                .outputs(graphNodeDO.getOutputs())
                .nodeDef(graphNodeDO.getNodeDef())
                .build();
    }

    /**
     * Batch build graph node information list via project graph node data object list
     *
     * @param graphNodeDOS project graph node data object list
     * @return graph node information list
     */
    public static List<GraphNodeInfo> fromDOList(List<ProjectGraphNodeDO> graphNodeDOS) {
        if (!CollectionUtils.isEmpty(graphNodeDOS)) {
            return graphNodeDOS.stream().map(GraphNodeInfo::fromDO).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static List<ProjectGraphNodeDO> toDOList(String projectId, String graphId, List<GraphNodeInfo> nodeInfos) {
        if (!CollectionUtils.isEmpty(nodeInfos)) {
            return nodeInfos.stream().map(nodeInfo -> toDO(projectId, graphId, nodeInfo)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public GraphNodeInfo toGraphNodeInfo() {
        return GraphNodeInfo.builder()
                .codeName(this.codeName)
                .graphNodeId(this.graphNodeId)
                .label(this.label)
                .x(this.x)
                .y(this.y)
                .inputs(this.inputs)
                .outputs(this.outputs)
                .nodeDef(this.nodeDef)
                .build();
    }
}
