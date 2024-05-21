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

import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Graph detail view object
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphDetailVO {
    /**
     * Project id
     */
    private String projectId;
    /**
     * Graph id
     */
    private String graphId;
    /**
     * Graph name
     */
    private String name;
    /**
     * Graph node detail list
     */
    private List<GraphNodeDetail> nodes;
    /**
     * Graph edge list
     */
    private List<GraphEdge> edges;

    /**
     * Graph max parallelism
     */
    private Integer maxParallelism;

    /**
     * Build a new graph detail view object from project graph data object and graph node status view object list
     *
     * @param graphDO    project graph data object
     * @param nodeStatus graph node status view object list
     * @return a new graph detail view object
     */
    public static GraphDetailVO fromDO(ProjectGraphDO graphDO, List<GraphNodeStatusVO> nodeStatus) {
        String graphId = graphDO.getUpk().getGraphId();
        return GraphDetailVO.builder()
                .projectId(graphDO.getUpk().getProjectId())
                .graphId(graphId)
                .name(graphDO.getName())
                .nodes(GraphNodeDetail.fromDOList(graphDO.getNodes(), nodeStatus))
                .edges(GraphEdge.fromDOList(graphDO.getEdges()))
                .maxParallelism(graphDO.getMaxParallelism())
                .build();
    }
}
