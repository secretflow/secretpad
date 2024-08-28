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
import org.secretflow.secretpad.service.DatasourceService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasource.*;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenmingliang
 * @date 2024/05/23
 */
@RestController
@RequestMapping("/api/v1alpha1/datasource")
@AllArgsConstructor
public class DataSourceController {

    private final DatasourceService datasourceService;

    @PostMapping("/create")
    @DataResource(field = "ownerId", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<CreateDatasourceVO> create(@RequestBody @Valid CreateDatasourceRequest createDatasourceRequest) {
        return SecretPadResponse.success(datasourceService.createDatasource(createDatasourceRequest));
    }

    @PostMapping("/delete")
    @DataResource(field = "ownerId", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse delete(@RequestBody @Valid DeleteDatasourceRequest deleteDatasourceRequest) {
        datasourceService.deleteDatasource(deleteDatasourceRequest);
        return SecretPadResponse.success();
    }

    @PostMapping("/list")
    @DataResource(field = "ownerId", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<DatasourceListVO> list(@RequestBody @Valid DatasourceListRequest datasourceListRequest) {
        return SecretPadResponse.success(datasourceService.listDatasource(datasourceListRequest));
    }

    @PostMapping("/detail")
    @DataResource(field = "ownerId", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<DatasourceDetailAggregateVO> detail(@RequestBody @Valid DatasourceDetailRequest datasourceListRequest) {
        return SecretPadResponse.success(datasourceService.datasourceDetail(datasourceListRequest));
    }

    /**
     * query the datasource belongs to which nodes
     *
     * @param datasourceNodesRequest
     * @return DatasourceNodesVO
     */
    @PostMapping("/nodes")
    @DataResource(field = "ownerId", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<DatasourceNodesVO> nodes(@RequestBody @Valid DatasourceNodesRequest datasourceNodesRequest) {
        return SecretPadResponse.success(datasourceService.datasourceNodes(datasourceNodesRequest));
    }

}
