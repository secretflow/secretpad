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

package org.secretflow.secretpad.persistence.entity;

import org.secretflow.secretpad.persistence.converter.Object2JsonStrConverter;
import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Project graph node data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "project_graph_node")
public class ProjectGraphNodeDO extends BaseAggregationRoot<ProjectGraphNodeDO> {
    /**
     * Project graph node unique primary key
     */
    @EmbeddedId
    private ProjectGraphNodeDO.UPK upk;

    /**
     * Project graph code name
     */
    @Column(name = "code_name")
    private String codeName;

    /**
     * label column
     */
    @Column(name = "label")
    private String label;

    /**
     * x value
     */
    @Column(name = "x")
    private Integer x;

    /**
     * y value
     */
    @Column(name = "y")
    private Integer y;

    /**
     * Project graph input list
     */
    @Column(name = "inputs")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> inputs;

    /**
     * Project graph output list
     */
    @Column(name = "outputs")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> outputs;

    /**
     * Project graph nodeDef metadata model
     */
    @Column(name = "node_def")
    @Convert(converter = Object2JsonStrConverter.class)
    private Object nodeDef;

    /**
     * Project graph node unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UPK implements Serializable {
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId;
        /**
         * Graph id
         */
        @Column(name = "graph_id", nullable = false, length = 64)
        private String graphId;
        /**
         * Graph node id
         */
        @Column(name = "graph_node_id", nullable = false, length = 64)
        private String graphNodeId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            ProjectGraphNodeDO.UPK that = (ProjectGraphNodeDO.UPK) o;
            return this.projectId.equals(that.projectId)
                    && this.graphId.equals(that.graphId)
                    && this.graphNodeId.equals(that.getGraphNodeId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, graphId, graphId);
        }
    }
}
