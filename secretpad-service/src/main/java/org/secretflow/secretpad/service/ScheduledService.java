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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectScheduleJobDO;
import org.secretflow.secretpad.persistence.model.ScheduledGraphCreateRequest;
import org.secretflow.secretpad.persistence.model.ScheduledGraphOnceSuccessRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledDelRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledIdRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledInfoRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledOfflineRequest;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.project.PageResponse;
import org.secretflow.secretpad.service.model.project.ProjectJobSummaryVO;
import org.secretflow.secretpad.service.model.project.ProjectJobVO;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;

/**
 * @author yutu
 * @date 2024/08/26
 */
public interface ScheduledService {

    /**
     * build scheduler id
     */
    String buildSchedulerId(ScheduledIdRequest scheduledIdRequest);


    /**
     * create scheduler
     */
    void createScheduler(ScheduledGraphCreateRequest scheduledGraphCreateRequest);

    /**
     * convert to project schedule job do
     */
    ProjectScheduleJobDO convertToProjectScheduleJobDO(ProjectJobDO projectJobDO);

    /**
     * query page
     */
    SecretPadPageResponse<PageScheduledVO> queryPage(PageScheduledRequest request, Pageable pageable);

    /**
     * offline
     */
    void offline(ScheduledOfflineRequest request);

    /**
     * del
     */
    void del(ScheduledDelRequest request);

    /**
     * info
     */
    ProjectJobVO info(ScheduledInfoRequest request);

    /**
     * task page
     */
    SecretPadPageResponse<TaskPageScheduledVO> taskPage(TaskPageScheduledRequest request, Pageable of);

    /**
     * task stop
     */
    void taskStop(TaskStopScheduledRequest request);

    /**
     * task rerun
     */
    void taskRerun(TaskReRunScheduledRequest request);

    /**
     * task info
     */
    ProjectJobVO taskInfo(@Valid TaskInfoScheduledRequest request);

    /**
     * once success
     */
    boolean onceSuccess(@Valid ScheduledGraphOnceSuccessRequest scheduledGraphCreateRequest);

    /**
     * list project job
     */
    PageResponse<ProjectJobSummaryVO> listProjectJob(@Valid ScheduleListProjectJobRequest request);
}