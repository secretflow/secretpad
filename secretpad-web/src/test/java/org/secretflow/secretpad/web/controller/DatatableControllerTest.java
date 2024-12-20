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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datasource.oss.OssAutoCloseableClient;
import org.secretflow.secretpad.manager.integration.datasource.oss.OssClientFactory;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.datatable.*;
import org.secretflow.secretpad.service.util.IpFilterUtil;
import org.secretflow.secretpad.service.util.RateLimitUtil;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_TYPE_TABLE;
import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_VENDOR_MANUAL;

import static org.mockito.Mockito.when;

/**
 * DatatableController test
 *
 * @author guyu
 * @date 2023/8/2
 */
class DatatableControllerTest extends ControllerTest {

    private static final String PROJECT_ID = "testProjectId";

    private static final String DATATABLE_ID = "testDatatableId";

    @MockBean
    private ProjectDatatableRepository datatableRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private FeatureTableRepository featureTableRepository;

    @MockBean
    private ProjectFeatureTableRepository projectFeatureTableRepository;
    @MockBean
    private OssClientFactory ossClientFactory;

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private IpFilterUtil ipFilterUtil;

    /**
     * createDatable test
     *
     * @throws Exception
     */
    @Test
    public void createDatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).thenAnswer(invocation -> null);
        CreateDatatableRequest request = FakerUtils.fake(CreateDatatableRequest.class);
        request.setDatasourceType("OSS");
        request.setDatasourceName("ossDatasource");
        request.setOwnerId("alice");
        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_CREATE));
        Domaindata.CreateDomainDataResponse response = Domaindata.CreateDomainDataResponse.newBuilder()
                .setData(Domaindata.CreateDomainDataResponseData.newBuilder()
                        .setDomaindataId(request.getOwnerId())
                        .build())
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.createDomainData(
                Mockito.any(), Mockito.any())).thenReturn(response);
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any())).thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setData(Domaindatasource.DomainDataSource.newBuilder()
                        .setDatasourceId("datasourceId")
                        .setName("name")
                        .setType("type")
                        .setInfo(Domaindatasource.DataSourceInfo.newBuilder()
                                .setOss(Domaindatasource.OssDataSourceInfo.newBuilder()
                                        .setAccessKeyId("ak")
                                        .setAccessKeySecret("sk")
                                        .setEndpoint("endpoint")
                                        .setPrefix("prefix")
                                        .setBucket("bucket")
                                        .build())
                                .build())
                        .build())
                .setStatus(Common.Status.newBuilder().setCode(0).build()).build());

        // Act & Assert
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "createDataTable", CreateDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * createDatable test in p2p mysql
     *
     * @throws Exception
     */
    @Test
    public void createDatableMysql() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        CreateDatatableRequest request = FakerUtils.fake(CreateDatatableRequest.class);
        request.setDatasourceType("MYSQL");
        request.setDatasourceName("mysqlDatasource");
        request.setNodeIds(Collections.singletonList("nodeId"));
        request.setOwnerId("nodeId");
        UserContextDTO user = UserContext.getUser();
        user.setName("alice-test2");
        UserContext.setBaseUser(user);
        when(nodeRepository.findByNodeId("nodeId")).thenReturn(FakerUtils.fake(NodeDO.class));
        when(nodeRepository.findByInstId("nodeId")).thenReturn(List.of(NodeDO.builder().nodeId("nodeId").build()));
        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_CREATE));
        Domaindata.CreateDomainDataResponse response = Domaindata.CreateDomainDataResponse.newBuilder()
                .setData(Domaindata.CreateDomainDataResponseData.newBuilder()
                        .setDomaindataId(request.getOwnerId())
                        .build())
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        when(kusciaGrpcClientAdapter.createDomainData(
                Mockito.any(), Mockito.any())).thenReturn(response);
        when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any())).thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setData(Domaindatasource.DomainDataSource.newBuilder()
                        .setDatasourceId("datasourceId")
                        .setName("name")
                        .setType("type")
                        .setInfo(Domaindatasource.DataSourceInfo.newBuilder()
                                .setDatabase(Domaindatasource.DatabaseDataSourceInfo.newBuilder()
                                        .setEndpoint("localhost:3306")
                                        .setUser("user")
                                        .setPassword("password")
                                        .setDatabase("database")
                                        .build())
                                .build())
                        .build())
                .setStatus(Common.Status.newBuilder().setCode(0).build()).build());

        // Act & Assert
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "createDataTable", CreateDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * createDatable test in p2p odps
     *
     * @throws Exception
     */
    @Test
    public void createDatableOdps() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        CreateDatatableRequest request = FakerUtils.fake(CreateDatatableRequest.class);
        request.setDatasourceType("ODPS");
        request.setDatasourceName("odpsDatasource");
        request.setNodeIds(Collections.singletonList("nodeId"));
        request.setOwnerId("nodeId");
        UserContextDTO user = UserContext.getUser();
        user.setName("alice-test1");
        UserContext.setBaseUser(user);
        when(nodeRepository.findByNodeId("nodeId")).thenReturn(FakerUtils.fake(NodeDO.class));
        when(nodeRepository.findByInstId("nodeId")).thenReturn(List.of(NodeDO.builder().nodeId("nodeId").build()));
        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_CREATE));
        Domaindata.CreateDomainDataResponse response = Domaindata.CreateDomainDataResponse.newBuilder()
                .setData(Domaindata.CreateDomainDataResponseData.newBuilder()
                        .setDomaindataId(request.getOwnerId())
                        .build())
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .build();
        when(kusciaGrpcClientAdapter.createDomainData(
                Mockito.any(), Mockito.any())).thenReturn(response);
        when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any())).thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setData(Domaindatasource.DomainDataSource.newBuilder()
                        .setDatasourceId("datasourceId")
                        .setName("name")
                        .setType("type")
                        .setInfo(Domaindatasource.DataSourceInfo.newBuilder()
                                .setOdps(Domaindatasource.OdpsDataSourceInfo.newBuilder()
                                        .setAccessKeyId("ak")
                                        .setAccessKeySecret("sk")
                                        .setEndpoint("endpoint")
                                        .setProject("project")
                                        .build())
                                .build())
                        .build())
                .setStatus(Common.Status.newBuilder().setCode(0).build()).build());

        // Act & Assert
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "createDataTable", CreateDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }


    /**
     * create http Datatable test
     *
     * @throws Exception
     */

    @Test
    public void createHttpDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            Mockito.when(ipFilterUtil.urlIsIpInRange(Mockito.anyString())).thenReturn(false);
            CreateDatatableRequest request = new CreateDatatableRequest();
            UserContextDTO user = UserContext.getUser();
            user.setName("alice-test");
            UserContext.setBaseUser(user);
            request.setOwnerId("owner123");
            request.setNodeIds(Arrays.asList("node1", "node2"));
            request.setDatatableName("table123");
            request.setDatasourceId("datasource123");
            request.setDatasourceName("datasourceName");
            request.setDatasourceType("HTTP");
            request.setDesc("Test datatable");
            request.setRelativeUri("http://example.com");
            request.setColumns(Arrays.asList(new TableColumnVO("col1", "string", "column1"), new TableColumnVO("col2", "int", "column2")));

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "createDataTable", CreateDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }


    @Test
    void listDatatables() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            ListDatatableRequest request = FakerUtils.fake(ListDatatableRequest.class);
            request.setPageSize(10);
            request.setPageNumber(1);
            request.setOwnerId("alice");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_LIST));

            Domaindata.ListDomainDataResponse response = Domaindata.ListDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainDataList.newBuilder().build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.listDomainData(Domaindata.ListDomainDataRequest.newBuilder()
                            .setData(
                                    Domaindata.ListDomainDataRequestData.newBuilder()
                                            .setDomaindataType(DATA_TYPE_TABLE)
                                            .setDomaindataVendor(DATA_VENDOR_MANUAL)
                                            .setDomainId(request.getOwnerId())
                                            .build()
                            )
                            .build(), "alice"))
                    .thenReturn(response);
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            FeatureTableDO featureTableDO = FakerUtils.fake(FeatureTableDO.class);
            ProjectFeatureTableDO projectFeatureTableDO = FakerUtils.fake(ProjectFeatureTableDO.class);
            Mockito.when(nodeRepository.findByInstId(Mockito.anyString())).thenReturn(Collections.singletonList(nodeDO));
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);
            Mockito.when(projectFeatureTableRepository.findByNodeIdAndFeatureTableIds(request.getOwnerId(), Lists.newArrayList(featureTableDO.getUpk().getFeatureTableId()))).thenReturn(Collections.singletonList(projectFeatureTableDO));
            Mockito.when(featureTableRepository.findByNodeId(request.getOwnerId())).thenReturn(Collections.singletonList(featureTableDO));

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "listDatatables", ListDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("LOCAL");
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDatatableId())
                                    .build())
                            .build()))
                    .thenReturn(response);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * getOSSDatatable test
     *
     * @throws Exception
     */
    @Test
    void getOSSDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("OSS");
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().putAttributes("DatasourceType", "OSS")
                                    .setDomainId("alice")
                                    .setAuthor("alice")
                                    .setDatasourceId("datasourceId")
                                    .setStatus("1").build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDatatableId())
                                    .build())
                            .build()))
                    .thenReturn(response);
            //datasourceResponse
            Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(
                            Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId(response.getData().getDatasourceId())
                                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setOss(Domaindatasource.OssDataSourceInfo.newBuilder()
                                                    .setAccessKeySecret("XXXX")
                                                    .setEndpoint("http://xxxxx.com")
                                                    .setBucket("secretflow-dev")
                                                    .setAccessKeyId("XXXX")
                                                    .build())
                                            .build())
                                    .build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                            .setDomainId(response.getData().getAuthor())
                            .setDatasourceId(response.getData().getDatasourceId())
                            .build(), response.getData().getAuthor())
                    )
                    .thenReturn(datasourceResponse);
            OssAutoCloseableClient ossAutoCloseableClient = Mockito.mock(OssAutoCloseableClient.class);
            Mockito.when(ossClientFactory.getOssClient(Mockito.any())).thenReturn(ossAutoCloseableClient);
            Mockito.when(ossAutoCloseableClient.doesObjectExist(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getOdpsDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("ODPS");
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().putAttributes("DatasourceType", "ODPS")
                                    .setDomainId("alice")
                                    .setAuthor("alice")
                                    .setDatasourceId("datasourceId")
                                    .setStatus("1").build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDatatableId())
                                    .build())
                            .build()))
                    .thenReturn(response);
            //datasourceResponse
            Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(
                            Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId(response.getData().getDatasourceId())
                                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setOdps(Domaindatasource.OdpsDataSourceInfo.newBuilder()
                                                    .setAccessKeySecret("XXXX")
                                                    .setEndpoint("http://xxxxx.com")
                                                    .setProject("projectName")
                                                    .setAccessKeyId("XXXX")
                                                    .build())
                                            .build())
                                    .build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                            .setDomainId(response.getData().getAuthor())
                            .setDatasourceId(response.getData().getDatasourceId())
                            .build(), response.getData().getAuthor())
                    )
                    .thenReturn(datasourceResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getOSSDatatableAwsFalse() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("OSS");
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().putAttributes("DatasourceType", "OSS")
                                    .setDomainId("alice")
                                    .setAuthor("alice")
                                    .setDatasourceId("datasourceId")
                                    .setStatus("1").build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDatatableId())
                                    .build())
                            .build()))
                    .thenReturn(response);
            //datasourceResponse
            Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(
                            Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId(response.getData().getDatasourceId())
                                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setOss(Domaindatasource.OssDataSourceInfo.newBuilder()
                                                    .setAccessKeySecret("XXXX")
                                                    .setEndpoint("http://xxxxx.com")
                                                    .setBucket("secretflow-dev")
                                                    .setAccessKeyId("XXXX")
                                                    .build())
                                            .build())
                                    .build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                            .setDomainId(response.getData().getAuthor())
                            .setDatasourceId(response.getData().getDatasourceId())
                            .build(), response.getData().getAuthor())
                    )
                    .thenReturn(datasourceResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));

        });
    }

    /**
     * get odps datatable
     *
     * @throws Exception
     */
    @Test
    void getODPSDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("ODPS");
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().putAttributes("DatasourceType", "ODPS")
                                    .setDomainId("alice")
                                    .setAuthor("alice")
                                    .setDatasourceId("datasourceId")
                                    .setStatus("1").build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDatatableId())
                                    .build())
                            .build()))
                    .thenReturn(response);
            //datasourceResponse
            Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(
                            Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId(response.getData().getDatasourceId())
                                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setOdps(Domaindatasource.OdpsDataSourceInfo.newBuilder()
                                                    .setAccessKeySecret("XXXX")
                                                    .setEndpoint("http://xxxxx.com")
                                                    .setProject("secretflow-dev")
                                                    .setAccessKeyId("XXXX")
                                                    .build())
                                            .build())
                                    .build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                            .setDomainId(response.getData().getAuthor())
                            .setDatasourceId(response.getData().getDatasourceId())
                            .build(), response.getData().getAuthor())
                    )
                    .thenReturn(datasourceResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));

        });
    }

    //get mysql
    @Test
    void getMysqlDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("MYSQL");
            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().putAttributes("DatasourceType", "MYSQL")
                                    .setDomainId("alice")
                                    .setAuthor("alice")
                                    .setDatasourceId("datasourceId")
                                    .setStatus("1").build()
                    )
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                            .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                                    .setDomainId(request.getNodeId())
                                    .setDomaindataId(request.getDatatableId())
                                    .build())
                            .build()))
                    .thenReturn(response);
            //datasourceResponse
            Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(
                            Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId(response.getData().getDatasourceId())
                                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setDatabase(Domaindatasource.DatabaseDataSourceInfo.newBuilder()
                                                    .setEndpoint("localhost:3306")
                                                    .setUser("test-mysql")
                                                    .setPassword("XXXX")
                                                    .setDatabase("test")
                                                    .build())
                                            .build())
                                    .build()).build();
            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                            .setDomainId(response.getData().getAuthor())
                            .setDatasourceId(response.getData().getDatasourceId())
                            .build(), response.getData().getAuthor())
                    )
                    .thenReturn(datasourceResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));

        });
    }

    @Test
    void deleteDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponseWithEmptyData(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setType("CSV");
            request.setNodeId("alice");
            request.setDatasourceType("LOCAL");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_DELETE));

            Mockito.when(datatableRepository.authProjectDatatablesByDatatableIds(request.getNodeId(),
                    Collections.singletonList(request.getDatatableId()))).thenReturn(new ArrayList<>());

            Mockito.when(projectRepository.findAllById(List.of(PROJECT_ID))).thenReturn(buildProjectDO());

            Domaindata.DeleteDomainDataResponse response = Domaindata.DeleteDomainDataResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder()
                            .setDomainId(request.getNodeId())
                            .setDomaindataId(request.getDatatableId())
                            .build()))
                    .thenReturn(response);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "deleteDatatable", DeleteDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * delete http datatable
     *
     * @throws Exception
     */

    @Test
    void deleteHttpDatatable() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertResponseWithEmptyData(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setType("HTTP");
            request.setNodeId("alice");
            request.setDatasourceType("HTTP");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_DELETE));

            Mockito.when(datatableRepository.authProjectDatatablesByDatatableIds(request.getNodeId(),
                    Collections.singletonList(request.getDatatableId()))).thenReturn(new ArrayList<>());

            Mockito.when(projectFeatureTableRepository.findByNodeIdAndFeatureTableIds(Mockito.anyString(), Mockito.anyList())).thenReturn(List.of(FakerUtils.fake(ProjectFeatureTableDO.class)));
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "deleteDatatable", DeleteDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * delete http datatable with DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED
     *
     * @throws Exception
     */
    @Test
    void deleteHttpDatatableWithDatatableErrorCode() throws Exception {
        mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> null);
        assertErrorCode(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setType("HTTP");
            request.setNodeId("alice");
            request.setDatasourceType("HTTP");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_DELETE));

            Mockito.when(datatableRepository.authProjectDatatablesByDatatableIds(request.getNodeId(),
                    Collections.singletonList(request.getDatatableId()))).thenReturn(new ArrayList<>());
            ProjectFeatureTableDO projectFeatureTableDO = new ProjectFeatureTableDO();
            ProjectFeatureTableDO.UPK upk = new ProjectFeatureTableDO.UPK();
            upk.setProjectId(PROJECT_ID);
            upk.setFeatureTableId("featureTableId");
            projectFeatureTableDO.setUpk(upk);
            ProjectDO projectDO = ProjectDO.builder()
                    .projectId(PROJECT_ID)
                    .build();
            Mockito.when(projectFeatureTableRepository.findByNodeIdAndFeatureTableIds(Mockito.anyString(), Mockito.anyList())).thenReturn(List.of(projectFeatureTableDO));

            Mockito.when(projectRepository.findAllById(List.of(PROJECT_ID))).thenReturn(List.of(projectDO));
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "deleteDatatable", DeleteDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED);
    }


    @Test
    void deleteDatatableHasBeenAuthException() throws Exception {
        assertErrorCode(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setType("CSV");
            request.setNodeId("alice");
            request.setDatasourceType("LOCAL");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_DELETE));

            Mockito.when(datatableRepository.authProjectDatatablesByDatatableIds(request.getNodeId(),
                    Collections.singletonList(request.getDatatableId()))).thenReturn(buildProjectDatatableDO());

            Mockito.when(projectRepository.findAllById(List.of(PROJECT_ID))).thenReturn(buildProjectDO());

            Domaindata.DeleteDomainDataResponse response = Domaindata.DeleteDomainDataResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder()
                            .setDomainId(request.getNodeId())
                            .setDomaindataId(request.getDatatableId())
                            .build()))
                    .thenReturn(response);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "deleteDatatable", DeleteDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED);
    }

    @Test
    void deleteDatatableException() throws Exception {
        assertErrorCode(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");
            request.setDatasourceType("LOCAL");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_DELETE));

            Mockito.when(datatableRepository.authProjectDatatablesByDatatableIds(request.getNodeId(),
                    Collections.singletonList(request.getDatatableId()))).thenReturn(new ArrayList<>());

            Mockito.when(projectRepository.findAllById(List.of(PROJECT_ID))).thenReturn(buildProjectDO());

            Domaindata.DeleteDomainDataResponse response = Domaindata.DeleteDomainDataResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(1).build())
                    .build();
            Mockito.when(kusciaGrpcClientAdapter.deleteDomainData(Domaindata.DeleteDomainDataRequest.newBuilder()
                            .setDomainId(request.getNodeId())
                            .setDomaindataId(request.getDatatableId())
                            .build()))
                    .thenReturn(response);

            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "deleteDatatable", DeleteDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.DELETE_DATATABLE_FAILED);
    }

    private List<ProjectDatatableDO> buildProjectDatatableDO() {
        List<ProjectDatatableDO> projectDatatableDOS = new ArrayList<>();
        ProjectDatatableDO projectDatatableDO = new ProjectDatatableDO();
        ProjectDatatableDO.UPK upk = new ProjectDatatableDO.UPK();
        upk.setProjectId(PROJECT_ID);
        upk.setDatatableId(DATATABLE_ID);
        projectDatatableDO.setUpk(upk);
        projectDatatableDOS.add(projectDatatableDO);
        return projectDatatableDOS;
    }

    private List<ProjectDO> buildProjectDO() {
        List<ProjectDO> projectDOS = new ArrayList<>();
        ProjectDO projectDO = new ProjectDO();
        projectDO.setProjectId(PROJECT_ID);
        projectDOS.add(projectDO);
        return projectDOS;
    }
}