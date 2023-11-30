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

import org.secretflow.secretpad.persistence.model.ResultKind;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serializable;

/**
 * Project result data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "project_result")
@SQLDelete(sql = "update project_result set is_deleted = 1 where kind = ? and node_id=? and project_id = ? and  ref_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectResultDO extends BaseAggregationRoot<ProjectResultDO> {
    /**
     * Project result unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Job id
     */
    @Column(name = "job_id", nullable = true)
    private String jobId;

    /**
     * Task id
     */
    @Column(name = "task_id", nullable = true)
    private String taskId;

    /**
     * Project result unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class UPK implements Serializable {
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId;
        /**
         * Result kind
         * FedTable,Model,Rule,Report
         */
        @Column(name = "kind", nullable = false, length = 16)
        @Enumerated(EnumType.STRING)
        private ResultKind kind;
        /**
         * Node id
         */
        @Column(name = "node_id", nullable = false, length = 64)
        private String nodeId;
        /**
         * Ref id, domain data id in domain data proto
         */
        @Column(name = "ref_id", nullable = false, length = 64)
        private String refId;
    }

    @Override
    public String getProjectId() {
        return this.upk.projectId;
    }

    @Override
    public String getNodeId() {
        return this.upk.nodeId;
    }
}