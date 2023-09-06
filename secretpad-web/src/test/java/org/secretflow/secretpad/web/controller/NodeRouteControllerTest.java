package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

/**
 * NodeRoute controller test
 *
 * @author yutu
 * @date 2023/8/31
 */
class NodeRouteControllerTest extends ControllerTest {

    @MockBean
    private NodeRepository nodeRepository;
    @MockBean
    private NodeRouteRepository nodeRouteRepository;

    @MockBean
    private DomainRouteServiceGrpc.DomainRouteServiceBlockingStub domainRouteServiceBlockingStub;

    @MockBean
    private DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub;


    @Test
    void create() throws Exception {
        assertResponse(() -> {
            CreateNodeRouterRequest request = buildCreateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildEmptyNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "create", CreateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void createByRouteExist() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRouterRequest request = buildCreateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "create", CreateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_ALREADY_EXISTS);
    }

    @Test
    void createByNodeNotReadyExist() throws Exception {
        assertErrorCode(() -> {
            CreateNodeRouterRequest request = buildCreateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(-1));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "create", CreateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR);
    }


    @Test
    void update() throws Exception {
        assertResponse(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void updateByRouteNotExist() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(null);
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR);
    }

    @Test
    void updateByNodeNotExist() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(null);
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyLong())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR);
    }

    private CreateNodeRouterRequest buildCreateNodeRouterRequest() {
        return CreateNodeRouterRequest.builder()
                .srcNodeId("alice")
                .dstNodeId("bob")
                .srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("http://127.0.0.1:8080")
                .routeType(DomainRouterConstants.DomainRouterTypeEnum.FullDuplex.name())
                .build();
    }

    private UpdateNodeRouterRequest buildUpdateNodeRouterRequest() {
        return UpdateNodeRouterRequest.builder()
                .routerId("1")
                .srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("http://127.0.0.1:8080")
                .build();
    }

    private NodeDO buildNodeDO() {
        return NodeDO.builder().nodeId("alice").build();
    }

    private Optional<NodeRouteDO> buildNodeRouteDO() {
        return Optional.ofNullable(NodeRouteDO.builder().srcNodeId("alice").dstNodeId("bob").srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("127.0.0.1:8080").id(1L).build());
    }

    private Optional<NodeRouteDO> buildEmptyNodeRouteDO() {
        return Optional.empty();
    }

    private DomainRoute.CreateDomainRouteResponse buildCreateDomainRouteResponse(int code) {
        return DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.QueryDomainRouteResponse buildQueryDomainRouteResponse(int code) {
        return DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domain.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return Domain.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(Domain.QueryDomainResponseData.newBuilder()
                        .addNodeStatuses(Domain.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()).build()).build())
                .build();
    }

}