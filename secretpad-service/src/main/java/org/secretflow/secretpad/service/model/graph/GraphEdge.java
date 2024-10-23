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

import org.secretflow.secretpad.persistence.model.GraphEdgeDO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Graph edge
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GraphEdge implements Serializable {
    /**
     * Edge id
     */
    private String edgeId;
    /**
     * Graph edge source attribute
     */
    private String source;
    /**
     * Graph edge sourceAnchor attribute
     */
    private String sourceAnchor;
    /**
     * Graph edge target attribute
     */
    private String target;
    /**
     * Graph edge targetAnchor attribute
     */
    private String targetAnchor;

    /**
     * Build a new graph edge from graph edge data object
     *
     * @param edge graph edge data object
     * @return a new graph edge
     */
    public static GraphEdge fromDO(GraphEdgeDO edge) {
        return new GraphEdge(
                edge.getEdgeId(),
                edge.getSource(),
                edge.getSourceAnchor(),
                edge.getTarget(),
                edge.getTargetAnchor());
    }

    /**
     * Batch build graph edge list from graph edge data object list
     *
     * @param edgeDOList graph edge data object list
     * @return graph edge list
     */
    public static List<GraphEdge> fromDOList(List<GraphEdgeDO> edgeDOList) {
        if (!CollectionUtils.isEmpty(edgeDOList)) {
            return edgeDOList.stream().map(GraphEdge::fromDO).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Build a new graph edge data object from graph edge
     *
     * @param edge graph edge
     * @return a new graph edge data object
     */
    public static GraphEdgeDO toDO(GraphEdge edge) {
        return new GraphEdgeDO(
                edge.getEdgeId(),
                edge.getSource(),
                edge.getSourceAnchor(),
                edge.getTarget(),
                edge.getTargetAnchor());
    }

    /**
     * Batch build graph edge data object list from graph edge list
     *
     * @param edges graph edge list
     * @return graph edge data object list
     */
    public static List<GraphEdgeDO> toDOList(List<GraphEdge> edges) {
        if (!CollectionUtils.isEmpty(edges)) {
            return edges.stream().map(GraphEdge::toDO).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
