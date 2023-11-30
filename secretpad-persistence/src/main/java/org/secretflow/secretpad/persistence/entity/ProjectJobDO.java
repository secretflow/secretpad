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
import org.secretflow.secretpad.persistence.converter.SqliteLocalDateTimeConverter;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Project job data object
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
@Table(name = "project_job")
@SQLDelete(sql = "update project_job set is_deleted = 1 where job_id = ? and project_id = ?")
@Where(clause = "is_deleted = 0")
@NamedEntityGraphs(
        @NamedEntityGraph(
                name = "project_job.all_task",
                attributeNodes = {
                        @NamedAttributeNode(value = "tasks")
                }
        )
)
public class ProjectJobDO extends BaseAggregationRoot<ProjectJobDO> {

    /**
     * Project job unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Project job name
     */
    @Column(name = "name", nullable = false, length = 40)
    private String name;

    /**
     * Map of task id and project task DO class
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumns({@JoinColumn(name = "project_id", referencedColumnName = "project_id"),
            @JoinColumn(name = "job_id", referencedColumnName = "job_id")})
    @MapKeyColumn(name = "task_id")
    private Map<String, ProjectTaskDO> tasks;

    /**
     * Project graph job status
     * When created, it must be running.
     */
    @Column(name = "status", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private GraphJobStatus status = GraphJobStatus.RUNNING;

    /**
     * Project job finish time
     * NOTE: this time is UTC time
     */
    @Column(name = "finished_time", nullable = true)
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime finishedTime;

    /**
     * Project job error message
     */
    @Column(name = "err_msg", nullable = true)
    private String errMsg;

    /**
     * Project graph id
     */
    @Column(name = "graph_id", nullable = true)
    private String graphId;

    /**
     * Project graph edge list
     */
    @Column(name = "edges", nullable = true)
    @Convert(converter = GraphEdgesConverter.class)
    private List<GraphEdgeDO> edges;

    /**
     * Whether the graph job status is finished
     *
     * @return whether finished
     */
    public boolean isFinished() {
        return this.status == GraphJobStatus.SUCCEED || this.status == GraphJobStatus.FAILED || this.status == GraphJobStatus.STOPPED;
    }

    /**
     * Stop the graph job and associated tasks
     */
    public void stop() {
        this.setStatus(GraphJobStatus.STOPPED);
        this.getTasks().forEach((k, v) -> {
                    switch (v.getStatus()) {
                        case RUNNING:
                        case INITIALIZED:
                            registerEvent(TaskStatusTransformEvent.of(this, k, v.getStatus(), GraphNodeTaskStatus.STOPPED, null));
                            v.setStatus(GraphNodeTaskStatus.STOPPED);
                            return;
                        default:
                            // do nothing
                    }
                }
        );
    }

    /**
     * Transform graph node task status to graph job status
     *
     * @param taskId
     * @param currentStatus
     * @param reason
     */
    public void transformTaskStatus(@Nonnull String taskId, @Nonnull GraphNodeTaskStatus currentStatus, @Nullable List<String> reason) {
        if (this.getTasks().containsKey(taskId)) {
            ProjectTaskDO task = this.getTasks().get(taskId);
            registerEvent(TaskStatusTransformEvent.of(this, taskId, task.getStatus(), currentStatus, reason));
            this.getTasks().get(taskId).setStatus(currentStatus);
        }
    }

    /**
     * Project job unique primary key
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
         * Job id
         */
        @Column(name = "job_id", nullable = false, length = 64)
        private String jobId;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            ProjectJobDO.UPK that = (ProjectJobDO.UPK) o;
            return this.projectId.equals(that.projectId)
                    && this.jobId.equals(that.jobId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, jobId);
        }
    }

    /**
     * Abstract event class
     */
    @AllArgsConstructor
    @Getter
    public static abstract class AbstractEvent {
        @JsonIgnore
        private ProjectJobDO source;

        /**
         * Get projectId from source, avoid pmd error
         *
         * @param source target ProjectJobDO
         * @return projectId
         */
        public abstract String getProjectId(ProjectJobDO source);
    }

    /**
     * Transform task status event class
     */
    @Getter
    public static class TaskStatusTransformEvent extends AbstractEvent {
        /**
         * Task id
         */
        private String taskId;
        /**
         * Graph node task status of source
         */
        private GraphNodeTaskStatus fromStatus;
        /**
         * Graph node task status of destination
         */
        private GraphNodeTaskStatus toStatus;
        /**
         * Task failed reasons from apiLite
         */
        private List<String> reasons;

        private TaskStatusTransformEvent(ProjectJobDO source) {
            super(source);
        }

        /**
         * Transform task status from graph node task status of source to graph node task status of destination
         * Fill failed reasons if graph node task status of source is failed
         *
         * @param source
         * @param taskId
         * @param fromStatus
         * @param toStatus
         * @param reasons
         * @return transform task status event
         */
        public static TaskStatusTransformEvent of(ProjectJobDO source, String taskId, GraphNodeTaskStatus fromStatus,
                                                  GraphNodeTaskStatus toStatus, List<String> reasons) {
            TaskStatusTransformEvent event = new TaskStatusTransformEvent(source);
            event.taskId = taskId;
            event.fromStatus = fromStatus;
            event.toStatus = toStatus;
            event.reasons = reasons;
            return event;
        }

        @Override
        public String getProjectId(ProjectJobDO source) {
            return source.getUpk().getProjectId();
        }
    }

    @Override
    public String getProjectId() {
        return this.upk.projectId;
    }
}
