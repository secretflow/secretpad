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

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.common.util.RestTemplateUtil;
import org.secretflow.secretpad.service.model.auth.ResetNodeUserPwdRequest;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import com.google.common.collect.ImmutableMap;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * rpc request to NodeUserController
 *
 * @author beiwei
 * @date 2023/9/14
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/user/remote")
public class RemoteUserController implements NodeUserControllerInterface {

    private final static String HTTP_PREFIX = "http://";
    /**
     * "http://localhost:8080/api/v1alpha1/user/node";
     */
    @Value("${secretpad.gateway}")
    private String gateway;
    /**
     * kuscia gateway forwarding destination
     */
    @Value("${secretpad.center-platform-service::secretpad.master.svc}")
    private String centerPlatformService;

    /**
     * rest password
     *
     * @param userRequest request
     * @return response
     */
    @ResponseBody
    @PostMapping(value = "/resetPassword", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.REMOTE_USER_RESET_PWD)
    @Override
    public SecretPadResponse<String> resetPwd(@Valid @RequestBody ResetNodeUserPwdRequest userRequest) {
        // proxy to remote NodeUserController
        return RestTemplateUtil.sendPostJson(centerUserNodeUrl() + "/resetPassword", userRequest, buildHeader(), SecretPadResponse.class);
    }

    /**
     * build remote header
     *
     * @return ImmutableMap of Host
     */
    @NotNull
    private ImmutableMap<String, String> buildHeader() {
        return ImmutableMap.of("Host", centerPlatformService);
    }

    /**
     * center platform url for edge user
     *
     * @return path
     */
    private String centerUserNodeUrl() {
        return HTTP_PREFIX + gateway + "/api/v1alpha1/user/node";
    }
}
