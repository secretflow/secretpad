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
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.UserService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * query user info
     *
     * @return successful SecretPadResponse with user name
     */
    @PostMapping(value = "/get")
    @ApiResource(code = ApiResourceCodeConstants.USER_GET)
    public SecretPadResponse<UserContextDTO> get() {
        return SecretPadResponse.success(UserContext.getUser());
    }

}
