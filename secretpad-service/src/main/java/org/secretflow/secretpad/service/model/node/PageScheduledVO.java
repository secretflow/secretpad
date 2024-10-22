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
import org.secretflow.secretpad.persistence.entity.ProjectScheduleDO;

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
public class PageScheduledVO {

    private String scheduleId;
    private String scheduleDesc;
    private String scheduleStats;
    private String creator;
    private String createTime;
    private boolean taskRunning;
    private String owner;
    private String ownerName;


    public static PageScheduledVO from(ProjectScheduleDO projectScheduleDO) {
        return PageScheduledVO.builder()
                .scheduleId(projectScheduleDO.getScheduleId())
                .scheduleDesc(projectScheduleDO.getScheduleDesc())
                .scheduleStats(projectScheduleDO.getStatus().name())
                .creator(projectScheduleDO.getCreator())
                .createTime(DateTimes.toLocalDateTimeString(projectScheduleDO.getCreateTime()))
                .owner(projectScheduleDO.getOwner())
                .build();
    }
}