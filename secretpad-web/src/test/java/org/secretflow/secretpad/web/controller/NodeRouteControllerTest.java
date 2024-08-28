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

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.impl.EnvServiceImpl;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;
import org.secretflow.secretpad.service.util.DbSyncUtil;
import org.secretflow.secretpad.web.utils.FakerUtils;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;
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
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private ProjectResultRepository resultRepository;
    @MockBean
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;
    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private EnvServiceImpl envService;

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    @MockBean
    private NodeRouteManager nodeRouteManager;

    @BeforeEach
    void set() {
        Mockito.when(envService.isAutonomy()).thenReturn(true);
        Mockito.when(envService.getPlatformType()).thenReturn(PlatformTypeEnum.TEST);
    }

    @Test
    void update() throws Exception {
        assertResponse(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_ROUTE_UPDATE));
            Mockito.when(nodeRepository.findByNodeId(Mockito.any())).thenReturn(buildNodeDO());
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(kusciaGrpcClientAdapter.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any(), Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            pushToCenterUtilMockedStatic.when(() -> DbSyncUtil.dbDataSyncToCenter(Mockito.any(VoteSyncRequest.class))).thenReturn(SecretPadResponse.success());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void updateByProhibitUpdate() throws Exception {
        assertErrorCode(() -> {
            UpdateNodeRouterRequest request = buildUpdateNodeRouterRequest();
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.NODE_ROUTE_UPDATE));
            ProjectJobDO projectJobDO = ProjectJobDO.builder()
                    .upk(new ProjectJobDO.UPK("projectId", "jobId"))
                    .status(GraphJobStatus.RUNNING)
                    .build();
            NodeDO alice = NodeDO.builder().nodeId("alice").instId("alice-inst").build();
            NodeDO bob = NodeDO.builder().nodeId("bob").instId("bob-inst").build();
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setParties(Lists.newArrayList("alice -inst", "bob -inst", "carol -inst"));
            projectApprovalConfigDO.setProjectId(PROJECT_ID);
            projectApprovalConfigDO.setParticipantNodeInfo(
                    List.of(new ParticipantNodeInstVO("alice", "alice1name", List.of(new ParticipantNodeInstVO.NodeInstVO("bob", "bobname", "bob -inst", "bob -inst -name"))),
                            new ParticipantNodeInstVO("alice2", "alice2name", List.of(new ParticipantNodeInstVO.NodeInstVO("carol", "bobname", "carol -inst", "carol -inst -name")))));
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(alice);
            Mockito.when(nodeRepository.findByNodeId("bob")).thenReturn(bob);

            Mockito.when(projectApprovalConfigRepository.findByInitiator(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of(projectApprovalConfigDO));
            Mockito.when(envService.isAutonomy()).thenReturn(true);
            Mockito.when(projectJobRepository.findByProjectIds(Mockito.anyList()))
                    .thenReturn(List.of(projectJobDO));
            Mockito.when(projectJobRepository.findStatusByJobIds(Mockito.anyList()))
                    .thenReturn(List.of(GraphJobStatus.RUNNING));

            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.save(Mockito.any())).thenReturn(buildNodeRouteDO().get());
            Mockito.when(nodeRouteRepository.findBySrcNodeIdAndDstNodeId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildNodeRouteDO());
            Mockito.when(kusciaGrpcClientAdapter.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));

            pushToCenterUtilMockedStatic.when(() -> DbSyncUtil.dbDataSyncToCenter(Mockito.any(VoteSyncRequest.class))).thenReturn(SecretPadResponse.success());
            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "update", UpdateNodeRouterRequest.class)).
                    content(JsonUtils.toJSONString(request));

        }, NodeRouteErrorCode.NODE_ROUTE_UPDATE_ERROR);
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
            Mockito.when(kusciaGrpcClientAdapter.createDomainRoute(Mockito.any())).thenReturn(buildCreateDomainRouteResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any())).thenReturn(buildQueryDomainRouteResponse(0));
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(buildQueryDomainResponse(0));

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
            Mockito.when(nodeRouteRepository.pageQuery(Mockito.anyCollection(), Mockito.anyString(), Mockito.any())).thenReturn(page);

            return MockMvcRequestBuilders.post(getMappingUrl(NodeRouteController.class, "page", PageNodeRouteRequest.class))
                    .content(JsonUtils.toJSONString(pageNodeRouteRequest));
        });
    }

    @Test
    void pageWithJobRunning() throws Exception {
        assertResponse(() -> {
            PageNodeRouteRequest pageNodeRouteRequest = FakerUtils.fake(PageNodeRouteRequest.class);
            pageNodeRouteRequest.setSort(null);
            pageNodeRouteRequest.setPage(1);
            pageNodeRouteRequest.setSize(100);
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("a1");
            nodeRouteDO.setDstNodeId("b1");
            Page<NodeRouteDO> page = new PageImpl<>(List.of(nodeRouteDO));

            List<ProjectNodeDO> projectNodeDOList = new ArrayList<>();
            ProjectNodeDO a1 = new ProjectNodeDO();
            a1.setUpk(new ProjectNodeDO.UPK("project","a1"));
            ProjectNodeDO b1 = new ProjectNodeDO();
            b1.setUpk(new ProjectNodeDO.UPK("project","b1"));

            projectNodeDOList.add(a1);
            projectNodeDOList.add(b1);

            NodeDO a1Node  =  new NodeDO();
            a1Node.setNodeId("a1");
            NodeDO b1Node  =  new NodeDO();
            b1Node.setNodeId("b1");
            List<String> nodeIds = List.of("a1","b1");

            Mockito.when(nodeRouteRepository.pageQuery(Mockito.anyCollection(), Mockito.anyString(), Mockito.any())).thenReturn(page);
            Mockito.when(projectNodeRepository.findByNodeIds(Mockito.anyList())).thenReturn(projectNodeDOList);
            Mockito.when(nodeRepository.findByNodeId("a1")).thenReturn(a1Node);
            Mockito.when(nodeRepository.findByNodeId("b1")).thenReturn(b1Node);
            DomainRoute.RouteStatus status = DomainRoute.RouteStatus.newBuilder().setStatus("Ready").build();
            Mockito.when(nodeRouteManager.getRouteStatus(Mockito.any(),Mockito.any(),Mockito.any())).thenReturn(status);
            Mockito.when(projectNodeRepository.findProjectNodesByProjectId(Mockito.any())).thenReturn(nodeIds);
            DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(queryDomainResponse);

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
            Mockito.when(kusciaGrpcClientAdapter.batchQueryDomain(Mockito.any())).thenReturn(buildBatchQueryDomainResponse(0));
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
            NodeDO alice = NodeDO.builder().nodeId("alice").instId("alice-inst").build();
            NodeDO bob = NodeDO.builder().nodeId("bob").instId("bob-inst").build();
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setParties(Lists.newArrayList("alice -inst", "bob -inst", "carol -inst"));
            projectApprovalConfigDO.setProjectId(PROJECT_ID);
            projectApprovalConfigDO.setParticipantNodeInfo(
                    List.of(new ParticipantNodeInstVO("alice", "alice1name", List.of(new ParticipantNodeInstVO.NodeInstVO("bob", "bobname", "bob -inst", "bob -inst -name"))),
                            new ParticipantNodeInstVO("alice2", "alice2name", List.of(new ParticipantNodeInstVO.NodeInstVO("carol", "bobname", "carol -inst", "carol -inst -name")))));
            Mockito.when(nodeRouteRepository.findByRouteId(Mockito.anyString())).thenReturn(info);
            Mockito.when(nodeRepository.findByNodeId("alice")).thenReturn(alice);
            Mockito.when(nodeRepository.findByNodeId("bob")).thenReturn(bob);
            Mockito.when(projectApprovalConfigRepository.findByInitiator(Mockito.anyString(), Mockito.anyString())).thenReturn(List.of(projectApprovalConfigDO));
            Mockito.when(envService.isAutonomy()).thenReturn(true);
            Mockito.when(kusciaGrpcClientAdapter.deleteDomainRoute(Mockito.any())).thenReturn(buildDeleteDomainRouteResponse(0));

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