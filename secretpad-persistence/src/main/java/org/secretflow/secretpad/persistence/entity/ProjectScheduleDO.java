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
import org.secretflow.secretpad.persistence.converter.*;
import org.secretflow.secretpad.persistence.model.ScheduledGraphCreateRequest;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Project schedule entity
 *
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
@Table(name = "project_schedule")
@SQLDelete(sql = "update project_schedule set is_deleted = 1 where schedule_id = ?")
@SQLRestriction("is_deleted = 0")
public class ProjectScheduleDO extends BaseAggregationRoot<ProjectScheduleDO> {

    @Id
    private String scheduleId;

    private String scheduleDesc;

    @Convert(converter = StringListJsonConverter.class)
    private List<String> cron;

    @Convert(converter = ScheduledGraphCreateRequestConverter.class)
    private ScheduledGraphCreateRequest request;

    @Convert(converter = ProjectGraphInfoConverter.class)
    private ProjectGraphDO graphInfo;

    private String graphJobId;

    @Convert(converter = ProjectJobInfoConverter.class)
    private ProjectJobDO jobInfo;

    private String owner;

    private String creator;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private ScheduledStatus status = ScheduledStatus.UP;

    private String projectId;

    private String graphId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = SqliteLocalDateTimeConverter.class)
    private LocalDateTime createTime;

}