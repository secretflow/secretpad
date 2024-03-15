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

import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.service.ModelExportService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.model.export.*;

import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * model export controller
 *
 * @author yutu
 * @date 2024/01/29
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/model")
public class ModelExportController {
    @Resource
    private ModelExportService modelExportService;


    @PostMapping(value = "/pack", produces = "application/json", consumes = "application/json")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    public SecretPadResponse<ModelExportPackageResponse> pack(@Valid @RequestBody ModelExportPackageRequest request) throws InvalidProtocolBufferException {
        return SecretPadResponse.success(modelExportService.exportModel(request));
    }

    @PostMapping(value = "/status", produces = "application/json", consumes = "application/json")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    public SecretPadResponse<Object> status(@Valid @RequestBody ModelExportStatusRequest request) {
        return SecretPadResponse.success(modelExportService.queryModel(request));
    }

    @PostMapping(value = "/modelPartyPath", produces = "application/json", consumes = "application/json")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    public SecretPadResponse<List<ModelPartyPathResponse>> modelPartyPath(@Valid @RequestBody ModelPartyPathRequest request) {
        return SecretPadResponse.success(modelExportService.modelPartyPath(request));
    }
}