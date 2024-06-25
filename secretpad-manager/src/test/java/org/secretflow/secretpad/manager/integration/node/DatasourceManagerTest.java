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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.manager.integration.datasource.DatasourceManager;
import org.secretflow.secretpad.manager.integration.model.DatasourceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author lufeng
 * @date 2024/5/24
 */
@ExtendWith(MockitoExtension.class)
public class DatasourceManagerTest {

    @Mock
    private DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub datasourceStub;
    @Test
    void findByIdSuccess() {

        DatasourceManager datasourceManager = new DatasourceManager(datasourceStub);
        DatasourceDTO.NodeDatasourceId nodeDatasourceId = DatasourceDTO.NodeDatasourceId.from("nodeId", "datasourceId");
        Domaindatasource.QueryDomainDataSourceRequest queryDomainDataSourceRequest;
        queryDomainDataSourceRequest = Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDomainId(nodeDatasourceId.getNodeId())
                .setDatasourceId(nodeDatasourceId.getDatasourceId())
                .build();
        Mockito.when(datasourceStub.queryDomainDataSource(queryDomainDataSourceRequest)).thenReturn(buildQueryDomainDatasourceResponse(0));
        datasourceManager.findById(nodeDatasourceId);
    }
    private Domaindatasource.QueryDomainDataSourceResponse buildQueryDomainDatasourceResponse(Integer code) {
        return Domaindatasource.QueryDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindatasource.DomainDataSource.newBuilder().setDomainId("domainId").setDatasourceId("datasourceId")
                        .setType("OSS").setName("name")
                        .setDatasourceId("datasourceId").build()
        ).build();
    }

}
