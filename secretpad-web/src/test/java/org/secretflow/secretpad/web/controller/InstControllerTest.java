package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.InstConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.errorcode.InstErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.TokenUtil;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.model.NodeDTO;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.inst.InstRequest;
import org.secretflow.secretpad.service.model.node.CreateNodeRequest;
import org.secretflow.secretpad.service.model.node.NodeIdRequest;
import org.secretflow.secretpad.service.model.node.NodeTokenRequest;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class InstControllerTest extends ControllerTest {

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private InstRepository instRepository;


    @MockBean
    private NodeRouteRepository nodeRouteRepository;

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    @MockBean
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Test
    public void testGet() throws Exception {
        assertResponse(() -> {
            InstRequest request = new InstRequest("alice");
            UserContext.getUser().setOwnerId("alice");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.INST_GET));
            InstDO instDO = new InstDO();
            instDO.setInstId("alice");
            instDO.setName("test_inst_name");
            when(instRepository.findByInstId(any())).thenReturn(instDO);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "get", InstRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });

    }

    @Test
    public void testGetLoginMiss() throws Exception {
        assertErrorCode(() -> {
            InstRequest request = new InstRequest("bob");
            UserContext.getUser().setOwnerId("alice");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.INST_GET));
            InstDO instDO = new InstDO();
            instDO.setInstId("alice");
            instDO.setName("test_inst_name");
            when(instRepository.findByInstId(any())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "get", InstRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, InstErrorCode.INST_MISMATCH_LOGIN);

    }


    @Test
    public void testGetNotExist() throws Exception {
        assertErrorCode(() -> {
            InstRequest request = new InstRequest("alice");
            UserContext.getUser().setOwnerId("alice");
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.INST_GET));
            InstDO instDO = new InstDO();
            instDO.setInstId("alice");
            instDO.setName("test_inst_name");
            when(instRepository.findByInstId(any())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "get", InstRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, InstErrorCode.INST_NOT_EXISTS);

    }


    /**
     * test list node
     *
     * @throws Exception
     */
    @Test
    public void testListNode() throws Exception {
        assertResponse(() -> {
            NodeDO node1 = new NodeDO();
            node1.setInstId("user");
            node1.setNodeId("node1");
            node1.setName("node1");
            node1.setInstToken("token1");
            NodeDO node2 = new NodeDO();
            node2.setInstId("user");
            node2.setNodeId("node2");
            node2.setName("node2");
            node2.setInstToken("token2");
            InstDO instDO = new InstDO();
            instDO.setInstId("user");
            instDO.setName("test_inst_name");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("node1");
            nodeRouteDO.setDstNodeId("node2");
            when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(DomainOuterClass.QueryDomainResponseData.newBuilder().setCert("123").build())
                    .build());
            when(instRepository.existsById(anyString())).thenReturn(true);
            when(nodeRepository.findByInstId(any())).thenReturn(List.of(node1, node2));
            when(instRepository.findById(any())).thenReturn(Optional.of(instDO));
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(any())).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "listNode"));
        });
    }

    /**
     * test list node isMainNode
     *
     * @throws Exception
     */

    @Test
    public void testListNodeisMainNode() throws Exception {
        assertResponse(() -> {
            NodeDO node1 = new NodeDO();
            node1.setInstId("user");
            node1.setNodeId("kuscia-system");
            node1.setName("node1");
            node1.setInstToken("token1");
            NodeDO node2 = new NodeDO();
            node2.setInstId("user");
            node2.setNodeId("kuscia-system");
            node2.setName("node2");
            node2.setInstToken("token2");
            InstDO instDO = new InstDO();
            instDO.setInstId("user");
            instDO.setName("test_inst_name");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("node1");
            nodeRouteDO.setDstNodeId("node2");
            when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(DomainOuterClass.QueryDomainResponseData.newBuilder().setCert("123").build())
                    .build());
            when(instRepository.existsById(anyString())).thenReturn(true);
            when(nodeRepository.findByInstId(any())).thenReturn(List.of(node1, node2));
            when(instRepository.findById(any())).thenReturn(Optional.of(instDO));
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(any())).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "listNode"));
        });
    }


    /**
     * test list node INST_NOT_EXISTS
     *
     * @throws Exception
     */
    @Test
    public void testListNodeINST_NOT_EXISTS() throws Exception {
        assertErrorCode(() -> {
            NodeDO node1 = new NodeDO();
            node1.setInstId("user");
            node1.setNodeId("node1");
            node1.setName("node1");
            node1.setInstToken("token1");
            NodeDO node2 = new NodeDO();
            node2.setInstId("user");
            node2.setNodeId("node2");
            node2.setName("node2");
            node2.setInstToken("token2");
            InstDO instDO = new InstDO();
            instDO.setInstId("user");
            instDO.setName("test_inst_name");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("node1");
            nodeRouteDO.setDstNodeId("node2");
            when(instRepository.existsById(anyString())).thenReturn(false);
            when(nodeRepository.findByInstId(any())).thenReturn(List.of(node1, node2));
            when(instRepository.findById(any())).thenReturn(Optional.of(instDO));
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(any())).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "listNode"));
        }, InstErrorCode.INST_NOT_EXISTS);
    }

    /**
     * test list node INST_NOT_EXISTS instRepository.findById(instId) return null
     *
     * @throws Exception
     */
    @Test
    public void testListNodeINST_NOT_EXISTS_instRepository_findById_return_null() throws Exception {
        assertErrorCode(() -> {
            NodeDO node1 = new NodeDO();
            node1.setInstId("user");
            node1.setNodeId("node1");
            node1.setName("node1");
            node1.setInstToken("token1");
            NodeDO node2 = new NodeDO();
            node2.setInstId("user");
            node2.setNodeId("node2");
            node2.setName("node2");
            node2.setInstToken("token2");
            InstDO instDO = new InstDO();
            instDO.setInstId("user");
            instDO.setName("test _inst _name");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("node1");
            nodeRouteDO.setDstNodeId("node2");
            when(instRepository.existsById(anyString())).thenReturn(true);
            when(nodeRepository.findByInstId(any())).thenReturn(List.of(node1, node2));
            when(instRepository.findById(any())).thenReturn(Optional.empty());
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(any())).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "listNode"));
        }, InstErrorCode.INST_NOT_EXISTS);
    }


    /**
     * test list node nodeDOs is empty
     *
     * @throws Exception
     */
    @Test
    public void testListNodeNodeDOsIsEmpty() throws Exception {
        assertResponse(() -> {
            NodeDO node1 = new NodeDO();
            node1.setInstId("user");
            node1.setNodeId("node1");
            node1.setName("node1");
            node1.setInstToken("token1");
            NodeDO node2 = new NodeDO();
            node2.setInstId("user");
            node2.setNodeId("node2");
            node2.setName("node2");
            node2.setInstToken("token2");
            InstDO instDO = new InstDO();
            instDO.setInstId("user");
            instDO.setName("test_inst_name");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("node1");
            nodeRouteDO.setDstNodeId("node2");
            when(instRepository.existsById(anyString())).thenReturn(true);
            when(nodeRepository.findByInstId(any())).thenReturn(List.of());
            when(instRepository.findById(any())).thenReturn(Optional.of(instDO));
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(any())).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "listNode"));
        });
    }


    /**
     * test createNode
     *
     * @throws Exception
     */
    @Test
    public void testCreateNode() throws Exception {
        assertResponse(() -> {
            NodeDTO node1 = new NodeDTO();
            node1.setInstId("user");
            node1.setNodeId("node1");
            node1.setNodeName("node1");
            node1.setInstToken("token1");
            NodeDTO node2 = new NodeDTO();
            node2.setInstId("user");
            node2.setNodeId("node2");
            node2.setNodeName("node2");
            node2.setInstToken("token2");
            when(nodeRepository.countByInstId(any())).thenReturn(0);
            CreateNodeRequest request = new CreateNodeRequest();
            request.setName("new_node");
            request.setMode(1);

            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "createNode", CreateNodeRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * test createNode node count limit exceeded
     *
     * @throws Exception
     */
    @Test
    public void testCreateNodeNodeCountLimitExceeded() throws Exception {
        assertErrorCode(() -> {
            NodeDTO node1 = new NodeDTO();
            node1.setInstId("user");
            node1.setNodeId("node1");
            node1.setNodeName("node1");
            node1.setInstToken("token1");
            NodeDTO node2 = new NodeDTO();
            node2.setInstId("user");
            node2.setNodeId("node2");
            node2.setNodeName("node2");
            node2.setInstToken("token2");
            when(nodeRepository.countByInstId(any())).thenReturn(InstConstants.MAX_NODE_NUM);
            CreateNodeRequest request = new CreateNodeRequest();
            request.setName("new_node");
            request.setMode(1);

            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "createNode", CreateNodeRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, InstErrorCode.INST_NODE_COUNT_LIMITED);
    }


    /**
     * test getToken
     *
     * @throws Exception
     */

    @Test
    public void testGetToken() throws Exception {
        assertResponse(() -> {
            NodeTokenRequest request = new NodeTokenRequest();
            request.setNodeId("alice");
            NodeDO node = new NodeDO();
            node.setInstId("user");
            node.setNodeId("alice");
            node.setName("alice");
            String sign = TokenUtil.sign(node.getInstId(), node.getNodeId());
            node.setInstToken(sign);
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(node);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "token", NodeTokenRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * test getToken node is empty
     *
     * @throws Exception
     */
    @Test
    public void testGetTokenNodeIsEmpty() throws Exception {
        assertErrorCode(() -> {
            NodeTokenRequest request = new NodeTokenRequest();
            request.setNodeId("alice");
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "token", NodeTokenRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    /**
     * test newToken
     *
     * @throws Exception
     */
    @Test
    public void testNewToken() throws Exception {
        assertResponse(() -> {
            NodeTokenRequest request = new NodeTokenRequest();
            request.setNodeId("alice");
            NodeDO node = new NodeDO();
            node.setInstId("user");
            node.setNodeId("alice");
            node.setName("alice");
            node.setInstToken("token1");
            UserContext.getUser().setName("user");
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(node);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "newToken", NodeTokenRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }


    /**
     * test newToken node is empty
     *
     * @throws Exception
     */
    @Test
    public void testNewTokenNodeIsEmpty() throws Exception {
        assertErrorCode(() -> {
            NodeTokenRequest request = new NodeTokenRequest();
            request.setNodeId("alice");
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "newToken", NodeTokenRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    /**
     * test newToken node is empty
     *
     * @throws Exception
     */
    @Test
    public void testNewTokenNodeIsError() throws Exception {
        try (MockedStatic<TokenUtil> tokenUtilMockedStatic = Mockito.mockStatic(TokenUtil.class)) {
            assertErrorCode(() -> {
                NodeTokenRequest request = new NodeTokenRequest();
                request.setNodeId("alice");
                NodeDO node = new NodeDO();
                node.setInstId("user");
                node.setNodeId("alice");
                node.setName("alice");
                node.setInstToken("token1");
                when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(node);
                tokenUtilMockedStatic.when(() -> TokenUtil.sign(any(), any())).thenReturn("123");
                return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "newToken", NodeTokenRequest.class))
                        .content(JsonUtils.toJSONString(request));
            }, InstErrorCode.INST_TOKEN_MISMATCH);
        }
    }

    /**
     * test deleteNode
     *
     * @throws Exception
     */

    @Test
    public void testDeleteNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            String instId = "testInstId";
            String nodeId = "test-node-id";
            String projectId = "testProjectId";
            NodeDO nodeDO = new NodeDO();
            nodeDO.setInstId(instId);
            nodeDO.setNodeId(nodeId);
            nodeDO.setName("testNodeName");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId(nodeId);
            nodeRouteDO.setDstNodeId(nodeId);
            ProjectNodeDO projectNodeDO = new ProjectNodeDO();
            projectNodeDO.setId(12L);
            projectNodeDO.setUpk(new ProjectNodeDO.UPK(projectId, nodeId));
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("testProjectName");
            projectDO.setOwnerId(instId);
            projectDO.setProjectId(projectId);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId(projectId);
            projectApprovalConfigDO.setParties(List.of(instId));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0))
                    .build();
            DomainOuterClass.DeleteDomainResponse deleteDomainResponse = DomainOuterClass.DeleteDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0))
                    .build();
            when(nodeRepository.findOneByInstId(Mockito.anyString(), Mockito.anyString())).thenReturn(nodeDO);
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeId)).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            when(projectNodeRepository.findByNodeId(nodeId)).thenReturn(List.of(projectNodeDO));
            when(projectRepository.findByStatus(Mockito.anyInt())).thenReturn(List.of(projectDO));
            when(projectApprovalConfigRepository.findByProjectIdsAndType(List.of(projectId), "PROJECT_CREATE")).thenReturn(List.of(projectApprovalConfigDO));
            when(kusciaGrpcClientAdapter.isDomainRegistered(Mockito.anyString())).thenReturn(true);
            when(kusciaGrpcClientAdapter.queryDomain(any(), anyString())).thenReturn(queryDomainResponse);
            when(kusciaGrpcClientAdapter.deleteDomain(any(), anyString())).thenReturn(deleteDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * test deleteNode node is empty
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNodeNodeIsEmpty() throws Exception {
        assertErrorCode(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    /**
     * test deleteNode node route is not empty
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNodeNodeRouteIsNotEmpty() throws Exception {
        assertErrorCode(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            NodeDO node = new NodeDO();
            node.setInstId("user");
            node.setNodeId("alice");
            node.setName("alice");
            node.setInstToken("token1");
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(node);
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId("node1");
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(Mockito.anyString())).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }

    /**
     * test deleteNode node have job running
     *
     * @throws Exception
     */
    @Test
    public void testDeleteNodeNodeHaveJobRunning() throws Exception {
        assertErrorCode(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            String nodeId = "test-node-id";
            String projectId = "testProjectId";
            NodeDO node = new NodeDO();
            node.setInstId("user");
            node.setNodeId("alice");
            node.setName("alice");
            node.setInstToken("token1");
            ProjectNodeDO projectNodeDO = new ProjectNodeDO();
            projectNodeDO.setId(12L);
            projectNodeDO.setUpk(new ProjectNodeDO.UPK(projectId, nodeId));
            when(nodeRepository.findOneByInstId(any(), Mockito.anyString())).thenReturn(node);
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(Mockito.anyString())).thenReturn(new HashSet<>());
            when(projectNodeRepository.findByNodeId(Mockito.anyString())).thenReturn(List.of(projectNodeDO));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }

    /**
     * test deleteNode NodeErrorCode.NODE_DELETE_ERROR
     *
     * @throws Exception
     */

    @Test
    public void testDeleteNode_NODE_DELETE_ERROR() throws Exception {
        assertErrorCode(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("kuscia-system");
            String instId = "testInstId";
            String nodeId = "kuscia-system";
            String projectId = "testProjectId";
            NodeDO nodeDO = new NodeDO();
            nodeDO.setInstId(instId);
            nodeDO.setNodeId(nodeId);
            nodeDO.setName("testNodeName");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId(nodeId);
            nodeRouteDO.setDstNodeId(nodeId);
            ProjectNodeDO projectNodeDO = new ProjectNodeDO();
            projectNodeDO.setId(12L);
            projectNodeDO.setUpk(new ProjectNodeDO.UPK(projectId, nodeId));
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("testProjectName");
            projectDO.setOwnerId(instId);
            projectDO.setProjectId(projectId);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId(projectId);
            projectApprovalConfigDO.setParties(List.of(instId));
            when(nodeRepository.findOneByInstId(Mockito.anyString(), Mockito.anyString())).thenReturn(nodeDO);
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeId)).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            when(projectNodeRepository.findByNodeId(nodeId)).thenReturn(List.of(projectNodeDO));
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectDO));
            when(projectApprovalConfigRepository.findByProjectId(projectId)).thenReturn(Optional.of(projectApprovalConfigDO));
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }

    /**
     * test deleteNode
     *
     * @throws Exception
     */

    @Test
    public void testDeleteNode_domainRegistered() throws Exception {
        assertResponseWithEmptyData(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            String instId = "testInstId";
            String nodeId = "test-node-id";
            String projectId = "testProjectId";
            NodeDO nodeDO = new NodeDO();
            nodeDO.setInstId(instId);
            nodeDO.setNodeId(nodeId);
            nodeDO.setName("testNodeName");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId(nodeId);
            nodeRouteDO.setDstNodeId(nodeId);
            ProjectNodeDO projectNodeDO = new ProjectNodeDO();
            projectNodeDO.setId(12L);
            projectNodeDO.setUpk(new ProjectNodeDO.UPK(projectId, nodeId));
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("testProjectName");
            projectDO.setOwnerId(instId);
            projectDO.setProjectId(projectId);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId(projectId);
            projectApprovalConfigDO.setParties(List.of(instId));
            when(nodeRepository.findOneByInstId(Mockito.anyString(), Mockito.anyString())).thenReturn(nodeDO);
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeId)).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            when(projectNodeRepository.findByNodeId(nodeId)).thenReturn(List.of(projectNodeDO));
            when(projectRepository.findByStatus(Mockito.anyInt())).thenReturn(List.of(projectDO));
            when(projectApprovalConfigRepository.findByProjectIdsAndType(List.of(projectId), "PROJECT_CREATE")).thenReturn(List.of(projectApprovalConfigDO));
            when(kusciaGrpcClientAdapter.isDomainRegistered(Mockito.anyString())).thenReturn(false);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * test deleteNode isDomainExist false
     *
     * @throws Exception
     */

    @Test
    public void testDeleteNode_isDomainExist() throws Exception {
        assertResponseWithEmptyData(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            String instId = "testInstId";
            String nodeId = "test-node-id";
            String projectId = "testProjectId";
            NodeDO nodeDO = new NodeDO();
            nodeDO.setInstId(instId);
            nodeDO.setNodeId(nodeId);
            nodeDO.setName("testNodeName");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId(nodeId);
            nodeRouteDO.setDstNodeId(nodeId);
            ProjectNodeDO projectNodeDO = new ProjectNodeDO();
            projectNodeDO.setId(12L);
            projectNodeDO.setUpk(new ProjectNodeDO.UPK(projectId, nodeId));
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("testProjectName");
            projectDO.setOwnerId(instId);
            projectDO.setProjectId(projectId);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId(projectId);
            projectApprovalConfigDO.setParties(List.of(instId));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(1).setMessage("domain not exist"))
                    .build();
            when(nodeRepository.findOneByInstId(Mockito.anyString(), Mockito.anyString())).thenReturn(nodeDO);
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeId)).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            when(projectNodeRepository.findByNodeId(nodeId)).thenReturn(List.of(projectNodeDO));
            when(projectRepository.findByStatus(Mockito.anyInt())).thenReturn(List.of(projectDO));
            when(projectApprovalConfigRepository.findByProjectIdsAndType(List.of(projectId), "PROJECT_CREATE")).thenReturn(List.of(projectApprovalConfigDO));
            when(kusciaGrpcClientAdapter.isDomainRegistered(Mockito.anyString())).thenReturn(true);
            when(kusciaGrpcClientAdapter.queryDomain(any(), anyString())).thenReturn(queryDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * test deleteNode NodeErrorCode.NODE_DELETE_ERROR
     *
     * @throws Exception
     */

    @Test
    public void testDeleteNode_NODE_DELETE_ERROR_2() throws Exception {
        assertErrorCode(() -> {
            NodeIdRequest request = new NodeIdRequest();
            request.setNodeId("alice");
            String instId = "testInstId";
            String nodeId = "test-node-id";
            String projectId = "testProjectId";
            NodeDO nodeDO = new NodeDO();
            nodeDO.setInstId(instId);
            nodeDO.setNodeId(nodeId);
            nodeDO.setName("testNodeName");
            NodeRouteDO nodeRouteDO = new NodeRouteDO();
            nodeRouteDO.setSrcNodeId(nodeId);
            nodeRouteDO.setDstNodeId(nodeId);
            ProjectNodeDO projectNodeDO = new ProjectNodeDO();
            projectNodeDO.setId(12L);
            projectNodeDO.setUpk(new ProjectNodeDO.UPK(projectId, nodeId));
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("testProjectName");
            projectDO.setOwnerId(instId);
            projectDO.setProjectId(projectId);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId(projectId);
            projectApprovalConfigDO.setParties(List.of("alice"));
            DomainOuterClass.QueryDomainResponse queryDomainResponse = DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).setMessage("domain exist"))
                    .build();
            DomainOuterClass.DeleteDomainResponse deleteDomainResponse = DomainOuterClass.DeleteDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(1).setMessage("kuscia delete domain failed"))
                    .build();
            when(nodeRepository.findOneByInstId(Mockito.anyString(), Mockito.anyString())).thenReturn(nodeDO);
            when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeId)).thenReturn(new HashSet<>(List.of(nodeRouteDO)));
            when(projectNodeRepository.findByNodeId(nodeId)).thenReturn(List.of(projectNodeDO));
            when(projectRepository.findByStatus(Mockito.anyInt())).thenReturn(List.of(projectDO));
            when(projectApprovalConfigRepository.findByProjectIdsAndType(List.of(projectId), "PROJECT_CREATE")).thenReturn(List.of(projectApprovalConfigDO));
            when(kusciaGrpcClientAdapter.isDomainRegistered(Mockito.anyString())).thenReturn(true);
            when(kusciaGrpcClientAdapter.queryDomain(any(), anyString())).thenReturn(queryDomainResponse);
            when(kusciaGrpcClientAdapter.deleteDomain(any(), anyString())).thenReturn(deleteDomainResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(InstController.class, "deleteNode", NodeIdRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_DELETE_ERROR);
    }


    /**
     * test registerNode
     *
     * @throws Exception
     */

    @Test
    void testRegisterNode() throws Exception {
        assertMultipartResponseWithEmptyData(() -> {
            String token = TokenUtil.sign("testInstId", "test-node-id");
            String tokenStr = FileUtils.readFile2String(token);
            String jsonData = "{\"token\":\"" + tokenStr + "\",\"domainId\":\"test-node-id\",\"host\":\"http://example.com\",\"port\":8080,\"mode\":\"P2P\",\"protocol\":\"NOTLS\"}";
            File clientCrt = FileUtils.readFile("./config/certs/client.crt");
            File clientPem = FileUtils.readFile("./config/certs/client.pem");
            File clientToken = FileUtils.readFile("./config/certs/token");
            MockMultipartFile file1 = new MockMultipartFile("certFile", "client.crt", MediaType.TEXT_PLAIN_VALUE, FileUtils.readFile2String(clientCrt).getBytes());
            MockMultipartFile file2 = new MockMultipartFile("keyFile", "client.pem", MediaType.TEXT_PLAIN_VALUE, FileUtils.readFile2String(clientPem).getBytes());
            MockMultipartFile file3 = new MockMultipartFile("token", "token", MediaType.TEXT_PLAIN_VALUE, FileUtils.readFile2String(clientToken).getBytes());
            NodeDO nodeDO = NodeDO.builder()
                    .nodeId("test-node-id")
                    .instId("testInstId")
                    .name("testNodeName")
                    .instToken(token)
                    .build();
            InstDO instDO = InstDO.builder()
                    .instId("testInstId")
                    .name("testInstName")
                    .build();
            when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(nodeDO);
            when(instRepository.findByInstId(Mockito.anyString())).thenReturn(instDO);
            return MockMvcRequestBuilders.multipart(getMappingUrl(InstController.class, "registerNode", String.class, MultipartFile.class, MultipartFile.class, MultipartFile.class))
                    .file(file1)
                    .file(file2)
                    .file(file3)
                    .param("json_data", jsonData);
        });
    }

    /**
     * test registerNode node is null with NodeErrorCode.NODE_NOT_EXIST_ERROR
     *
     * @throws Exception
     */
    @Test
    void testRegisterNodeNodeIsNull() throws Exception {
        assertMultipartErrorCode(() -> {
            String token = TokenUtil.sign("testInstId", "test-node-id");
            String jsonData = "{\"token\":\"" + token + "\",\"domainId\":\"test-node-id\",\"host\":\"http://example.com\",\"port\":8080,\"mode\":\"P2P\",\"protocol\":\"NOTLS\"}";
            MockMultipartFile file1 = new MockMultipartFile("certFile", "client.crt", MediaType.TEXT_PLAIN_VALUE, "client.crt".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("keyFile", "client.pem", MediaType.TEXT_PLAIN_VALUE, "client.pem".getBytes());
            MockMultipartFile file3 = new MockMultipartFile("token", "token", MediaType.TEXT_PLAIN_VALUE, "token".getBytes());
            when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(null);
            return MockMvcRequestBuilders.multipart(getMappingUrl(InstController.class, "registerNode", String.class, MultipartFile.class, MultipartFile.class, MultipartFile.class))
                    .file(file1)
                    .file(file2)
                    .file(file3)
                    .param("json_data", jsonData);
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    /**
     * test registerNode inst is null with InstErrorCode.INST_NOT_EXISTS
     *
     * @throws Exception
     */
    @Test
    void testRegisterNodeInstIsNull() throws Exception {
        assertMultipartErrorCode(() -> {
            String token = TokenUtil.sign("testInstId", "test-node-id");
            String jsonData = "{\"token\":\"" + token + "\",\"domainId\":\"test-node-id\",\"host\":\"http://example.com\",\"port\":8080,\"mode\":\"P2P\",\"protocol\":\"NOTLS\"}";
            MockMultipartFile file1 = new MockMultipartFile("certFile", "client.crt", MediaType.TEXT_PLAIN_VALUE, "client.crt".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("keyFile", "client.pem", MediaType.TEXT_PLAIN_VALUE, "client.pem".getBytes());
            MockMultipartFile file3 = new MockMultipartFile("token", "token", MediaType.TEXT_PLAIN_VALUE, "token".getBytes());
            NodeDO nodeDO = NodeDO.builder()
                    .nodeId("test-node-id")
                    .instId("testInstId")
                    .name("testNodeName")
                    .instToken(token)
                    .build();
            when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(nodeDO);
            when(instRepository.findByInstId(Mockito.anyString())).thenReturn(null);
            return MockMvcRequestBuilders.multipart(getMappingUrl(InstController.class, "registerNode", String.class, MultipartFile.class, MultipartFile.class, MultipartFile.class))
                    .file(file1)
                    .file(file2)
                    .file(file3)
                    .param("json_data", jsonData);
        }, InstErrorCode.INST_NOT_EXISTS);
    }

    /**
     * test registerNode with InstErrorCode.INST_TOKEN_MISMATCH
     *
     * @throws Exception
     */

    @Test
    void testRegisterNodeInstTokenMismatch() throws Exception {
        assertMultipartErrorCode(() -> {
            String token = TokenUtil.sign("testInstId", "test-node-id");
            String jsonData = "{\"token\":\"" + token + "\",\"domainId\":\"test-node-id\",\"host\":\"http://example.com\",\"port\":8080,\"mode\":\"P2P\",\"protocol\":\"NOTLS\"}";
            MockMultipartFile file1 = new MockMultipartFile("certFile", "client.crt", MediaType.TEXT_PLAIN_VALUE, "client.crt".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("keyFile", "client.pem", MediaType.TEXT_PLAIN_VALUE, "client.pem".getBytes());
            MockMultipartFile file3 = new MockMultipartFile("token", "token", MediaType.TEXT_PLAIN_VALUE, "token".getBytes());
            NodeDO nodeDO = NodeDO.builder()
                    .nodeId("test-node-id")
                    .instId("testInstId")
                    .name("testNodeName")
                    .instToken(token + "1")
                    .build();
            InstDO instDO = InstDO.builder()
                    .instId("testInstId")
                    .name("testInstName")
                    .build();
            when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(nodeDO);
            when(instRepository.findByInstId(Mockito.anyString())).thenReturn(instDO);
            return MockMvcRequestBuilders.multipart(getMappingUrl(InstController.class, "registerNode", String.class, MultipartFile.class, MultipartFile.class, MultipartFile.class))
                    .file(file1)
                    .file(file2)
                    .file(file3)
                    .param("json_data", jsonData);
        }, InstErrorCode.INST_TOKEN_MISMATCH);
    }

    /**
     * test registerNode verify false with InstErrorCode.INST_TOKEN_MISMATCH
     *
     * @throws Exception
     */

    @Test
    void testRegisterNodeVerifyFalse() throws Exception {
        assertMultipartErrorCode(() -> {
            String jsonData = "{\"token\":\"token\",\"domainId\":\"test-node-id\",\"host\":\"http://example.com\",\"port\":8080,\"mode\":\"P2P\",\"protocol\":\"NOTLS\"}";
            MockMultipartFile file1 = new MockMultipartFile("certFile", "client.crt", MediaType.TEXT_PLAIN_VALUE, "client.crt".getBytes());
            MockMultipartFile file2 = new MockMultipartFile("keyFile", "client.pem", MediaType.TEXT_PLAIN_VALUE, "client.pem".getBytes());
            MockMultipartFile file3 = new MockMultipartFile("token", "token", MediaType.TEXT_PLAIN_VALUE, "token".getBytes());
            NodeDO nodeDO = NodeDO.builder()
                    .nodeId("test-node-id")
                    .instId("testInstId")
                    .name("testNodeName")
                    .instToken("token")
                    .build();
            InstDO instDO = InstDO.builder()
                    .instId("testInstId")
                    .name("testInstName")
                    .build();
            when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(nodeDO);
            when(instRepository.findByInstId(Mockito.anyString())).thenReturn(instDO);
            return MockMvcRequestBuilders.multipart(getMappingUrl(InstController.class, "registerNode", String.class, MultipartFile.class, MultipartFile.class, MultipartFile.class))
                    .file(file1)
                    .file(file2)
                    .file(file3)
                    .param("json_data", jsonData);
        }, InstErrorCode.INST_TOKEN_MISMATCH);
    }


}
