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

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;

/**
 * Project node data object
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
@Table(name = "project_node")
@SQLDelete(sql = "update project_node set is_deleted = 1 where node_id = ? and project_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectNodeDO extends BaseAggregationRoot<ProjectNodeDO> {

    /**
     * Project node unique primary key
     */
    @EmbeddedId
    private ProjectNodeDO.UPK upk;

    public static class Factory {
        /**
         * Create a new project node DO via projectId and nodeId
         *
         * @param projectId project id
         * @param nodeId    node id
         * @return a new project node DO
         */
        public static ProjectNodeDO newProjectNode(String projectId, String nodeId) {
            return ProjectNodeDO.builder().upk(new UPK(projectId, nodeId)).build();
        }
    }

    /**
     * Project node unique primary key
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
         * Node id
         */
        @Column(name = "node_id", nullable = false, length = 64)
        private String nodeId;
    }

}
