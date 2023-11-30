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
import org.secretflow.secretpad.service.NodeUserService;
import org.secretflow.secretpad.service.model.auth.ResetNodeUserPwdRequest;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * For node user in center platform(rpc mode)
 *
 * @author beiwei
 * @date 2023/9/13
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/user/node")
public class NodeUserController implements NodeUserControllerInterface {

    @Autowired
    private NodeUserService nodeUserService;


    /**
     * reset password
     *
     * @param userRequest reset password request
     * @return response
     */
    @Override
    @ResponseBody
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_USER_RESET_PWD)
    @PostMapping(value = "/resetPassword", consumes = "application/json")
    public SecretPadResponse<String> resetPwd(@Valid @RequestBody ResetNodeUserPwdRequest userRequest) {
        nodeUserService.resetPassword(userRequest);
        return SecretPadResponse.success("ok");
    }
}
