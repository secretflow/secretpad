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
import org.secretflow.secretpad.service.FeatureTableService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasource.feature.CreateFeatureDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.feature.FeatureDataSourceVO;
import org.secretflow.secretpad.service.model.datasource.feature.ListProjectFeatureDatasourceRequest;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/24
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/feature_datasource")
public class FeatureDatasourceController {

    @Resource
    private FeatureTableService featureTableService;

    @PostMapping(value = "/create", consumes = "application/json")
    @DataResource(field = "ownerId", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse createFeatureDatasource(@RequestBody @Valid CreateFeatureDatasourceRequest createFeatureDatasourceRequest) {
        featureTableService.createFeatureTable(createFeatureDatasourceRequest);
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/auth/list", consumes = "application/json")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    public SecretPadResponse<List<FeatureDataSourceVO>> projectFeatureTableList(@RequestBody @Valid ListProjectFeatureDatasourceRequest listProjectFeatureDatasourceRequest) {
        List<FeatureDataSourceVO> featureDataSourceVOS = featureTableService.projectFeatureTableList(listProjectFeatureDatasourceRequest.getNodeId(), listProjectFeatureDatasourceRequest.getProjectId());
        return SecretPadResponse.success(featureDataSourceVOS);
    }

}
