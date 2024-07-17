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
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.service.decorator.awsoss.AwsOssConfig;
import org.secretflow.secretpad.service.decorator.awsoss.OssAutoCloseableClient;
import org.secretflow.secretpad.service.factory.OssClientFactory;
import org.secretflow.secretpad.service.model.datasource.*;
import org.secretflow.secretpad.service.util.HttpUtils;
import org.secretflow.secretpad.service.util.RateLimitUtil;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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

    @Test
    public void create() throws Exception {
        assertResponse(() -> {
            CreateDatasourceRequest request = new CreateDatasourceRequest();
            request.setType("OSS");
            request.setName("myOss");
            request.setNodeId("nodeId");

            OssDatasourceInfo info = new OssDatasourceInfo();
            info.setAk("ak");
            info.setSk("sk");
            info.setEndpoint("https://127.0.0.1:8080");
            info.setPrefix("/prefix");
            info.setBucket("my-bucket");
            info.setVirtualhost(true);

            request.setDataSourceInfo(info);

            OssAutoCloseableClient ossAutoCloseableClient = Mockito.mock(OssAutoCloseableClient.class);
            Mockito.when(ossClientFactory.getOssClient(Mockito.any(AwsOssConfig.class))).thenReturn(ossAutoCloseableClient);
            Mockito.when(ossAutoCloseableClient.doesBucketExistV2(Mockito.anyString())).thenReturn(true);

            Mockito.when(kusciaGrpcClientAdapter.createDomainDataSource(Mockito.any())).thenReturn(Domaindatasource.CreateDomainDataSourceResponse.newBuilder()
                    .setData(Domaindatasource.CreateDomainDataSourceResponseData.newBuilder()
                            .setDatasourceId("datasourceId")
                            .build())
                    .build());
            MockedStatic<HttpUtils> httpUtilsMockedStatic = Mockito.mockStatic(HttpUtils.class);
            httpUtilsMockedStatic.when(() -> HttpUtils.detection(Mockito.anyString())).thenReturn(true);
            MockedStatic<RateLimitUtil> rateLimitUtilMockedStatic = Mockito.mockStatic(RateLimitUtil.class);
            rateLimitUtilMockedStatic.when(RateLimitUtil::verifyRate).then(invocationOnMock -> true);
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "create", CreateDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }


    @Test
    public void delete() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteDatasourceRequest deleteDatasourceRequest = new DeleteDatasourceRequest();
            deleteDatasourceRequest.setDatasourceId("datasourceId");
            deleteDatasourceRequest.setNodeId("nodeId");
            deleteDatasourceRequest.setType("OSS");

            FeatureTableDO featureTableDO = new FeatureTableDO();
            featureTableDO.setUpk(new FeatureTableDO.UPK("featureTableId", "nodeId", DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
            featureTableDO.setFeatureTableName("featureTableName");
            featureTableDO.setDesc("desc");
            featureTableDO.setType(DataSourceTypeEnum.HTTP.name());

            Mockito.when(featureTableRepository.findByNodeId(Mockito.anyString())).thenReturn(Lists.newArrayList(featureTableDO));
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
            listRequest.setNodeId("nodeId");
            listRequest.setStatus(Constants.STATUS_AVAILABLE);
            listRequest.setTypes(List.of(DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE));

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
            listRequest.setNodeId("nodeId");
            listRequest.setStatus(Constants.STATUS_AVAILABLE);
            listRequest.setTypes(List.of(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_TYPE));

            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any()))
                    .thenReturn(Domaindatasource.ListDomainDataSourceResponse.newBuilder()
                            .setData(Domaindatasource.DomainDataSourceList.newBuilder().addAllDatasourceList(List.of())
                                    .build()).build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "list", DatasourceListRequest.class))
                    .content(JsonUtils.toJSONString(listRequest));
        });
    }

    @Test
    public void detail() throws Exception {
        assertResponse(() -> {
            DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
            datasourceDetailRequest.setDatasourceId("datasourceId");
            datasourceDetailRequest.setNodeId("nodeId");
            datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE);

            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any()))
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
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "detail", DatasourceDetailRequest.class))
                    .content(JsonUtils.toJSONString(datasourceDetailRequest));
        });
    }


    @Test
    public void detailHttp() throws Exception {
        assertResponse(() -> {
            DatasourceDetailRequest datasourceDetailRequest = new DatasourceDetailRequest();
            datasourceDetailRequest.setDatasourceId("datasourceId");
            datasourceDetailRequest.setNodeId("nodeId");
            datasourceDetailRequest.setType(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_TYPE);

            Mockito.when(kusciaGrpcClientAdapter.queryDomainDataSource(Mockito.any()))
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
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "detail", DatasourceDetailRequest.class))
                    .content(JsonUtils.toJSONString(datasourceDetailRequest));
        });
    }

}
