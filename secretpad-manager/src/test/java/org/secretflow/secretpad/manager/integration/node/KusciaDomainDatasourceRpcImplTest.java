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

import org.secretflow.secretpad.manager.kuscia.grpc.impl.KusciaDomainDatasourceRpcImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author lufeng
 * @date 2024/5/28
 */
@ExtendWith(MockitoExtension.class)
public class KusciaDomainDatasourceRpcImplTest {

    @Mock
    private DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub domainDataSourceServiceBlockingStub;

    private KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc;

    @BeforeEach
    void setUp() {
        kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
    }

    @Test
    void createDomainDataSourceSuccess() {

        Domaindatasource.CreateDomainDataSourceRequest request = Domaindatasource.CreateDomainDataSourceRequest.newBuilder()
                .build();
        Domaindatasource.CreateDomainDataSourceResponse response = Domaindatasource.CreateDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0)
                        .setMessage("Success")
                        .build())
                .build();

        when(domainDataSourceServiceBlockingStub.createDomainDataSource(request)).thenReturn(response);

        Domaindatasource.CreateDomainDataSourceResponse actualResponse = kusciaDomainDatasourceRpc.createDomainDataSource(request);

        assertNotNull(actualResponse);
        assertEquals(response.getStatus(), actualResponse.getStatus());
    }

    @Test
    void queryDomainDataSourceSuccess() {
        Domaindatasource.QueryDomainDataSourceRequest request = Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .build();
        Domaindatasource.QueryDomainDataSourceResponse response = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0)
                        .setMessage("Success")
                        .build())
                .build();

        when(domainDataSourceServiceBlockingStub.queryDomainDataSource(request)).thenReturn(response);

        Domaindatasource.QueryDomainDataSourceResponse actualResponse = kusciaDomainDatasourceRpc.queryDomainDataSource(request);

        assertNotNull(actualResponse);
        assertEquals(response.getStatus(), actualResponse.getStatus());
    }

    @Test
    void deleteDomainDataSourceSuccess() {
        Domaindatasource.DeleteDomainDataSourceRequest request = Domaindatasource.DeleteDomainDataSourceRequest.newBuilder()
                .build();
        Domaindatasource.DeleteDomainDataSourceResponse response = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0)
                        .setMessage("Success")
                        .build())
                .build();

        when(domainDataSourceServiceBlockingStub.deleteDomainDataSource(request)).thenReturn(response);

        Domaindatasource.DeleteDomainDataSourceResponse actualResponse = kusciaDomainDatasourceRpc.deleteDomainDataSource(request);

        assertNotNull(actualResponse);
        assertEquals(response.getStatus(), actualResponse.getStatus());
    }

    @Test
    void updateDomainDataSourceSuccess() {
        Domaindatasource.UpdateDomainDataSourceRequest request = Domaindatasource.UpdateDomainDataSourceRequest.newBuilder()
                .build();
        Domaindatasource.UpdateDomainDataSourceResponse response = Domaindatasource.UpdateDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0)
                        .setMessage("Success")
                        .build())
                .build();

        when(domainDataSourceServiceBlockingStub.updateDomainDataSource(request)).thenReturn(response);

        Domaindatasource.UpdateDomainDataSourceResponse actualResponse = kusciaDomainDatasourceRpc.updateDomainDataSource(request);

        assertNotNull(actualResponse);
        assertEquals(response.getStatus(), actualResponse.getStatus());
    }

    @Test
    void listDomainDataSourceSuccess() {
        Domaindatasource.ListDomainDataSourceRequest request = Domaindatasource.ListDomainDataSourceRequest.newBuilder()
                .build();
        Domaindatasource.ListDomainDataSourceResponse response = Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0)
                        .setMessage("Success")
                        .build())
                .build();

        when(domainDataSourceServiceBlockingStub.listDomainDataSource(request)).thenReturn(response);

        Domaindatasource.ListDomainDataSourceResponse actualResponse = kusciaDomainDatasourceRpc.listDomainDataSource(request);
        assertNotNull(actualResponse);
        assertEquals(response.getStatus(), actualResponse.getStatus());
    }

    @Test
    void batchQueryDomainDataSourceSuccess() {
        Domaindatasource.BatchQueryDomainDataSourceRequest request = Domaindatasource.BatchQueryDomainDataSourceRequest.newBuilder()
                .build();
        Domaindatasource.BatchQueryDomainDataSourceResponse response = Domaindatasource.BatchQueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0)
                        .setMessage("Success")
                        .build())
                .build();

        when(domainDataSourceServiceBlockingStub.batchQueryDomainDataSource(request)).thenReturn(response);

        Domaindatasource.BatchQueryDomainDataSourceResponse actualResponse = kusciaDomainDatasourceRpc.batchQueryDomainDataSource(request);

        assertNotNull(actualResponse);
        assertEquals(response.getStatus(), actualResponse.getStatus());
    }


}