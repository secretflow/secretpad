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

package org.secretflow.secretpad.service.model.node;

import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.persistence.entity.ProjectScheduleTaskDO;

import lombok.*;

/**
 * @author yutu
 * @date 2023/08/03
 */
@Builder
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TaskPageScheduledVO {

    private String scheduleTaskId;
    private String scheduleTaskExpectStartTime;
    private String scheduleTaskStartTime;
    private String scheduleTaskEndTime;
    private String scheduleTaskStatus;
    private Boolean allReRun;


    public static TaskPageScheduledVO from(ProjectScheduleTaskDO projectScheduleTaskDO) {
        return TaskPageScheduledVO.builder()
                .scheduleTaskId(projectScheduleTaskDO.getScheduleTaskId())
                .scheduleTaskExpectStartTime(DateTimes.toLocalDateTimeString(projectScheduleTaskDO.getScheduleTaskExpectStartTime()))
                .scheduleTaskStartTime(DateTimes.toLocalDateTimeString(projectScheduleTaskDO.getScheduleTaskStartTime()))
                .scheduleTaskEndTime(DateTimes.toLocalDateTimeString(projectScheduleTaskDO.getScheduleTaskEndTime()))
                .scheduleTaskStatus(projectScheduleTaskDO.getStatus().name())
                .allReRun(projectScheduleTaskDO.getAllReRun())
                .build();
    }
}