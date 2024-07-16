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

import org.secretflow.secretpad.common.util.DateTimes;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Project job task data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
@Setter
@Entity
@Table(name = "project_job_task_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ProjectJobTaskLogDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 291568296509217011L;
    /**
     * Project job task start time
     */
    @CreatedDate
    @Column(name = "gmt_create", nullable = false, insertable = false, updatable = false)
    LocalDateTime gmtCreate;
    /**
     * The id of the database is automatically added
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, insertable = false, updatable = false)
    private Long id;
    /**
     * Project id
     */
    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;
    /**
     * Project job id
     */
    @Column(name = "job_id", nullable = false, length = 64)
    private String jobId;
    /**
     * Project job task id
     */
    @Column(name = "task_id", nullable = false, length = 64)
    private String taskId;
    /**
     * Project job task log
     */
    @Column(name = "log", nullable = false, length = 64)
    private String log;

    /**
     * Build project job task log via content string
     *
     * @param content
     * @return log string
     */
    public static String makeLog(String content) {
        return String.format("%s %s %s", DateTimes.localDateTimeString(LocalDateTime.now()), "INFO", content);
    }

    /**
     * read_data/datatable log
     *
     * @param content dateTime
     * @return log string
     */
    public static String makeLog(LocalDateTime dateTime, String content) {
        return String.format("%s %s %s", DateTimes.localDateTimeString(dateTime), "INFO", content);
    }

    /**
     * Build project job task log when task starts
     *
     * @param task
     * @return project job task log DO class
     */
    public static ProjectJobTaskLogDO taskStartLog(ProjectTaskDO task) {
        return ProjectJobTaskLogDO.builder()
                .projectId(task.getUpk().getProjectId())
                .jobId(task.getUpk().getJobId())
                .taskId(task.getUpk().getTaskId())
                .log(ProjectJobTaskLogDO.makeLog(String.format("the jobId=%s, taskId=%s start ...", task.getUpk().getJobId(), task.getUpk().getTaskId())))
                .build();
    }

    /**
     * Build project job task log if task is success
     *
     * @param task
     * @return project job task log DO class
     */
    public static ProjectJobTaskLogDO taskSucceedLog(ProjectTaskDO task) {
        return ProjectJobTaskLogDO.builder()
                .projectId(task.getUpk().getProjectId())
                .jobId(task.getUpk().getJobId())
                .taskId(task.getUpk().getTaskId())
                .log(ProjectJobTaskLogDO.makeLog(String.format("the jobId=%s, taskId=%s succeed", task.getUpk().getJobId(), task.getUpk().getTaskId())))
                .build();
    }

    /**
     * Build project job task log if task is stopped
     *
     * @param task
     * @return project job task log DO class
     */
    public static ProjectJobTaskLogDO taskStoppedLog(ProjectTaskDO task) {
        return ProjectJobTaskLogDO.builder()
                .projectId(task.getUpk().getProjectId())
                .jobId(task.getUpk().getJobId())
                .taskId(task.getUpk().getTaskId())
                .log(ProjectJobTaskLogDO.makeLog(String.format("the jobId=%s, taskId=%s stopped", task.getUpk().getJobId(), task.getUpk().getTaskId())))
                .build();
    }

    /**
     * Build project job task log if task is failed
     *
     * @param task
     * @return project job task log DO class
     */
    public static ProjectJobTaskLogDO taskFailedLog(ProjectTaskDO task, String errMsg) {
        return ProjectJobTaskLogDO.builder()
                .projectId(task.getUpk().getProjectId())
                .jobId(task.getUpk().getJobId())
                .taskId(task.getUpk().getTaskId())
                .log(ProjectJobTaskLogDO.makeLog(String.format("the jobId=%s, taskId=%s failed: %s",
                        task.getUpk().getJobId(), task.getUpk().getTaskId(), errMsg)))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProjectJobTaskLogDO that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(projectId, that.projectId) && Objects.equals(jobId, that.jobId) && Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, jobId, taskId);
    }
}
