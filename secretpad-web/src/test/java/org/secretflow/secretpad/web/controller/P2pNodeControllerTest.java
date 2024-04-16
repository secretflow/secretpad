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
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.service.model.node.p2p.P2pCreateNodeRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;
import org.secretflow.secretpad.web.controller.p2p.P2pNodeController;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Node controller test of p2p mode
 *
 * @author wb-698356
 * @date 2024/01/04
 */
public class P2pNodeControllerTest extends ControllerTest {

    private static final String ALICE_NODE_ID = "alice";
    private static final String BOB_NODE_ID = "bob";

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private NodeRouteRepository nodeRouteRepository;

    @MockBean
    private DomainRouteServiceGrpc.DomainRouteServiceBlockingStub domainRouteServiceStub;

    @MockBean
    private DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub;

    private List<NodeDO> buildNodeDOList() {
        List<NodeDO> nodeDOList = new ArrayList<>();
        nodeDOList.add(NodeDO.builder().nodeId("alice").name("alice").description("alice").auth("alice").type("mpc").build());
        return nodeDOList;
    }

    private DomainOuterClass.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code)
                .build()).build();
    }

    private DomainOuterClass.BatchQueryDomainResponse buildBatchQueryDomainResponse(Integer code) {
        return DomainOuterClass.BatchQueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(DomainOuterClass.BatchQueryDomainResponseData.newBuilder().addAllDomains(List.of(DomainOuterClass.Domain.newBuilder().setDomainId(ALICE_NODE_ID)
                        .setCert("adasda").build()))).build();
    }

    private DomainOuterClass.CreateDomainResponse buildCreateDomainResponse(Integer code) {
        return DomainOuterClass.CreateDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainOuterClass.DeleteDomainResponse buildDeleteDomainResponse(Integer code) {
        return DomainOuterClass.DeleteDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.QueryDomainRouteResponse buildQueryDomainRouterResponse(Integer code) {
        return DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.CreateDomainRouteResponse buildCreateDomainRouteResponse(Integer code) {
        return DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.DeleteDomainRouteResponse buildDeleteDomainRouteResponse(Integer code) {
        return DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    @Test
    void createNode() throws Exception {
        assertResponse(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            UserContext.getUser().setPlatformNodeId(BOB_NODE_ID);

            request.setMode(1);
            request.setDstNodeId(ALICE_NODE_ID);
            request.setDstNetAddress("http://127.0.0.1:8080");
            request.setSrcNetAddress("http://127.0.0.1:8090");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(FakerUtils.fake(NodeDO.class));
            DomainOuterClass.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);

            DomainRoute.QueryDomainRouteResponse queryDomainRouteResponse = buildQueryDomainRouterResponse(0);
            Mockito.when(domainRouteServiceStub.queryDomainRoute(Mockito.any())).thenReturn(queryDomainRouteResponse);

            DomainRoute.CreateDomainRouteResponse createDomainRouteResponse = buildCreateDomainRouteResponse(0);
            Mockito.when(domainRouteServiceStub.createDomainRoute(Mockito.any())).thenReturn(createDomainRouteResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void createNodeByNodeRouteConfigException() throws Exception {
        assertErrorCode(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));

            request.setMode(1);
            request.setSrcNetAddress("http://127.0.0.1:80");
            request.setDstNetAddress("http://127.0.0.1:80");
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_CONFIG_ERROR);
    }

    @Test
    void createNodeByNodeCertConfigException() throws Exception {
        assertErrorCode(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            UserContext.getUser().setPlatformNodeId(ALICE_NODE_ID);

            request.setMode(1);
            request.setDstNodeId(ALICE_NODE_ID);
            request.setDstNetAddress("https://127.0.0.1:80");
            request.setSrcNetAddress("https://127.0.0.1:8090");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(batchQueryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_CERT_CONFIG_ERROR);
    }

    @Test
    void createNodeByNodeExistsInDbException() throws Exception {
        assertErrorCode(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            UserContext.getUser().setPlatformNodeId(BOB_NODE_ID);

            request.setMode(1);
            request.setDstNodeId(ALICE_NODE_ID);
            request.setDstNetAddress("https://127.0.0.1:80");
            request.setSrcNetAddress("https://127.0.0.1:8090");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findById(Mockito.any())).thenReturn(Optional.of(FakerUtils.fake(NodeDO.class)));
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_ALREADY_EXIST_ERROR);
    }

    @Test
    void createNodeByNodeCreateInKusciaException() throws Exception {
        assertErrorCode(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            UserContext.getUser().setPlatformNodeId(BOB_NODE_ID);

            request.setMode(1);
            request.setDstNodeId(ALICE_NODE_ID);
            request.setDstNetAddress("https://127.0.0.1:80");
            request.setSrcNetAddress("https://127.0.0.1:8090");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findDeletedRecordByNodeId(Mockito.any())).thenReturn(Optional.of(FakerUtils.fake(NodeDO.class)));
            DomainOuterClass.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(1);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_CREATE_ERROR);
    }

    @Test
    void createNodeByNodeRouteCreateInKusciaException() throws Exception {
        assertErrorCode(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            UserContext.getUser().setPlatformNodeId(BOB_NODE_ID);

            request.setMode(1);
            request.setDstNodeId(ALICE_NODE_ID);
            request.setDstNetAddress("https://127.0.0.1:80");
            request.setSrcNetAddress("https://127.0.0.1:8090");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(FakerUtils.fake(NodeDO.class));
            DomainOuterClass.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);

            DomainRoute.QueryDomainRouteResponse queryDomainRouteResponse = buildQueryDomainRouterResponse(0);
            Mockito.when(domainRouteServiceStub.queryDomainRoute(Mockito.any())).thenReturn(queryDomainRouteResponse);

            DomainRoute.CreateDomainRouteResponse createDomainRouteResponse = buildCreateDomainRouteResponse(1);
            Mockito.when(domainRouteServiceStub.createDomainRoute(Mockito.any())).thenReturn(createDomainRouteResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR);
    }

    @Test
    void createNodeExtractProtocolHostIPException() throws Exception {
        assertErrorCode(() -> {
            P2pCreateNodeRequest request = FakerUtils.fake(P2pCreateNodeRequest.class);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_CREATE));
            UserContext.getUser().setPlatformNodeId(BOB_NODE_ID);

            request.setMode(1);
            request.setDstNodeId(ALICE_NODE_ID);
            request.setDstNetAddress("http://127.0.0.1:80:80");
            request.setSrcNetAddress("http://127.0.0.1:8090:80");
            DomainOuterClass.BatchQueryDomainResponse batchQueryDomainResponse = buildBatchQueryDomainResponse(0);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(batchQueryDomainResponse);
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(FakerUtils.fake(NodeDO.class));
            DomainOuterClass.CreateDomainResponse createDomainResponse = buildCreateDomainResponse(0);
            Mockito.when(domainServiceStub.createDomain(Mockito.any())).thenReturn(createDomainResponse);

            DomainRoute.QueryDomainRouteResponse queryDomainRouteResponse = buildQueryDomainRouterResponse(0);
            Mockito.when(domainRouteServiceStub.queryDomainRoute(Mockito.any())).thenReturn(queryDomainRouteResponse);

            DomainRoute.CreateDomainRouteResponse createDomainRouteResponse = buildCreateDomainRouteResponse(1);
            Mockito.when(domainRouteServiceStub.createDomainRoute(Mockito.any())).thenReturn(createDomainRouteResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "createP2pNode", P2pCreateNodeRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR);
    }

    @Test
    void deleteNodeNotExistsInKuscia() throws Exception {
        assertResponseWithEmptyData(() -> {
            RouterIdRequest request = FakerUtils.fake(RouterIdRequest.class);
            request.setNodeId(ALICE_NODE_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));

            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.any())).thenReturn(FakerUtils.fake(NodeRouteDO.class));
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(buildNodeDOList());

            DomainRoute.DeleteDomainRouteResponse deleteDomainRouteResponse = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceStub.deleteDomainRoute(Mockito.any())).thenReturn(deleteDomainRouteResponse);

            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(1);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "deleteP2pNode", RouterIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            RouterIdRequest request = FakerUtils.fake(RouterIdRequest.class);
            request.setNodeId(ALICE_NODE_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));

            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.any())).thenReturn(FakerUtils.fake(NodeRouteDO.class));
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(buildNodeDOList());

            DomainRoute.DeleteDomainRouteResponse deleteDomainRouteResponse = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceStub.deleteDomainRoute(Mockito.any())).thenReturn(deleteDomainRouteResponse);

            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            DomainOuterClass.DeleteDomainResponse deleteDomainResponse = buildDeleteDomainResponse(0);
            Mockito.when(domainServiceStub.deleteDomain(Mockito.any())).thenReturn(deleteDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "deleteP2pNode", RouterIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteNodeByNodeRouteNotExistsException() throws Exception {
        assertErrorCode(() -> {
            RouterIdRequest request = FakerUtils.fake(RouterIdRequest.class);
            request.setNodeId(ALICE_NODE_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));

            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "deleteP2pNode", RouterIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR);
    }

    @Test
    void deleteNodeByNodeRouteDeleteInKusciaException() throws Exception {
        assertErrorCode(() -> {
            RouterIdRequest request = FakerUtils.fake(RouterIdRequest.class);
            request.setNodeId(ALICE_NODE_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));

            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.any())).thenReturn(FakerUtils.fake(NodeRouteDO.class));
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(buildNodeDOList());

            DomainRoute.DeleteDomainRouteResponse deleteDomainRouteResponse = buildDeleteDomainRouteResponse(1);
            Mockito.when(domainRouteServiceStub.deleteDomainRoute(Mockito.any())).thenReturn(deleteDomainRouteResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "deleteP2pNode", RouterIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_DELETE_ERROR);
    }

    @Test
    void deleteNodeByJobRunningException() throws Exception {
        assertErrorCode(() -> {
            RouterIdRequest request = FakerUtils.fake(RouterIdRequest.class);
            request.setNodeId(ALICE_NODE_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));

            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.any())).thenReturn(FakerUtils.fake(NodeRouteDO.class));
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(buildNodeDOList());

            DomainRoute.DeleteDomainRouteResponse deleteDomainRouteResponse = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceStub.deleteDomainRoute(Mockito.any())).thenReturn(deleteDomainRouteResponse);

            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            Mockito.when(projectNodeRepository.findByNodeId(Mockito.anyString())).thenReturn(List.of(FakerUtils.fake(ProjectNodeDO.class)));

            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "deleteP2pNode", RouterIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }

    @Test
    void deleteNodeByNodeDeleteInKusciaException() throws Exception {
        assertErrorCode(() -> {
            RouterIdRequest request = FakerUtils.fake(RouterIdRequest.class);
            request.setNodeId(ALICE_NODE_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_DELETE));

            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.any())).thenReturn(FakerUtils.fake(NodeRouteDO.class));
            Mockito.when(nodeRepository.findByType(Mockito.anyString())).thenReturn(buildNodeDOList());

            DomainRoute.DeleteDomainRouteResponse deleteDomainRouteResponse = buildDeleteDomainRouteResponse(0);
            Mockito.when(domainRouteServiceStub.deleteDomainRoute(Mockito.any())).thenReturn(deleteDomainRouteResponse);

            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(P2pNodeController.class, "deleteP2pNode", RouterIdRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }
}
