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
import org.secretflow.secretpad.service.ICloudLogService;
import org.secretflow.secretpad.service.model.CloudGraphNodeTaskLogsVO;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.graph.GraphNodeCloudLogsRequest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author chenmingliang
 * @date 2024/04/18
 */
@RestController
@RequestMapping("/api/v1alpha1/cloud_log")
public class CloudLogController {

    @Autowired
    private ApplicationContext applicationContext;


    /**
     * Query graph node logs api
     *
     * @param graphNodeLogsRequest query graph node cloud logs request
     * @return successful SecretPadResponse with graph node task logs view object
     */
    @Operation(summary = "graph node cloud logs,such as sls,elk,etc.")
    @PostMapping("/sls")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.GRAPH_NODE_LOGS)
    public SecretPadResponse<CloudGraphNodeTaskLogsVO> getCloudLog(@Valid @RequestBody GraphNodeCloudLogsRequest graphNodeLogsRequest) {
        ObjectProvider<ICloudLogService> provider = applicationContext.getBeanProvider(ICloudLogService.class);
        ICloudLogService cloudLogService = provider.getIfAvailable(() -> null);
        if (Objects.isNull(cloudLogService)) {
            return SecretPadResponse.success(CloudGraphNodeTaskLogsVO.buildUnReadyResult());
        }
        return SecretPadResponse.success(cloudLogService.fetchLog(graphNodeLogsRequest));
    }
}
