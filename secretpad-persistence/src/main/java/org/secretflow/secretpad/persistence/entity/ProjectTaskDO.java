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

import org.secretflow.secretpad.persistence.converter.BaseObjectJsonConverter;
import org.secretflow.secretpad.persistence.converter.BaseObjectListJsonConverter;
import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Project task data object
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
@Table(name = "project_job_task")
@SQLDelete(sql = "update project_job_task set is_deleted = 1 where job_id = ? and project_id = ? and task_id = ?")
@Where(clause = "is_deleted = 0")
@ToString
public class ProjectTaskDO extends BaseAggregationRoot {

    /**
     * Project task unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Initiator and participants
     * Every party is json string
     */
    @Column(name = "parties")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> parties;

    /**
     * Graph node task status
     * When created, it is INITIALIZED.
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private GraphNodeTaskStatus status = GraphNodeTaskStatus.INITIALIZED;

    /**
     * Error message when graph node task is failed
     */
    @Column(name = "err_msg", nullable = true)
    private String errMsg;

    /**
     * Graph node id
     */
    @Column(name = "graph_node_id", nullable = true)
    private String graphNodeId;

    /**
     * Project graph node DO information
     */
    @Column(name = "graph_node", nullable = true)
    @Convert(converter = GraphNodeConverter.class)
    private ProjectGraphNodeDO graphNode;

    /**
     * task extra info
     */
    @Column(name = "extra_info", nullable = true)
    @Convert(converter = ExtraInfoConverter.class)
    private ExtraInfo extraInfo;


    /** prevent npe */
    public ExtraInfo getExtraInfo() {
        if (this.extraInfo == null) {
            this.extraInfo = new ExtraInfo();
        }
        return this.extraInfo;
    }


    @Override
    public String getProjectId() {
        return this.upk.projectId;
    }

    @Override
    public List<String> getNodeIds() {
        return null;
    }

    /**
     * Whether the graph job status is finished
     *
     * @return whether finished
     */
    public boolean isFinished() {
        return this.status == GraphNodeTaskStatus.SUCCEED || this.status == GraphNodeTaskStatus.FAILED || this.status == GraphNodeTaskStatus.STOPPED;
    }

    /**
     * Project task unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId;
        /**
         * Job id
         */
        @Column(name = "job_id", nullable = false, length = 64)
        private String jobId;
        /**
         * Task id
         */
        @Column(name = "task_id", nullable = false, length = 64)
        private String taskId;
    }

    @Converter
    public static class GraphNodeConverter extends BaseObjectJsonConverter<ProjectGraphNodeDO> {
        public GraphNodeConverter() {
            super(ProjectGraphNodeDO.class);
        }
    }


    @Converter
    public static class ExtraInfoConverter extends BaseObjectJsonConverter<ExtraInfo> {
        public ExtraInfoConverter() {
            super(ExtraInfo.class);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtraInfo implements Serializable{
        /** task progress */
        private static final long serialVersionUID = -941002226318258211L;
        private Float progress;
    }

    /**
     * Every party class, initiator or participant
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class Party {
        /**
         * Domain id, same as node id
         */
        private String domainId;
        /**
         * Role
         */
        private String role;

        /**
         * Create a new party
         *
         * @param domainId
         * @param role
         * @return a new party
         */
        public static Party newParty(String domainId, String role) {
            Party party = new Party();
            party.domainId = domainId;
            party.role = role;
            return party;
        }
    }

    /**
     * Party status class
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PartyStatus extends Party {
        /**
         * Domain id, same as node id
         */
        private String domainId;
        /**
         * Role
         */
        private String role;
        /**
         * Status
         */
        private String status;
        /**
         * Error message
         */
        private String errMsg;

        public Party party() {
            return newParty(getDomainId(), getRole());
        }
    }

    @Converter
    public static class PartyStatusesConverter extends BaseObjectListJsonConverter<PartyStatus> {
        public PartyStatusesConverter() {
            super(PartyStatus.class);
        }
    }
}