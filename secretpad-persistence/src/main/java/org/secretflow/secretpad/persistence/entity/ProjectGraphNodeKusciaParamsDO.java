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

package org.secretflow.secretpad.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author yutu
 * @date 2024/01/29
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Where(clause = "is_deleted = 0")
@Table(name = "project_graph_node_kuscia_params")
public class ProjectGraphNodeKusciaParamsDO extends BaseAggregationRoot<ProjectGraphNodeKusciaParamsDO> {
    /**
     * Project graph graph_node unique primary key
     */
    @EmbeddedId
    private ProjectGraphNodeKusciaParamsDO.UPK upk;
    /**
     * Project graph job_id
     */
    @Column(name = "job_id")
    private String jobId;

    /**
     * Project graph task_id
     */
    @Column(name = "task_id")
    private String taskId;

    /**
     * Project graph inputs
     */
    @Column(name = "inputs")
    private String inputs;

    /**
     * Project graph outputs
     */
    @Column(name = "outputs")
    private String outputs;

    /**
     * node eval param
     */
    @Column(name = "node_eval_param")
    private String nodeEvalParam;


    /**
     * Project graph graph_node unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Builder
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

        /**
         * graph node id
         */
        @Column(name = "graph_node_id", nullable = false, length = 64)
        private String graphNodeId;
    }

    @Override
    public String getProjectId() {
        return this.upk.projectId;
    }
}