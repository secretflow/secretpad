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

import org.secretflow.secretpad.common.enums.ScheduledStatus;
import org.secretflow.secretpad.persistence.converter.Boolean2IntConverter;
import org.secretflow.secretpad.persistence.converter.SqliteLocalDateTimeConverter;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * @author yutu
 * @date 2024/08/27
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "project_schedule_task")
@SQLDelete(sql = "update project_schedule_task set is_deleted = 1 where schedule_task_id = ?")
@SQLRestriction("is_deleted = 0")
public class ProjectScheduleTaskDO extends BaseAggregationRoot<ProjectScheduleTaskDO> {


    private String projectId;

    private String graphId;

    private String scheduleJobId;

    private String scheduleId;

    @Id
    private String scheduleTaskId;

    private String cron;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime scheduleTaskExpectStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime scheduleTaskStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime scheduleTaskEndTime;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private ScheduledStatus status = ScheduledStatus.TO_BE_RUN;

    private String owner;

    private String creator;

    private String jobRequest;

    @Convert(converter = Boolean2IntConverter.class)
    private Boolean allReRun = false;
}