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
import org.secretflow.secretpad.service.ModelManagementService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.model.*;
import org.secretflow.secretpad.service.model.serving.ServingDetailVO;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenmingliang
 * @date 2024/01/18
 */
@RestController
@RequestMapping("/api/v1alpha1/model")
public class ModelManagementController {

    @Resource
    private ModelManagementService modelManagementService;

    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @PostMapping(value = "/page", consumes = "application/json")
    @Operation(summary = "query model list", description = "query model list")
    public SecretPadResponse<ModelPackListVO> modelPackPage(@RequestBody @Valid QueryModelPageRequest queryModelPageRequest) {
        ModelPackListVO modelPackListVO = modelManagementService.modelPackPage(queryModelPageRequest);
        return SecretPadResponse.success(modelPackListVO);
    }


    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @PostMapping(value = "/detail", consumes = "application/json")
    @Operation(summary = "query model detail", description = "query model detail")
    public SecretPadResponse<ModelPackDetailVO> modelPackDetail(@RequestBody @Valid QueryModelDetailRequest queryModelDetailRequest) {
        ModelPackDetailVO modelPackDetailVO = modelManagementService.modelPackDetail(queryModelDetailRequest.getModelId(), queryModelDetailRequest.getProjectId());
        return SecretPadResponse.success(modelPackDetailVO);
    }

    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @PostMapping(value = "/info", consumes = "application/json")
    @Operation(summary = "query model info", description = "query model info")
    public SecretPadResponse<ModelPackInfoVO> modelPackInfo(@RequestBody @Valid QueryModelDetailRequest queryModelDetailRequest) {
        ModelPackInfoVO modelPackInfoVO = modelManagementService.modelPackInfo(queryModelDetailRequest.getModelId(), queryModelDetailRequest.getProjectId());
        return SecretPadResponse.success(modelPackInfoVO);
    }


    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @PostMapping(value = "/serving/create", consumes = "application/json")
    @Operation(summary = "create model serving", description = "create model serving online")
    public SecretPadResponse<ModelPartiesVO> createServing(@RequestBody @Valid CreateModelServingRequest createModelServingRequest) {
        modelManagementService.createModelServing(createModelServingRequest.getProjectId(), createModelServingRequest.getModelId(), createModelServingRequest.getPartyConfigs());
        return SecretPadResponse.success();
    }


    @PostMapping(value = "/serving/detail", consumes = "application/json")
    @Operation(summary = "query model serving", description = "query model serving details")
    public SecretPadResponse<ServingDetailVO> modelServing(@RequestBody @Valid QueryModelServingRequest queryModelServingRequest) {
        ServingDetailVO servingDetailVO = modelManagementService.queryModelServingDetail(queryModelServingRequest.getServingId());
        return SecretPadResponse.success(servingDetailVO);
    }


    @PostMapping(value = "/serving/delete", consumes = "application/json")
    @Operation(summary = "delete model serving", description = "delete model serving")
    public SecretPadResponse deleteModelServing(@RequestBody @Valid DeleteModelServingRequest deleteModelServingRequest) {
        modelManagementService.deleteModelServing(deleteModelServingRequest.getServingId());
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/discard", consumes = "application/json")
    @Operation(summary = "discard model pack", description = "discard model pack")
    public SecretPadResponse discardModelPack(@RequestBody @Valid DiscardModelPackRequest discardModelPackRequest) {
        modelManagementService.discardModelPack(discardModelPackRequest.getModelId());
        return SecretPadResponse.success();
    }


    @PostMapping(value = "/delete", consumes = "application/json")
    @Operation(summary = "delete model pack", description = "delete model pack")
    public SecretPadResponse deleteModelPack(@RequestBody @Valid DeleteModelPackRequest deleteModelPackRequest) {
        modelManagementService.deleteModelPack(deleteModelPackRequest.getNodeId(), deleteModelPackRequest.getModelId());
        return SecretPadResponse.success();
    }

}
