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
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.InstService;
import org.secretflow.secretpad.service.model.datasource.DatasourceNodesRequest;
import org.secretflow.secretpad.service.model.node.NodeVO;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

/**
 * @author guanxi
 * @date 2024/07/11
 */
@TestPropertySource(properties = {
        "secretpad.gateway=127.0.0.1:9001",
        "secretpad.datasync.p2p=true",
        "secretpad.datasync.center=false",
        "secretpad.platform-type=AUTONOMY"
})
public class P2PDatasourceControllerTest extends ControllerTest {


    @MockBean
    private NodeRepository nodeRepository;


    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private InstService instService;


    /**
     * nodes test in p2p
     *
     * @throws Exception
     */
    @Test
    public void nodes_oss() throws Exception {
        assertResponse(() -> {
            DatasourceNodesRequest datasourceNodesRequest = new DatasourceNodesRequest();
            datasourceNodesRequest.setOwnerId("nodeId");
            datasourceNodesRequest.setDatasourceId("http-data-source");
            NodeDO alice = NodeDO.builder().nodeId("alice").instId("nodeId").name("alice").build();
            Mockito.when(nodeRepository.findByInstId("nodeId")).thenReturn(List.of(alice));
            Mockito.when(instService.listNode()).thenReturn(List.of(NodeVO.builder().nodeId("alice").nodeStatus(Constants.STATUS_UNAVAILABLE).build()));
            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any(), Mockito.any()))
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
     * p2p datasource nodes DATA_SOURCE_NOT_FOUND
     *
     * @throws Exception
     */
    @Test
    void nodes_oss_ATA_SOURCE_NOT_FOUND() throws Exception {
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

    @Test
    void nodes_http_ATA_SOURCE_NOT_FOUND() throws Exception {
        assertErrorCode(() -> {
            DatasourceNodesRequest datasourceNodesRequest = new DatasourceNodesRequest();
            datasourceNodesRequest.setOwnerId("nodeId");
            datasourceNodesRequest.setDatasourceId("http-data-source");
            NodeDO alice = NodeDO.builder().nodeId("alice").instId("nodeId").name("alice").build();
            Mockito.when(nodeRepository.findByInstId("nodeId")).thenReturn(List.of(alice));

            return MockMvcRequestBuilders.post(getMappingUrl(DataSourceController.class, "nodes", DatasourceNodesRequest.class))
                    .content(JsonUtils.toJSONString(datasourceNodesRequest));
        }, DatasourceErrorCode.DATA_SOURCE_NOT_FOUND);
    }

}
