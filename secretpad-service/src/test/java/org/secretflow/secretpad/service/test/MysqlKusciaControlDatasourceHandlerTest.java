/*
 * Copyright 2023 Ant Group Co., Ltd.
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
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datasource.mysql.MysqlManager;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.handler.datasource.MysqlKusciaControlDatasourceHandler;
import org.secretflow.secretpad.service.model.datasource.CreateDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailRequest;
import org.secretflow.secretpad.service.model.datasource.DeleteDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.MysqlDatasourceInfo;
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

import java.util.List;

import static org.mockito.Mockito.when;

/**
 * @author lufeng
 * @date 2024/9/3
 */
@ExtendWith(MockitoExtension.class)
public class MysqlKusciaControlDatasourceHandlerTest {
    @Mock
    private MysqlManager mysqlManager;
    @Mock
    private EnvService envService;
    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Mock
    private DatatableService datatableService;

    /**
     * test MysqlKusciaControlDatasourceHandler supports
     */
    @Test
    public void supportsTest() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        Assertions.assertEquals(List.of(DataSourceTypeEnum.MYSQL), mysqlKusciaControlDatasourceHandler.supports());
    }

    /**
     * listDatasourceTest
     */
    @Test
    public void listDatasourceTest() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSourceList.newBuilder().addDatasourceList(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build()).build())
                .build();
        when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(listDomainDataSourceResponse);
        mysqlKusciaControlDatasourceHandler.listDatasource("alice");
    }

    /**
     * datasourceDetailTest
     */
    @Test
    public void datasourceDetailTest() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
        datasourceDetailRequest.setDatasourceId("datasourceId");
        datasourceDetailRequest.setOwnerId("domainId");
        datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE);
        Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                 .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(true).build())
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(queryDomainDataSourceResponse);
        mysqlKusciaControlDatasourceHandler.datasourceDetail(datasourceDetailRequest);
    }



    /**
     * datasourceDetailTestisCenter
     */
    @Test
    public void datasourceDetailTestisCenter() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        Mockito.when(envService.isCenter()).thenReturn(true);
        Mockito.when(envService.isEmbeddedNode(Mockito.any())).thenReturn(true);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
        datasourceDetailRequest.setDatasourceId("datasourceId");
        datasourceDetailRequest.setOwnerId("domainId");
        datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE);
        Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(Domaindatasource.DomainDataSource.newBuilder().setAccessDirectly(false).build())
                .build();
        when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(queryDomainDataSourceResponse);
        mysqlKusciaControlDatasourceHandler.datasourceDetail(datasourceDetailRequest);
    }

    /**
     * createDatasourceTest
     */
    @Test
    public void createDatasourceTest() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        CreateDatasourceRequest createDatasourceRequest = new CreateDatasourceRequest();
        createDatasourceRequest.setOwnerId("domainId");
        createDatasourceRequest.setNodeIds(List.of("nodeId"));
        createDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE);
        createDatasourceRequest.setName("datasourceId");
        createDatasourceRequest.setDataSourceInfo(MysqlDatasourceInfo.builder().endpoint("localhost:3306").user("username").password("password").database("database").build());
        try (MockedStatic<HttpUtils> theMock = Mockito.mockStatic(HttpUtils.class)) {
            when(HttpUtils.detection(Mockito.any())).thenReturn(true);
            Assertions.assertThrows(NullPointerException.class, () -> mysqlKusciaControlDatasourceHandler.createDatasource(createDatasourceRequest));
        }
    }

    /**
     * deleteDatasourceTest
     */
    @Test
    public void deleteDatasourceTest() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
        deleteDatasourceRequest.setOwnerId("domainId");
        deleteDatasourceRequest.setDatasourceId("datasourceId");
        deleteDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE);
        Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSourceResponse = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        when(kusciaGrpcClientAdapter.deleteDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(deleteDomainDataSourceResponse);
        mysqlKusciaControlDatasourceHandler.deleteDatasource(deleteDatasourceRequest);
    }

    /**
     * deleteDatasourceTestisCenter
     */
    @Test
    public void deleteDatasourceTestisCenter() {
        MysqlKusciaControlDatasourceHandler mysqlKusciaControlDatasourceHandler = new MysqlKusciaControlDatasourceHandler();
        mysqlKusciaControlDatasourceHandler.setMysqlManager(mysqlManager);
        mysqlKusciaControlDatasourceHandler.setEnvService(envService);
        Mockito.when(envService.isCenter()).thenReturn(true);
        Mockito.when(envService.isEmbeddedNode(Mockito.any())).thenReturn(true);
        mysqlKusciaControlDatasourceHandler.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        mysqlKusciaControlDatasourceHandler.setDatatableService(datatableService);
        DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
        deleteDatasourceRequest.setOwnerId("domainId");
        deleteDatasourceRequest.setDatasourceId("datasourceId");
        deleteDatasourceRequest.setType(DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE);
        Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSourceResponse = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        when(kusciaGrpcClientAdapter.deleteDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(deleteDomainDataSourceResponse);
        mysqlKusciaControlDatasourceHandler.deleteDatasource(deleteDatasourceRequest);
    }

}