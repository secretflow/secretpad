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

package org.secretflow.secretpad.manager.kuscia.grpc.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author chenmingliang
 * @date 2024/05/28
 */
@ExtendWith(MockitoExtension.class)
public class KusciaDomainDatasourceRpcImplTest {


    @Mock
    private DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub domainDataSourceServiceBlockingStub;

    @Test
    public void testCreateDomainDatasource() {
        Domaindatasource.CreateDomainDataSourceRequest request = Domaindatasource.CreateDomainDataSourceRequest.newBuilder().build();
        Domaindatasource.CreateDomainDataSourceResponse response = Domaindatasource.CreateDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        when(domainDataSourceServiceBlockingStub.createDomainDataSource(request)).thenReturn(response);
        KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
        kusciaDomainDatasourceRpc.createDomainDataSource(request);
        verify(domainDataSourceServiceBlockingStub).createDomainDataSource(request);
    }

    @Test
    public void testQueryDomainDataSource() {
        Domaindatasource.QueryDomainDataSourceRequest request = Domaindatasource.QueryDomainDataSourceRequest.newBuilder().build();
        Domaindatasource.QueryDomainDataSourceResponse response = Domaindatasource.QueryDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        when(domainDataSourceServiceBlockingStub.queryDomainDataSource(request)).thenReturn(response);
        KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
        kusciaDomainDatasourceRpc.queryDomainDataSource(request);
        verify(domainDataSourceServiceBlockingStub).queryDomainDataSource(request);
    }

    @Test
    public void testDeleteDomainDataSource() {
        Domaindatasource.DeleteDomainDataSourceRequest request = Domaindatasource.DeleteDomainDataSourceRequest.newBuilder().build();
        Domaindatasource.DeleteDomainDataSourceResponse response = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        when(domainDataSourceServiceBlockingStub.deleteDomainDataSource(request)).thenReturn(response);
        KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
        kusciaDomainDatasourceRpc.deleteDomainDataSource(request);
        verify(domainDataSourceServiceBlockingStub).deleteDomainDataSource(request);
    }

    @Test
    public void testUpdateDomainDataSource() {
        Domaindatasource.UpdateDomainDataSourceRequest request = Domaindatasource.UpdateDomainDataSourceRequest.newBuilder().build();
        Domaindatasource.UpdateDomainDataSourceResponse response = Domaindatasource.UpdateDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        when(domainDataSourceServiceBlockingStub.updateDomainDataSource(request)).thenReturn(response);
        KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
        kusciaDomainDatasourceRpc.updateDomainDataSource(request);
        verify(domainDataSourceServiceBlockingStub).updateDomainDataSource(request);
    }

    @Test
    public void testListDomainDataSource() {
        Domaindatasource.ListDomainDataSourceRequest request = Domaindatasource.ListDomainDataSourceRequest.newBuilder().build();
        Domaindatasource.ListDomainDataSourceResponse response = Domaindatasource.ListDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        when(domainDataSourceServiceBlockingStub.listDomainDataSource(request)).thenReturn(response);
        KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
        kusciaDomainDatasourceRpc.listDomainDataSource(request);
        verify(domainDataSourceServiceBlockingStub).listDomainDataSource(request);
    }

    @Test
    public void testBatchQueryDomainDataSource() {
        Domaindatasource.BatchQueryDomainDataSourceRequest request = Domaindatasource.BatchQueryDomainDataSourceRequest.newBuilder().build();
        Domaindatasource.BatchQueryDomainDataSourceResponse response = Domaindatasource.BatchQueryDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        when(domainDataSourceServiceBlockingStub.batchQueryDomainDataSource(request)).thenReturn(response);
        KusciaDomainDatasourceRpcImpl kusciaDomainDatasourceRpc = new KusciaDomainDatasourceRpcImpl(domainDataSourceServiceBlockingStub);
        kusciaDomainDatasourceRpc.batchQueryDomainDataSource(request);
        verify(domainDataSourceServiceBlockingStub).batchQueryDomainDataSource(request);
    }

}
