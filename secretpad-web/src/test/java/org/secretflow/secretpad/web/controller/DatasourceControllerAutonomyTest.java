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

import org.secretflow.secretpad.common.errorcode.InstErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datasource.oss.AwsOssConfig;
import org.secretflow.secretpad.manager.integration.datasource.oss.OssAutoCloseableClient;
import org.secretflow.secretpad.manager.integration.datasource.oss.OssClientFactory;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.model.datasource.CreateDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.OssDatasourceInfo;
import org.secretflow.secretpad.service.util.HttpUtils;
import org.secretflow.secretpad.service.util.RateLimitUtil;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author chenmingliang
 * @date 2024/05/27
 */

@TestPropertySource(properties = {
        "secretpad.platform-type=AUTONOMY"
})
public class DatasourceControllerAutonomyTest extends ControllerTest {

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private OssClientFactory ossClientFactory;


    @MockBean
    private NodeRepository nodeRepository;

    @Test
    public void create() throws Exception {
        assertResponse(() -> {
            CreateDatasourceRequest request = new CreateDatasourceRequest();
            request.setType("OSS");
            request.setName("myOss");
            request.setOwnerId("nodeId");
            request.setNodeIds(Lists.newArrayList("nodeId"));

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

            Mockito.when(nodeRepository.findByInstId("nodeId")).thenReturn(Lists.newArrayList(NodeDO.builder().nodeId("nodeId").build()));
            Mockito.when(kusciaGrpcClientAdapter.createDomainDataSource(Mockito.any(), Mockito.anyString())).thenReturn(Domaindatasource.CreateDomainDataSourceResponse.newBuilder()
                    .setData(Domaindatasource.CreateDomainDataSourceResponseData.newBuilder()
                            .setDatasourceId("datasourceId")
                            .build())
                    .build());
            MockedStatic<HttpUtils> httpUtilsMockedStatic = Mockito.mockStatic(HttpUtils.class);
            httpUtilsMockedStatic.when(() -> HttpUtils.detection(Mockito.anyString())).thenReturn(true);
            mockedRateLimitUtil.when(RateLimitUtil::verifyRate).then(invocationOnMock -> true);
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "create", CreateDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void createFail() throws Exception {
        assertErrorCode(() -> {
            CreateDatasourceRequest request = new CreateDatasourceRequest();
            request.setType("OSS");
            request.setName("myOss");
            request.setOwnerId("nodeId");
            request.setNodeIds(Lists.newArrayList("nodeId"));

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

            Mockito.when(nodeRepository.findByInstId("instId")).thenReturn(Lists.newArrayList());
            Mockito.when(kusciaGrpcClientAdapter.createDomainDataSource(Mockito.any(), Mockito.anyString())).thenReturn(Domaindatasource.CreateDomainDataSourceResponse.newBuilder()
                    .setData(Domaindatasource.CreateDomainDataSourceResponseData.newBuilder()
                            .setDatasourceId("datasourceId")
                            .build())
                    .build());
            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "create", CreateDatasourceRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, InstErrorCode.INST_NOT_MATCH_NODE);
    }

}
