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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author lufeng
 * @date 2024/8/7
 */
@ExtendWith(MockitoExtension.class)
public class NodeRouteManagerTest {
    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @InjectMocks
    private NodeRouteManager nodeRouteManager;

    @Mock
    private NodeRouteRepository nodeRouteRepository;

    @Mock
    private SecretpadException secretpadException;


    /**
     * test deleteNodeRouteInKuscia
     */

    @Test
    public void testDeleteNodeRouteInKuscia_NormalCase() {
        when(kusciaGrpcClientAdapter.deleteDomainRoute(any(), any())).thenReturn(DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());

        nodeRouteManager.deleteNodeRouteInKuscia("sourceNodeId", "dstNodeId", "channelNodeId");

        verify(kusciaGrpcClientAdapter, times(1)).deleteDomainRoute(any(), any());
    }

    /**
     * test deleteNodeRouteInKuscia FailureCase
     */
    @Test
    public void testDeleteNodeRouteInKuscia_FailureCase() {
        when(kusciaGrpcClientAdapter.deleteDomainRoute(any(), any())).thenReturn(DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(1)).build());

        try {
            nodeRouteManager.deleteNodeRouteInKuscia("sourceNodeId", "dstNodeId", "channelNodeId");
        } catch (SecretpadException e) {
            assertEquals(NodeRouteErrorCode.NODE_ROUTE_DELETE_ERROR, e.getErrorCode());
        }

