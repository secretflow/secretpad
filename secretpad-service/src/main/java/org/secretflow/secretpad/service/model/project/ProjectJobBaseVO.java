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
import org.secretflow.secretpad.persistence.model.GraphJobStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project job basic view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProjectJobBaseVO {

    /**
     * Job id
     */
    @Schema(description = "job id")
    private String jobId;


    /**
     * Job status
     */
    @Schema(description = "job status")
    private GraphJobStatus status;

    /**
     * Job error message
     */
    @Schema(description = "job error message")
    private String errMsg;

    /**
     * Job start time
     */
    @Schema(description = "job start time")
    private String gmtCreate;

    /**
     * Job update time
     */
    @Schema(description = "job update time")
    private String gmtModified;

    /**
     * Job finish time
     */
    @Schema(description = "job finish time")
    private String gmtFinished;

    public ProjectJobBaseVO(ProjectJobDO jobDO) {
        this.jobId = jobDO.getUpk().getJobId();
        this.status = jobDO.getStatus();
        this.errMsg = jobDO.getErrMsg();
        this.gmtCreate = DateTimes.toRfc3339(jobDO.getGmtCreate());
        this.gmtModified = DateTimes.toRfc3339(jobDO.getGmtModified());
        if (jobDO.getFinishedTime() != null) {
            this.gmtFinished = DateTimes.toRfc3339(jobDO.getFinishedTime());
        }
    }

}
