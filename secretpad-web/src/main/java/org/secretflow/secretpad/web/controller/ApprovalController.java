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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.service.ApprovalService;
import org.secretflow.secretpad.service.model.approval.CreateApprovalRequest;
import org.secretflow.secretpad.service.model.approval.PullStatusRequest;
import org.secretflow.secretpad.service.model.approval.PullStatusVO;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ApprovalController.
 *
 * @author cml
 * @date 2023/09/19
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    public ApprovalController(@Autowired ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * create approval
     *
     * @param createApprovalRequest
     * @return
     */
    @PostMapping(value = "/create", consumes = "application/json")
    @DataResource(field = "nodeID", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<Object> create(@Valid @RequestBody CreateApprovalRequest createApprovalRequest) {
        approvalService.createApproval(createApprovalRequest.getNodeID(), createApprovalRequest.getVoteConfig(), createApprovalRequest.getVoteType());
        return SecretPadResponse.success();
    }

    /**
     * get the tee_down_load vote status in pipeline
     *
     * @param pullStatusRequest
     * @return
     */
    @PostMapping(value = "/pull/status", consumes = "application/json")
    public SecretPadResponse<PullStatusVO> pullStatus(@Valid @RequestBody PullStatusRequest pullStatusRequest) {
        PullStatusVO pullStatusVO = approvalService.pullStatus(pullStatusRequest.getProjectID(), pullStatusRequest.getJobID(), pullStatusRequest.getTaskID(), pullStatusRequest.getResourceID(), pullStatusRequest.getResourceType());
        return SecretPadResponse.success(pullStatusVO);
    }
}
