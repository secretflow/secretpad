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

package org.secretflow.secretpad.manager.integration.datasource;

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatable.DatatableManager;
import org.secretflow.secretpad.manager.integration.model.DatasourceDTO;

import lombok.Setter;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

/**
 * @author lufeng
 * @date 2024/5/23
 */
public class DatasourceManager extends AbstractDatasourceManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatatableManager.class);
    private final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Value("${secretpad.platform-type}")
    @Setter
    private String plaformType;

    public DatasourceManager(KusciaGrpcClientAdapter kusciaGrpcClientAdapter) {
        this.kusciaGrpcClientAdapter = kusciaGrpcClientAdapter;
    }

    @Override
    public Optional<DatasourceDTO> findById(DatasourceDTO.NodeDatasourceId nodeDatasourceId) {
        Domaindatasource.QueryDomainDataSourceRequest queryDomainDataSourceRequest;
        queryDomainDataSourceRequest = Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDomainId(nodeDatasourceId.getNodeId())
                .setDatasourceId(nodeDatasourceId.getDatasourceId())
                .build();

        Domaindatasource.QueryDomainDataSourceResponse response = PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(plaformType))
                ? kusciaGrpcClientAdapter.queryDomainDataSource(queryDomainDataSourceRequest, nodeDatasourceId.getNodeId())
                : kusciaGrpcClientAdapter.queryDomainDataSource(queryDomainDataSourceRequest);
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("lock up from kusciaapi failed: code={}, message={}, request={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), JsonUtils.toJSONString(nodeDatasourceId));
            throw SecretpadException.of(DatasourceErrorCode.QUERY_DATASOURCE_FAILED, response.getStatus().getMessage());
        }
        return Optional.of(DatasourceDTO.fromDomainDatasource(response.getData()));
    }
}
