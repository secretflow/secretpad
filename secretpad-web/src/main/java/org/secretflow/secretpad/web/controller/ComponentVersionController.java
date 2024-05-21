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
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.dto.SecretPadResponse;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.model.component.ComponentVersion;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * ComponentVersionController
 *
 * @author lufeng
 * @date 2023/4/22
 */

@RestController
@RequestMapping(value = "/api/v1alpha1")
public class ComponentVersionController {

    @Resource
    private ComponentService componentService;


    @Value("${secretpad.deploy-mode}")
    private String deployMode;

    /**
     * list secretflow component version
     *
     * @return successful SecretPadResponse with secretflow component version
     */
    @Operation(summary = "list version")
    @PostMapping("/version/list")
    @ApiResource(code = ApiResourceCodeConstants.COMPONENT_VERSION_LIST)
    public SecretPadResponse<ComponentVersion> listVersion() {
        return SecretPadResponse.success(componentService.listComponentVersion(deployMode));
    }


}