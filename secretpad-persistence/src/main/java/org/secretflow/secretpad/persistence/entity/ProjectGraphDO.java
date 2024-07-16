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

import org.secretflow.secretpad.persistence.converter.GraphEdgesConverter;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Project graph data object
 *
 * @author jiezi
 * @date 2023/5/25
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
@Table(name = "project_graph")
public class ProjectGraphDO extends BaseAggregationRoot<ProjectGraphDO> {
    /**
     * Project graph unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Project graph name
     */
    @Column(name = "name")
    private String name;

    /**
     * Project graph edge list
     */
    @Column(name = "edges")
    @Convert(converter = GraphEdgesConverter.class)
    private List<GraphEdgeDO> edges;

    /**
     * Project graph node DO list
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns(
            value = {
                    @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false, updatable = false, insertable = false),
                    @JoinColumn(name = "graph_id", referencedColumnName = "graph_id", nullable = false, updatable = false, insertable = false)
            }
    )
    private List<ProjectGraphNodeDO> nodes;

    /**
     * project owner
     */
    @Column(name = "owner_id", nullable = false, length = 64)
    private String ownerId;

    @Column(name = "node_max_index", nullable = false)
    private Integer nodeMaxIndex;

    @Column(name = "max_parallelism", nullable = false)
    private Integer maxParallelism;

    public void setNodes(List<ProjectGraphNodeDO> nodes) {
        if (this.nodes == null) {
            this.nodes = new ArrayList<>();
        }
        this.nodes.clear();
        if (ObjectUtils.isNotEmpty(nodes)) {
            this.nodes.addAll(nodes);
        }
    }

    /**
     * Project graph unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 5005877919773504643L;
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


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            UPK that = (UPK) o;
            return this.projectId.equals(that.projectId)
                    && this.graphId.equals(that.graphId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, graphId);
        }

    }

    @Override
    public String getProjectId() {
        return this.upk.projectId;
    }
}
