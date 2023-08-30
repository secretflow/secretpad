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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Project job summary view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectJobSummaryVO extends ProjectJobBaseVO {
    /**
     * The count of datatable
     */
    @Schema(description = "the count of datatable")
    private Long tableCount;

    /**
     * The count of model
     */
    @Schema(description = "the count of model")
    private Long modelCount;

    /**
     * The count of rule
     */
    @Schema(description = "the count of rule")
    private Long ruleCount;

    /**
     * The count of report
     */
    @Schema(description = "the count of report")
    private Long reportCount;

    /**
     * The count of completed subtasks
     */
    @Schema(description = "the count of completed subtasks")
    private Long finishedTaskCount;

    /**
     * The count of total subtasks
     */
    @Schema(description = "the count of total subtasks")
    private Long taskCount;

    /**
     * Finish time
     */
    @Schema(description = "finish time")
    private String gmtFinished;

    ProjectJobSummaryVO(ProjectJobDO jobDO, Long tableCount, Long modelCount, Long ruleCount, Long reportCount,
                        Long finishedTaskCount, Long taskCount) {
        super(jobDO);
        this.tableCount = tableCount;
        this.modelCount = modelCount;
        this.ruleCount = ruleCount;
        this.reportCount = reportCount;
        this.finishedTaskCount = finishedTaskCount;
        this.taskCount = taskCount;
        if (jobDO.getFinishedTime() != null) {
            this.gmtFinished = DateTimes.toRfc3339(jobDO.getFinishedTime());
        }
    }

    /**
     * Build a new project job summary view object via params
     *
     * @param jobDO             job data object
     * @param tableCount        the count of datatable
     * @param modelCount        the count of model
     * @param ruleCount         the count of rule
     * @param reportCount       the count of report
     * @param finishedTaskCount the count of completed subtasks
     * @param taskCount         the count of total subtasks
     * @return a new project job summary view object
     */
    public static ProjectJobSummaryVO of(ProjectJobDO jobDO, Long tableCount, Long modelCount, Long ruleCount, Long reportCount,
                                         Long finishedTaskCount, Long taskCount) {
        return new ProjectJobSummaryVO(jobDO, tableCount, modelCount, ruleCount, reportCount, finishedTaskCount, taskCount);
    }
}
