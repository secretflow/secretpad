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

import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datasource.odps.OdpsManager;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.handler.datasource.OdpsKusciaControlDatasourceHandler;
import org.secretflow.secretpad.service.model.datasource.CreateDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailRequest;
import org.secretflow.secretpad.service.model.datasource.DeleteDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.OdpsDatasourceInfo;
import org.secretflow.secretpad.service.model.datatable.DatatableVO;
import org.secretflow.secretpad.service.util.HttpUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author yutu
 * @date 2024/08/05
 */
@ExtendWith(MockitoExtension.class)
public class OdpsKusciaControlDatasourceHandlerTest {

    @Mock
    private OdpsManager odpsManager;
    @Mock
    private EnvService envService;
    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Mock
    private DatatableService datatableService;

    @Test
    public void supportsTest() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        Assertions.assertEquals(List.of(DataSourceTypeEnum.ODPS), odpsKusciaControlDatasourceHandler.supports());
    }

    @Test
    void listDatasourceTest() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSourceList.newBuilder().addDatasourceList(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build()).build())
                .build();
        when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(listDomainDataSourceResponse);
        odpsKusciaControlDatasourceHandler.listDatasource("alice");
    }

    @Test
    void listDatasourceTestisCenter() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        Mockito.when(envService.isCenter()).thenReturn(true);
        Mockito.when(envService.isEmbeddedNode(Mockito.any())).thenReturn(true);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSourceList.newBuilder().addDatasourceList(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build()).build())
                .build();
        when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(listDomainDataSourceResponse);
        odpsKusciaControlDatasourceHandler.listDatasource("alice");
    }

    @Test
    void datasourceDetailTest() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
        datasourceDetailRequest.setDatasourceId("datasourceId");
        datasourceDetailRequest.setOwnerId("domainId");
        datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE);

        Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build())
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(queryDomainDataSourceResponse);
        odpsKusciaControlDatasourceHandler.datasourceDetail(datasourceDetailRequest);
    }

    @Test
    void datasourceDetailTestisCenter() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        Mockito.when(envService.isCenter()).thenReturn(true);
        Mockito.when(envService.isEmbeddedNode(Mockito.any())).thenReturn(true);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
        datasourceDetailRequest.setDatasourceId("datasourceId");
        datasourceDetailRequest.setOwnerId("domainId");
        datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE);

        Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build())
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(queryDomainDataSourceResponse);
        odpsKusciaControlDatasourceHandler.datasourceDetail(datasourceDetailRequest);
    }

    @Test
    void createDatasourceTest() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        CreateDatasourceRequest createDatasourceRequest = new CreateDatasourceRequest();
        createDatasourceRequest.setOwnerId("domainId");
        createDatasourceRequest.setNodeIds(List.of("nodeId"));
        createDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE);
        createDatasourceRequest.setName("datasourceId");
        createDatasourceRequest.setDataSourceInfo(OdpsDatasourceInfo.builder().accessId("accessId").accessKey("accessKey").endpoint("http://128.0.8.1").project("project").build());
        try (MockedStatic<HttpUtils> theMock = Mockito.mockStatic(HttpUtils.class)) {
            when(HttpUtils.detection(Mockito.any())).thenReturn(true);
            Assertions.assertThrows(NullPointerException.class, () -> odpsKusciaControlDatasourceHandler.createDatasource(createDatasourceRequest));
        }
    }

    @Test
    void deleteDatasourceTest() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
        deleteDatasourceRequest.setOwnerId("domainId");
        deleteDatasourceRequest.setDatasourceId("datasourceId");
        deleteDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE);
        Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSourceResponse = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        when(kusciaGrpcClientAdapter.deleteDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(deleteDomainDataSourceResponse);
        odpsKusciaControlDatasourceHandler.deleteDatasource(deleteDatasourceRequest);
    }

    @Test
    void deleteDatasourceTestisCenter() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        Mockito.when(envService.isCenter()).thenReturn(true);
        Mockito.when(envService.isEmbeddedNode(Mockito.any())).thenReturn(true);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
        deleteDatasourceRequest.setOwnerId("domainId");
        deleteDatasourceRequest.setDatasourceId("datasourceId");
        deleteDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE);
        Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSourceResponse = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        when(kusciaGrpcClientAdapter.deleteDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(deleteDomainDataSourceResponse);
        odpsKusciaControlDatasourceHandler.deleteDatasource(deleteDatasourceRequest);
    }

    @Test
    void deleteDatasourceTestWithDatatableVOS() {
        OdpsKusciaControlDatasourceHandler odpsKusciaControlDatasourceHandler = new OdpsKusciaControlDatasourceHandler();
        odpsKusciaControlDatasourceHandler.setOdpsManager(odpsManager);
        odpsKusciaControlDatasourceHandler.setEnvService(envService);
        odpsKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        odpsKusciaControlDatasourceHandler.setDatatableService(datatableService);
        List<DatatableVO> datatableVOS = new ArrayList<>();
        DatatableVO datatableVO = new DatatableVO();
        datatableVO.setDatatableId("datatableId");
        datatableVO.setDatasourceId("datasourceId");
        datatableVOS.add(datatableVO);
        when(datatableService.findDatatableByNodeId(Mockito.any())).thenReturn(datatableVOS);
        DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
        deleteDatasourceRequest.setOwnerId("domainId");
        deleteDatasourceRequest.setDatasourceId("datasourceId");
        deleteDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE);
        Assertions.assertThrows(SecretpadException.class, () -> odpsKusciaControlDatasourceHandler.deleteDatasource(deleteDatasourceRequest));
    }
}