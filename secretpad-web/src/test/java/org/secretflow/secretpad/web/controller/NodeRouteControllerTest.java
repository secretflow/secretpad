package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.persistence.repository.ProjectResultRepository;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;
import org.secretflow.secretpad.service.util.DbSyncUtil;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @MockBean
    private ProjectResultRepository resultRepository;

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

            pushToCenterUtilMockedStatic.when(() -> DbSyncUtil.dbDataSyncToCenter(Mockito.any(VoteSyncRequest.class))).thenReturn(SecretPadResponse.success());
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

            pushToCenterUtilMockedStatic.when(() -> DbSyncUtil.dbDataSyncToCenter(Mockito.any(VoteSyncRequest.class))).thenReturn(SecretPadResponse.success());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR);
    }

    @Test
    void page() throws Exception {
        assertResponse(() -> {
            PageNodeRouteRequest pageNodeRouteRequest = FakerUtils.fake(PageNodeRouteRequest.class);
            pageNodeRouteRequest.setSort(null);
            pageNodeRouteRequest.setPage(1);
            pageNodeRouteRequest.setSize(100);
            Page<NodeRouteDO> page = new PageImpl<>(Lists.newArrayList());
            Mockito.when(nodeRouteRepository.pageQuery(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(page);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "page", PageNodeRouteRequest.class))
                    .content(JsonUtils.toJSONString(pageNodeRouteRequest));
        });
    }

    @Test
    void getByNodeNotExist() throws Exception {
        assertErrorCode(() -> {
            RouterIdRequest routerIdRequest = FakerUtils.fake(RouterIdRequest.class);
            NodeRouteDO info = new NodeRouteDO();
            NodeDO node = new NodeDO();
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(info);
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(node);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "get", RouterIdRequest.class))
                    .content(JsonUtils.toJSONString(routerIdRequest));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void listNode() throws Exception {
        assertResponse(() -> {
            Mockito.when(resultRepository.countByNodeId(Mockito.anyString())).thenReturn(1L);
            Mockito.when(domainServiceStub.batchQueryDomain(Mockito.any())).thenReturn(buildBatchQueryDomainResponse(0));
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "listNode"));
        });
    }

    @Test
    void refresh() throws Exception {
        assertErrorCode(() -> {
            RouterIdRequest routerIdRequest = FakerUtils.fake(RouterIdRequest.class);
            NodeRouteDO info = new NodeRouteDO();
            NodeDO node = new NodeDO();
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(info);
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(node);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "refresh", RouterIdRequest.class))
                    .content(JsonUtils.toJSONString(routerIdRequest));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void delete() throws Exception {
        assertResponseWithEmptyData(() -> {
            RouterIdRequest routerIdRequest = FakerUtils.fake(RouterIdRequest.class);
            NodeRouteDO info = NodeRouteDO.builder().routeId("a_b").srcNodeId("alice").dstNodeId("bob").build();
            NodeDO node = new NodeDO();
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(info);
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(node);
            Mockito.when(domainRouteServiceBlockingStub.deleteDomainRoute(Mockito.any())).thenReturn(buildDeleteDomainRouteResponse(0));

            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "delete", RouterIdRequest.class))
                    .content(JsonUtils.toJSONString(routerIdRequest));
        });
    }

    private UpdateNodeRouterRequest buildUpdateNodeRouterRequest() {
        return UpdateNodeRouterRequest.builder()
                .routerId("1")
                .srcNetAddress("http://127.0.0.1:8080")
                .dstNetAddress("http://127.0.0.1:8080")
                .build();
    }

    private NodeDO buildNodeDO() {
        return NodeDO.builder().nodeId("alice").netAddress("http://127.0.0.1:8080").build();
    }

    private Optional<NodeRouteDO> buildNodeRouteDO() {
        return Optional.ofNullable(NodeRouteDO.builder().srcNodeId("alice").dstNodeId("bob").srcNetAddress("http://127.0.0.1:8080")
                .dstNetAddress("http://127.0.0.1:8080").routeId("1").build());
    }

    private DomainRoute.CreateDomainRouteResponse buildCreateDomainRouteResponse(int code) {
        return DomainRoute.CreateDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainRoute.QueryDomainRouteResponse buildQueryDomainRouteResponse(int code) {
        return DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private DomainOuterClass.BatchQueryDomainResponse buildBatchQueryDomainResponse(int code) {
        return DomainOuterClass.BatchQueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }


    private DomainOuterClass.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(DomainOuterClass.QueryDomainResponseData.newBuilder()
                        .addNodeStatuses(DomainOuterClass.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()).build()).build())
                .build();
    }

    private DomainRoute.DeleteDomainRouteResponse buildDeleteDomainRouteResponse(int code) {
        return DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }
}