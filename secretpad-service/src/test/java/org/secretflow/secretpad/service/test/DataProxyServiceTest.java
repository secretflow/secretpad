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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.dataproxy.DataProxyService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

import java.util.List;

/**
 * @author yutu
 * @date 2024/08/02
 */
@ExtendWith(MockitoExtension.class)
public class DataProxyServiceTest {

    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Mock
    private NodeRepository nodeRepository;

    @Test
    public void updateDataSourceUseDataProxyInMasterTest() {
        DataProxyService dataProxyService = new DataProxyService();
        dataProxyService.setDataProxyEnabled(true);
        dataProxyService.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        dataProxyService.setNodeRepository(nodeRepository);
        Mockito.when(nodeRepository.findAll()).thenReturn(List.of(NodeDO.builder().nodeId("alice").build()));
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSourceList.newBuilder().addDatasourceList(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build()).build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(listDomainDataSourceResponse);
        dataProxyService.updateDataSourceUseDataProxyInMaster();
    }

    @Test
    void updateDataSourceUseDataProxyInP2pTest() {
        DataProxyService dataProxyService = new DataProxyService();
        dataProxyService.setDataProxyEnabled(true);
        dataProxyService.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        dataProxyService.setNodeRepository(nodeRepository);
        Mockito.when(nodeRepository.findByInstId(Mockito.any())).thenReturn(List.of(NodeDO.builder().nodeId("alice").build()));
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSourceList.newBuilder().addDatasourceList(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build()).build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(listDomainDataSourceResponse);
        dataProxyService.updateDataSourceUseDataProxyInP2p("alice");
    }
}