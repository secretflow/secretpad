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
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.decorator.awsoss.OssAutoCloseableClient;
import org.secretflow.secretpad.service.factory.OssClientFactory;
import org.secretflow.secretpad.service.model.datatable.CreateDatatableRequest;
import org.secretflow.secretpad.service.model.datatable.DeleteDatatableRequest;
import org.secretflow.secretpad.service.model.datatable.GetDatatableRequest;
import org.secretflow.secretpad.service.model.datatable.ListDatatableRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_TYPE_TABLE;
import static org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager.DATA_VENDOR_MANUAL;

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


    /**
     * createDatable test
     *
     * @throws Exception
     */
    @Test
    public void createDatable() throws Exception {
        CreateDatatableRequest request = FakerUtils.fake(CreateDatatableRequest.class);
        request.setDatasourceType("OSS");
        request.setDatasourceName("ossDatasource");
        request.setOwnerId("alice");
        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_CREATE));
        Domaindata.CreateDomainDataResponse response = Domaindata.CreateDomainDataResponse.newBuilder()
                .setData(Domaindata.CreateDomainDataResponseData.newBuilder()
                        .setDomaindataId(request.getOwnerId())
                        .build())
                .build();
        Mockito.when(kusciaGrpcClientAdapter.createDomainData(
                Mockito.any())).thenReturn(response);
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any())).thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build());

        // Act & Assert
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "createDataTable", CreateDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }


    @Test
    void listDatatables() throws Exception {
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
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");

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
        assertResponse(() -> {
            GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
            request.setNodeId("alice");
            request.setType("CSV");

            NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
            nodeDO.setNodeId("alice");
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

            Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                    .setData(
                            Domaindata.DomainData.newBuilder().putAttributes("DatasourceType", "OSS")
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
                            .setDomainId(response.getData().getDatasourceId())
                            .setDatasourceId(response.getData().getDatasourceId())
                            .build())
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
    void getOSSDatatableAwsFalse() throws Exception {

        GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
        request.setNodeId("alice");
        request.setType("CSV");

        NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
        nodeDO.setNodeId("alice");
        Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

        Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                .setData(
                        Domaindata.DomainData.newBuilder()
                                .setType("OSS")
                                .setRelativeUri("alice.csv")
                                .build()
                )
                .build();

        Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(
                        Domaindatasource.DomainDataSource.newBuilder()
                                .setDatasourceId(response.getData().getDatasourceId())
                                .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setOss(Domaindatasource.OssDataSourceInfo.newBuilder()
                                        .setAccessKeySecret("XXXX")
                                        .setEndpoint("http://127.0.0.1:9000/")
                                        .setAccessKeyId("XXXX")
                                        .setBucket("alice")
                                        .build()))
                                .build()
                )
                .build();
        Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                        .setDomainId(request.getNodeId())
                        .setDomaindataId(request.getDatatableId())
                        .build())
                .build())).thenReturn(response);
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDatasourceId(response.getData().getDatasourceId())
                .setDatasourceId(response.getData().getDatasourceId())
                .build())).thenReturn(datasourceResponse);
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class))
                    .content(JsonUtils.toJSONString(request));

        });

    }


    @Test
    void getOSSDatatableAwsTrue() throws Exception {

        GetDatatableRequest request = FakerUtils.fake(GetDatatableRequest.class);
        request.setNodeId("alice");
        request.setType("CSV");

        NodeDO nodeDO = FakerUtils.fake(NodeDO.class);
        nodeDO.setNodeId("alice");
        Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(nodeDO);

        UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.DATATABLE_GET));

        Domaindata.QueryDomainDataResponse response = Domaindata.QueryDomainDataResponse.newBuilder()
                .setData(
                        Domaindata.DomainData.newBuilder()
                                .setType("OSS")
                                .setRelativeUri("alice.csv")
                                .build()
                )
                .build();

        Domaindatasource.QueryDomainDataSourceResponse datasourceResponse = Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(
                        Domaindatasource.DomainDataSource.newBuilder()
                                .setDatasourceId(response.getData().getDatasourceId())
                                .setInfo(Domaindatasource.DataSourceInfo.newBuilder().setOss(Domaindatasource.OssDataSourceInfo.newBuilder()
                                        .setAccessKeySecret("XXXXX")
                                        .setEndpoint("http://127.0.0.1:9000/")
                                        .setAccessKeyId("XXX")
                                        .setBucket("alice")
                                        .build()))
                                .build()
                )
                .build();
        Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest.newBuilder()
                .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                        .setDomainId(request.getNodeId())
                        .setDomaindataId(request.getDatatableId())
                        .build())
                .build())).thenReturn(response);
        Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDatasourceId(response.getData().getDatasourceId())
                .setDatasourceId(response.getData().getDatasourceId())
                .build())).thenReturn(datasourceResponse);
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(DatatableController.class, "getDatatable", GetDatatableRequest.class)).content(JsonUtils.toJSONString(request));
        });

    }

    @Test
    void deleteDatatable() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setType("CSV");
            request.setNodeId("alice");

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

    @Test
    void deleteDatatableHasBeenAuthException() throws Exception {
        assertErrorCode(() -> {
            DeleteDatatableRequest request = FakerUtils.fake(DeleteDatatableRequest.class);
            request.setType("CSV");
            request.setNodeId("alice");

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