        verify(kusciaGrpcClientAdapter, times(1)).deleteDomainRoute(any(), any());

    }

    /**
     * test createNodeRouteInKuscia
     */
    @Test
    public void testCreateNodeRouteInKuscia_NormalCase() {
        CreateNodeRouteParam createNodeRouteParam = CreateNodeRouteParam.builder()
                .srcNodeId("your_src_node_id")
                .dstNodeId("your_dst_node_id")
                .srcNetAddress("your_src_net_address")
                .dstNetAddress("your_dst_net_address")
                .build();
        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        when(kusciaGrpcClientAdapter.createDomainRoute(any())).thenReturn(DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());
        when(kusciaGrpcClientAdapter.queryDomainRoute(any())).thenReturn(response);
        nodeRouteManager.createNodeRouteInKuscia(createNodeRouteParam, srcNode, dstNode, false);

        verify(kusciaGrpcClientAdapter, times(1)).createDomainRoute(any());
    }

    /**
     * test creating node route using channel node id
     */
    @Test
    public void testCreateNodeRouteWithChannelNodeId() {
        CreateNodeRouteParam createNodeRouteParam = CreateNodeRouteParam.builder()
                .srcNodeId("your_src_node_id")
                .dstNodeId("your_dst_node_id")
                .srcNetAddress("your_src_net_address")
                .dstNetAddress("your_dst_net_address")
                .build();

        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();

        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();

        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        // SET THE PLATFORM TYPE TO AUTONOMY
        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "AUTONOMY");
        // When the createDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(kusciaGrpcClientAdapter.createDomainRoute(any(), any())).thenReturn(DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());
        // When the queryDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(kusciaGrpcClientAdapter.queryDomainRoute(any(), any())).thenReturn(response);
        nodeRouteManager.createNodeRouteInKuscia(createNodeRouteParam, srcNode, dstNode, false);

        verify(kusciaGrpcClientAdapter, times(1)).createDomainRoute(any(), any());
    }

    /**
     * test creating node route using channel node id and check is true
     */
    @Test
    public void testCreateNodeRouteWithChannelNodeIdAndCheckIsTrue() {
        CreateNodeRouteParam createNodeRouteParam = CreateNodeRouteParam.builder()
                .srcNodeId("your_src_node_id")
                .dstNodeId("your_dst_node_id")
                .srcNetAddress("your_src_net_address")
                .dstNetAddress("your_dst_net_address")
                .build();

        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        // SET THE PLATFORM TYPE TO AUTONOMY
        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "AUTONOMY");
        // When the createDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(kusciaGrpcClientAdapter.createDomainRoute(any(), any())).thenReturn(DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());
        // When the queryDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(kusciaGrpcClientAdapter.queryDomainRoute(any(), any())).thenReturn(response);
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.empty());
        nodeRouteManager.createNodeRouteInKuscia(createNodeRouteParam, srcNode, dstNode, true);

        verify(kusciaGrpcClientAdapter, times(1)).createDomainRoute(any(), any());
    }




    /**
     * test createNodeRouteInKuscia with !routeExist
     */
    @Test
    public void testCreateNodeRouteInKuscia_withRouteExist() {
        CreateNodeRouteParam createNodeRouteParam = CreateNodeRouteParam.builder()
                .srcNodeId("your_src_node_id")
                .dstNodeId("your_dst_node_id")
                .srcNetAddress("your_src_net_address")
                .dstNetAddress("your_dst_net_address")
                .routeType(DomainRouterConstants.DomainRouterTypeEnum.FullDuplex.name())
                .build();
        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        NodeRouteDO nodeRouteDO = NodeRouteDO.builder()
                .routeId("test").build();
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.of(nodeRouteDO));
        when(kusciaGrpcClientAdapter.createDomainRoute(any())).thenReturn(DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());
        when(kusciaGrpcClientAdapter.queryDomainRoute(any())).thenReturn(response);
        nodeRouteManager.createNodeRouteInKuscia(createNodeRouteParam, srcNode, dstNode, false);

        verify(kusciaGrpcClientAdapter, times(1)).createDomainRoute(any());
    }

    /**
     * test createNodeRouteInKuscia with routeExist throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_ALREADY_EXISTS,
     */

    @Test
    public void testCreateNodeRouteInKuscia_withRouteExist_throwSecretpadException() {
        CreateNodeRouteParam createNodeRouteParam = CreateNodeRouteParam.builder()
                .srcNodeId("your_src_node_id")
                .dstNodeId("your_dst_node_id")
                .srcNetAddress("your_src_net_address")
                .dstNetAddress("your_dst_net_address")
                .routeType(DomainRouterConstants.DomainRouterTypeEnum.FullDuplex.name())
                .build();
        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();

        NodeRouteDO nodeRouteDO = NodeRouteDO.builder()
                .routeId("test").build();
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.of(nodeRouteDO));
        try {
            nodeRouteManager.createNodeRouteInKuscia(createNodeRouteParam, srcNode, dstNode, true);
        } catch (SecretpadException e) {
            Assertions.assertEquals(NodeRouteErrorCode.NODE_ROUTE_ALREADY_EXISTS, e.getErrorCode());
        }
    }

    /**
     * test createNodeRouteInKuscia with routeNotExist
     */
    @Test
    public void testCreateNodeRouteInKuscia_withRouteNotExist() {
        CreateNodeRouteParam createNodeRouteParam = CreateNodeRouteParam.builder()
                .srcNodeId("your_src_node_id")
                .dstNodeId("your_dst_node_id")
                .srcNetAddress("your_src_net_address")
                .dstNetAddress("your_dst_net_address")
                .routeType(DomainRouterConstants.DomainRouterTypeEnum.FullDuplex.name())
                .build();
        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.empty());
        when(kusciaGrpcClientAdapter.createDomainRoute(any())).thenReturn(DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());
        when(kusciaGrpcClientAdapter.queryDomainRoute(any())).thenReturn(response);
        nodeRouteManager.createNodeRouteInKuscia(createNodeRouteParam, srcNode, dstNode, false);

        verify(kusciaGrpcClientAdapter, times(2)).createDomainRoute(any());
    }

    @Test
    public void testDeleteNodeRouteRouteIdNotExist() {
        // prepareTestData
        String nodeRouteId = "7";
        // simulateTheBehaviorOf NodeRouteRepository
        when(nodeRouteRepository.findByRouteId(nodeRouteId)).thenReturn(null);

        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "CENTER");

        // call TheMethodUnderTest
        try {
            nodeRouteManager.deleteNodeRoute(nodeRouteId);
        } catch (SecretpadException e) {
            Assertions.assertEquals(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR, e.getErrorCode());
        }

    }

    /**
     * Unit test for deleting a node route.
     */
    @Test
    public void testDeleteNodeRoute() {
        // prepare_test_data
        String nodeRouteId = "7";
        NodeRouteDO nodeRouteDO = new NodeRouteDO();
        nodeRouteDO.setRouteId(nodeRouteId);
        nodeRouteDO.setSrcNodeId("srcNodeId");
        nodeRouteDO.setDstNodeId("dstNodeId");


        // Mock the behavior of nodeRouteRepository.findByRouteId()
        when(nodeRouteRepository.findByRouteId(nodeRouteId)).thenReturn(nodeRouteDO);

        // Set the value of platformType field in nodeRouteManager
        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "CENTER");

        // Create a DeleteDomainRouteResponse object for mocking kusciaGrpcClientAdapter.deleteDomainRoute()
        DomainRoute.DeleteDomainRouteResponse response = DomainRoute.DeleteDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(1).build()).build();

        // Mock the behavior of kusciaGrpcClientAdapter.deleteDomainRoute()
        when(kusciaGrpcClientAdapter.deleteDomainRoute(any())).thenReturn(response);

        // Call the method to be tested
        try {
            nodeRouteManager.deleteNodeRoute(nodeRouteId);
        } catch (SecretpadException e) {
            // Assert the error code of the thrown exception
            Assertions.assertEquals(NodeRouteErrorCode.NODE_ROUTE_DELETE_ERROR, e.getErrorCode());
        }


    }

    @Test
    public void testDeleteNodeRouteDeleteSuccess() {
        // Prepare test data
        String nodeRouteId = "7";
        NodeRouteDO nodeRouteDO = new NodeRouteDO();
        nodeRouteDO.setRouteId(nodeRouteId);
        nodeRouteDO.setSrcNodeId("srcNodeId");
        nodeRouteDO.setDstNodeId("dstNodeId");

        // Simulate the behavior of NodeRouteRepository
        when(nodeRouteRepository.findByRouteId(nodeRouteId)).thenReturn(nodeRouteDO);
        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "CENTER");
        // Simulate the behavior of DomainRoute.DeleteDomainRouteResponse
        DomainRoute.DeleteDomainRouteResponse response = DomainRoute.DeleteDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder()
                        .setCode(11404).build()).build();

        // Simulate the behavior of kusciaGrpcClientAdapter.deleteDomainRoute
        when(kusciaGrpcClientAdapter.deleteDomainRoute(any())).thenReturn(response);

        // Call the method to be tested
        nodeRouteManager.deleteNodeRoute(nodeRouteId);

        // Verify the behavior of NodeRouteRepository
        verify(nodeRouteRepository, times(2)).deleteById(nodeRouteDO.getRouteId());
        verify(nodeRouteRepository).flush();

    }

    /**
     * test updateNodeRoute
     */
    @Test
    public void testUpdateNodeRoute() {
        // prepare_test_data
        String nodeRouteId = "7";
        NodeRouteDO nodeRouteDO = new NodeRouteDO();
        nodeRouteDO.setRouteId(nodeRouteId);
        nodeRouteDO.setSrcNodeId("srcNodeId");
        nodeRouteDO.setDstNodeId("dstNodeId");
        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.of(nodeRouteDO));
        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        // SET THE PLATFORM TYPE TO AUTONOMY
        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "AUTONOMY");
        // When the createDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(kusciaGrpcClientAdapter.createDomainRoute(any(), any())).thenReturn(DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());
        // When the queryDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(kusciaGrpcClientAdapter.queryDomainRoute(any(), any())).thenReturn(response);
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.of(nodeRouteDO));

        nodeRouteManager.updateNodeRoute(nodeRouteDO, srcNode, dstNode);
    }
    /**
     * test updateNodeRoute with route not exist
     */
    @Test
    public void testUpdateNodeRouteRouteNotExist() {
        // prepare_test_data
        String nodeRouteId = "7";
        NodeRouteDO nodeRouteDO = new NodeRouteDO();
        nodeRouteDO.setRouteId(nodeRouteId);
        nodeRouteDO.setSrcNodeId("srcNodeId");
        nodeRouteDO.setDstNodeId("dstNodeId");
        NodeDO srcNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test srcNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("bob")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8080")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        NodeDO dstNode = NodeDO.builder()
                .nodeId("1234567890")
                .name("Test dstNode")
                .auth("test-auth")
                .description("This is a test node.")
                .masterNodeId("master-node-id")
                .instId("alice")
                .instToken("inst-token")
                .protocol("http")
                .controlNodeId("control-node-id")
                .netAddress("http://172.0.0.1:8081")
                .token("token")
                .type("type")
                .mode(1)
                .build();
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.empty());
        DomainRoute.QueryDomainRouteResponse response = DomainRoute.QueryDomainRouteResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0).build())
                .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder()
                        .setStatus(DomainRoute.RouteStatus.newBuilder()
                                .setStatus("0").build())
                        .setEndpoint(DomainRoute.RouteEndpoint.newBuilder()
                                .setHost("testhost")
                                .addPorts(DomainRoute.EndpointPort.newBuilder()
                                        .setName("test")
                                        .setPort(123)
                                        .setProtocol("testsss")
                                        .setIsTLS(false).build()).build())
                        .setName("testname")
                        .setDestination("1")
                        .setSource("testsource")
                        .build())
                .build();
        // SET THE PLATFORM TYPE TO AUTONOMY
        ReflectionTestUtils.setField(nodeRouteManager, "platformType", "AUTONOMY");
        // When the createDomainRoute method of kusciaGrpcClientAdapter is called, the specified response is returned
        when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(any(), any())).thenReturn(Optional.empty());

        Assertions.assertThrows(SecretpadException.class, () -> nodeRouteManager.updateNodeRoute(nodeRouteDO, srcNode, dstNode));
    }
}