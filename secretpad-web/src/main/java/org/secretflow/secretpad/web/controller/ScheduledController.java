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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.persistence.model.ScheduledGraphCreateRequest;
import org.secretflow.secretpad.persistence.model.ScheduledGraphOnceSuccessRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledDelRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledIdRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledInfoRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledOfflineRequest;
import org.secretflow.secretpad.service.ScheduledService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.project.PageResponse;
import org.secretflow.secretpad.service.model.project.ProjectJobSummaryVO;
import org.secretflow.secretpad.service.model.project.ProjectJobVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author yutu
 * @date 2024/08/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1alpha1/scheduled")
public class ScheduledController {
    private final ScheduledService scheduledService;

    @PostMapping("/id")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_ID)
    public SecretPadResponse<String> id(@RequestBody @Valid ScheduledIdRequest scheduledIdRequest) {
        return SecretPadResponse.success(scheduledService.buildSchedulerId(scheduledIdRequest));
    }

    @PostMapping("/graph/once/success")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_CREATE)
    public SecretPadResponse<Boolean> onceSuccess(@RequestBody @Valid ScheduledGraphOnceSuccessRequest scheduledGraphCreateRequest) {
        return SecretPadResponse.success(scheduledService.onceSuccess(scheduledGraphCreateRequest));
    }

    @PostMapping("/graph/create")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_CREATE)
    public SecretPadResponse<Void> create(@RequestBody @Valid ScheduledGraphCreateRequest scheduledGraphCreateRequest) {
        scheduledService.createScheduler(scheduledGraphCreateRequest);
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/page", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<SecretPadPageResponse<PageScheduledVO>> page(@Valid @RequestBody PageScheduledRequest request) {
        return SecretPadResponse.success(scheduledService.queryPage(request, request.of()));
    }

    @PostMapping(value = "/offline", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_OFFLINE)
    public SecretPadResponse<Void> offline(@Valid @RequestBody ScheduledOfflineRequest request) {
        scheduledService.offline(request);
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/del", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_OFFLINE)
    public SecretPadResponse<Void> del(@Valid @RequestBody ScheduledDelRequest request) {
        scheduledService.del(request);
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/info", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<ProjectJobVO> info(@Valid @RequestBody ScheduledInfoRequest request) {
        return SecretPadResponse.success(scheduledService.info(request));
    }


    @PostMapping(value = "/task/page", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<SecretPadPageResponse<TaskPageScheduledVO>> taskPage(@Valid @RequestBody TaskPageScheduledRequest request) {
        return SecretPadResponse.success(scheduledService.taskPage(request, request.of()));
    }


    @PostMapping(value = "/task/stop", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<Void> taskStop(@Valid @RequestBody TaskStopScheduledRequest request) {
        scheduledService.taskStop(request);
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/task/rerun", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<Void> taskRerun(@Valid @RequestBody TaskReRunScheduledRequest request) {
        scheduledService.taskRerun(request);
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/task/info", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<ProjectJobVO> taskInfo(@Valid @RequestBody TaskInfoScheduledRequest request) {
        return SecretPadResponse.success(scheduledService.taskInfo(request));
    }

    @ResponseBody
    @PostMapping(value = "/job/list")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.SCHEDULED_PAGE)
    public SecretPadResponse<PageResponse<ProjectJobSummaryVO>> listJob(@Valid @RequestBody ScheduleListProjectJobRequest request) {
        return SecretPadResponse.success(scheduledService.listProjectJob(request));
    }

}