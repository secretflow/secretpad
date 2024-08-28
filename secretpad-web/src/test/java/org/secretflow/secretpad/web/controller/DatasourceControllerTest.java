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

import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.factory.OssClientFactory;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailRequest;
import org.secretflow.secretpad.service.model.datasource.DatasourceListRequest;
import org.secretflow.secretpad.service.model.datasource.DatasourceNodesRequest;
import org.secretflow.secretpad.service.model.datasource.DeleteDatasourceRequest;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/05/27
 */
public class DatasourceControllerTest extends ControllerTest {

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private OssClientFactory ossClientFactory;

    @MockBean
    private AbstractDatatableManager datatableManager;

    @MockBean
    private FeatureTableRepository featureTableRepository;

    @MockBean
    private NodeRepository nodeRepository;


    @Test
    public void delete() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
            deleteDatasourceRequest.setDatasourceId("datasourceId");
            deleteDatasourceRequest.setOwnerId("nodeId");
            deleteDatasourceRequest.setType("OSS");

            FeatureTableDO featureTableDO = new FeatureTableDO();
            featureTableDO.setUpk(new FeatureTableDO.UPK("featureTableId", "nodeId", DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
            featureTableDO.setFeatureTableName("featureTableName");
            featureTableDO.setDesc("desc");
            featureTableDO.setType(DataSourceTypeEnum.HTTP.name());
            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any())).thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                    .setData(Domaindatasource.DomainDataSourceList.newBuilder().
                            addAllDatasourceList(Lists.newArrayList(Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId("datasourceId")
                                    .setDomainId("nodeId")
                                    .setType("oss")
                                    .setName("oss")
                                    .build()))
                            .build())
                    .build());
            Mockito.when(featureTableRepository.findByNodeId(Mockito.anyString())).thenReturn(Lists.newArrayList(featureTableDO));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(NodeDO.builder().nodeId("nodeId").build());
            DatatableDTO datatableDTO = DatatableDTO.builder().datasourceId("id").datatableName("name").type("oss").datatableId("od").build();
            Mockito.when(datatableManager.findByNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(Lists.newArrayList(datatableDTO));
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "delete", DeleteDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(deleteDatasourceRequest));
        });
    }

    @Test
    public void list() throws Exception {
        assertResponse(() -> {
            DatasourceListRequest listRequest = new DatasourceListRequest();
            listRequest.setName("name");
            listRequest.setOwnerId("nodeId");
            listRequest.setStatus(Constants.STATUS_AVAILABLE);
            listRequest.setTypes(List.of(DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(NodeDO.builder().nodeId("nodeId").build());

            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any()))
                    .thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                            .setData(Domaindatasource.DomainDataSourceList.newBuilder().addAllDatasourceList(List.of())
                                    .build()).build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "list", DatasourceListRequest.class))
                    .content(JsonUtils.toJSONString(listRequest));
        });
    }

    @Test
    public void listHttp() throws Exception {
        assertResponse(() -> {
            DatasourceListRequest listRequest = new DatasourceListRequest();
            listRequest.setName("name");
            listRequest.setOwnerId("nodeId");
            listRequest.setStatus(Constants.STATUS_AVAILABLE);
            listRequest.setTypes(List.of(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_TYPE));

            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any()))
                    .thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                            .setData(Domaindatasource.DomainDataSourceList.newBuilder().addAllDatasourceList(List.of())
                                    .build()).build());
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(NodeDO.builder().nodeId("nodeId").build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "list", DatasourceListRequest.class))
                    .content(JsonUtils.toJSONString(listRequest));
        });
    }

    @Test
    public void detail() throws Exception {
        assertResponse(() -> {
            DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
            datasourceDetailRequest.setDatasourceId("datasourceId");
            datasourceDetailRequest.setOwnerId("nodeId");
            datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE);
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(NodeDO.builder().nodeId("nodeId").build());

            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any(), Mockito.eq("nodeId")))
                    .thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
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
                            .build());

            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.anyString())).thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                    .setData(Domaindatasource.DomainDataSourceList.newBuilder().
                            addAllDatasourceList(Lists.newArrayList(Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId("datasourceId")
                                    .setDomainId("nodeId")
                                    .setType("oss")
                                    .setName("oss")
                                    .build()))
                            .build())
                    .build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "detail", DatasourceDetailRequest.class))
                    .content(JsonUtils.toJSONString(datasourceDetailRequest));
        });
    }


    @Test
    public void detailHttp() throws Exception {
        assertResponse(() -> {
            DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
            datasourceDetailRequest.setDatasourceId("http-data-source");
            datasourceDetailRequest.setOwnerId("nodeId");
            datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_TYPE);

            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(NodeDO.builder().nodeId("nodeId").build());

            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any()))
                    .thenReturn(Domaindatasource.QueryDomainDataSourceResponse.newBuilder()
                            .setData(Domaindatasource.DomainDataSource.newBuilder()
                                    .setDatasourceId("http-data-source")
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
                            .build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "detail", DatasourceDetailRequest.class))
                    .content(JsonUtils.toJSONString(datasourceDetailRequest));
        });
    }

    /**
     * center datasource nodes
     *
     * @throws Exception
     */
    @Test
    public void nodes() throws Exception {
        assertResponse(() -> {
            DatasourceNodesRequest datasourceNodesRequest = new DatasourceNodesRequest();
            datasourceNodesRequest.setOwnerId("nodeId");
            datasourceNodesRequest.setDatasourceId("datasourceId");
            NodeDO nodeDO = new NodeDO();
            nodeDO.setNodeId("nodeId");
            nodeDO.setName("alice");
            Mockito.when(nodeRepository.findByNodeId("nodeId")).thenReturn(nodeDO);
            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.anyString()))
                    .thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                            .setData(Domaindatasource.DomainDataSourceList.newBuilder().
                                    addAllDatasourceList(Lists.newArrayList(Domaindatasource.DomainDataSource.newBuilder()
                                            .setDatasourceId("datasourceId")
                                            .setDomainId("nodeId")
                                            .setType("oss")
                                            .setName("oss")
                                            .build()))
                                    .build())
                            .build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "nodes", DatasourceNodesRequest.class))
                    .content(JsonUtils.toJSONString(datasourceNodesRequest));
        });
    }

    /**
     * center datasource nodes DATA_SOURCE_NOT_FOUND
     *
     * @throws Exception
     */
    @Test
    void nodes_DATA_SOURCE_NOT_FOUND() throws Exception {
        assertErrorCode(() -> {
            DatasourceNodesRequest datasourceNodesRequest = new DatasourceNodesRequest();
            datasourceNodesRequest.setOwnerId("nodeId");
            datasourceNodesRequest.setDatasourceId("datasourceId");
            NodeDO nodeDO = new NodeDO();
            nodeDO.setNodeId("nodeId");
            nodeDO.setName("alice");
            Mockito.when(nodeRepository.findByNodeId("nodeId")).thenReturn(nodeDO);
            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any()))
                    .thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                            .setData(Domaindatasource.DomainDataSourceList.newBuilder().
                                    addAllDatasourceList(Lists.newArrayList(Domaindatasource.DomainDataSource.newBuilder()
                                            .setDatasourceId("datasourceId")
                                            .setDomainId("nodeId")
                                            .setType("oss")
                                            .setName("oss")
                                            .build()))
                                    .build())
                            .build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "nodes", DatasourceNodesRequest.class))
                    .content(JsonUtils.toJSONString(datasourceNodesRequest));
        }, DatasourceErrorCode.DATA_SOURCE_NOT_FOUND);
    }

}
