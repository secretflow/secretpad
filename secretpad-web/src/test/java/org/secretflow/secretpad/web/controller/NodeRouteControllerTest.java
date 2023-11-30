package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;
import org.secretflow.secretpad.service.util.PushToCenterUtil;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;
import java.util.Set;

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
    void update() throws Exception {
        assertResponse(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_ROUTE_UPDATE));
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));

            pushToCenterUtilMockedStatic.when(() -> PushToCenterUtil.dataPushToCenter(Mockito.any(VoteSyncRequest.class))).thenReturn(SecretPadResponse.success());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void updateByRouteNotExist() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_ROUTE_UPDATE));
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(null);
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(domainRouteServiceBlockingStub.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(domainRouteServiceBlockingStub.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(domainServiceStub.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));

            pushToCenterUtilMockedStatic.when(() -> PushToCenterUtil.dataPushToCenter(Mockito.any(VoteSyncRequest.class))).thenReturn(SecretPadResponse.success());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR);
    }

    private UpdateNodeRouterRequest buildUpdateNodeRouterRequest() {
        return UpdateNodeRouterRequest.builder()
                .routerId("1")
                .srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("http://127.0.0.1:8080")
                .build();
    }

    private NodeDO buildNodeDO() {
        return NodeDO.builder().nodeId("alice").netAddress("127.0.0.1:8080").build();
    }

    private Optional<NodeRouteDO> buildNodeRouteDO() {
        return Optional.ofNullable(NodeRouteDO.builder().srcNodeId("alice").dstNodeId("bob").srcNetAddress("127.0.0.1:8080")
                .dstNetAddress("127.0.0.1:8080").routeId("1").build());
    }

    private DomainRoute.CreateDomainRouteResponse buildCreateDomainRouteResponse(int code) {
        return DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.QueryDomainRouteResponse buildQueryDomainRouteResponse(int code) {
        return DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainOuterClass.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(DomainOuterClass.QueryDomainResponseData.newBuilder()
                        .addNodeStatuses(DomainOuterClass.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()).build()).build())
                .build();
    }

}