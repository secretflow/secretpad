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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.model.NodeResultDTO;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.repository.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;

/**
 * @author beiwei
 * @date 2023/9/11
 */
@ExtendWith(MockitoExtension.class)
class NodeManagerTest {
    @Mock
    private NodeRepository nodeRepository;
    @Mock
    private NodeRouteRepository nodeRouteRepository;

    @Mock
    private InstRepository instRepository;

    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @InjectMocks
    private NodeManager nodeManager;

    @Test
    void genDomainId() {
        NodeManager nodeManager = new NodeManager(null, null,
                null, null, null,
                null, null, null,
                null, null);
        String s = nodeManager.genDomainId();
        Assertions.assertThat(s).hasSize(8);
    }

    /**
     * test InitialNode_NodeExists_InstIdExists
     */
    @Test
    public void testInitialNode_NodeExists_InstIdExists() {
        when(nodeRepository.existsById(anyString())).thenReturn(true);
        NodeDO mockNodeDO = new NodeDO();
        mockNodeDO.setInstId("testInstId");
        when(nodeRepository.findByNodeId(anyString())).thenReturn(mockNodeDO);

        nodeManager.initialNode("testNodeId", "testInstName");
    }

    /**
     * test InitialNode_nodeDOIsNull
     */
    @Test
    public void testInitialNode_nodeDOIsNull() {
        when(nodeRepository.existsById(anyString())).thenReturn(true);
        NodeDO mockNodeDO = new NodeDO();
        mockNodeDO.setInstId("testInstId");
        when(nodeRepository.findByNodeId(anyString())).thenReturn(mockNodeDO);

        nodeManager.initialNode("testNodeId", "testInstName");
    }

    private DomainOuterClass.QueryDomainResponse buildQueryDomainResponse(Integer code) {
        return DomainOuterClass.QueryDomainResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(code)
                        .build()).setData(DomainOuterClass.QueryDomainResponseData.newBuilder()
                        .addNodeStatuses(DomainOuterClass.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()).build())
                        .build()).build();
    }

    @Test
    public void testListReadyNodeByIds_NodeIdsNotEmpty() {
        List<NodeDO> nodeDOS = new ArrayList<>();
        NodeDO node1 = new NodeDO();
        node1.setNodeId("node1");
        node1.setInstId("inst1");
        nodeDOS.add(node1);
        NodeDO node2 = new NodeDO();
        node2.setNodeId("node2");
        node2.setInstId("inst1");
        nodeDOS.add(node2);

        InstDO instDO = new InstDO();
        instDO.setInstId("inst1");
        Optional<InstDO> optionalInstDO = Optional.of(instDO);
        when(instRepository.findById(anyString())).thenReturn(optionalInstDO);
        when(nodeRepository.findByInstId(anyString())).thenReturn(nodeDOS);
        when(nodeRouteRepository.findBySrcNodeIdOrDstNodeId(anyString())).thenReturn(new HashSet<>());
        when(kusciaGrpcClientAdapter.isDomainRegistered(anyString())).thenReturn(true);
        DomainOuterClass.QueryDomainResponse queryDomainResponse = buildQueryDomainResponse(0);
        when(kusciaGrpcClientAdapter.queryDomain(Mockito.any(), Mockito.any())).thenReturn(queryDomainResponse);

        List<String> result = nodeManager.listReadyNodeByIds("inst1", Arrays.asList("node1", "node2"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("node1"));
        assertTrue(result.contains("node2"));
    }


    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectJobRepository projectJobRepository;


    @Test
    public void testMergeNodeResult_ProjectAndJobExist() {
        ProjectDO projectDO = new ProjectDO();
        projectDO.setProjectId("projectId");
        projectDO.setName("projectName");
        projectDO.setComputeMode("computeMode");
        when(projectRepository.findById("projectId")).thenReturn(Optional.of(projectDO));

        ProjectJobDO projectJobDO = new ProjectJobDO();
        projectJobDO.setName("trainFlow");
        projectJobDO.setUpk(new ProjectJobDO.UPK("projectId", "jobId"));
        when(projectJobRepository.findByJobId("jobId")).thenReturn(Optional.of(projectJobDO));

        ProjectResultDO projectResultDO = new ProjectResultDO();
        projectResultDO.setJobId("jobId");
        projectResultDO.setUpk(new ProjectResultDO.UPK("projectId", ResultKind.FedTable, "nodeId", "jobId"));
        NodeResultDTO result = invokeMethod(nodeManager, "mergeNodeResult", projectResultDO);

        assertEquals("jobId", result.getJobId());
        assertEquals("projectId", result.getSourceProjectId());
        assertEquals("projectName", result.getSourceProjectName());
        assertEquals("trainFlow", result.getTrainFlow());
    }

    /**
     *
     */
    @Test
    public void testMergeNodeResult_ProjectExistButJobNotExist() {
        ProjectDO projectDO = new ProjectDO();
        projectDO.setProjectId("projectId");
        projectDO.setName("projectName");
        projectDO.setComputeMode("computeMode");
        when(projectRepository.findById("projectId")).thenReturn(Optional.of(projectDO));

        when(projectJobRepository.findByJobId("jobId")).thenReturn(Optional.empty());

        ProjectResultDO projectResultDO = new ProjectResultDO();
        projectResultDO.setJobId("jobId");
        projectResultDO.setUpk(new ProjectResultDO.UPK("projectId", ResultKind.FedTable, "nodeId", "jobId"));
        NodeResultDTO result = invokeMethod(nodeManager, "mergeNodeResult", projectResultDO);

        assertEquals("jobId", result.getJobId());
        assertEquals("projectId", result.getSourceProjectId());
        assertEquals("projectName", result.getSourceProjectName());
        assertEquals(null, result.getTrainFlow());
    }

    /**
     *
     */
    @Test
    public void testMergeNodeResult_ProjectNotExist() {
        when(projectRepository.findById("projectId")).thenReturn(Optional.empty());

        ProjectResultDO projectResultDO = new ProjectResultDO();
        projectResultDO.setJobId("jobId");
        projectResultDO.setUpk(new ProjectResultDO.UPK("projectId", ResultKind.FedTable, "nodeId", "jobId"));
        NodeResultDTO result = invokeMethod(nodeManager, "mergeNodeResult", projectResultDO);

        assertEquals("jobId", result.getJobId());
        assertEquals(null, result.getSourceProjectId());
        assertEquals(null, result.getSourceProjectName());
        assertEquals(null, result.getTrainFlow());
    }

    @Test
    public void testSearchAllTargetNode() {
        when(projectRepository.findById("projectId")).thenReturn(Optional.empty());

        ProjectResultDO projectResultDO = new ProjectResultDO();
        projectResultDO.setJobId("jobId");
        projectResultDO.setUpk(new ProjectResultDO.UPK("projectId", ResultKind.FedTable, "nodeId", "jobId"));
        NodeResultDTO result = invokeMethod(nodeManager, "mergeNodeResult", projectResultDO);

        assertEquals("jobId", result.getJobId());
        assertEquals(null, result.getSourceProjectId());
        assertEquals(null, result.getSourceProjectName());
        assertEquals(null, result.getTrainFlow());
    }

    /**
     *
     */
    @Test
    public void testSearchAllTargetNodeFromInitiator() {
        String ss = "[{\"initiatorNodeId\":\"b1\",\"invitees\":[{\"inviteeId\":\"a1\"},{\"inviteeId\":\"a2\"}]}]";
        List<ParticipantNodeInstVO> participantNodeInstVOS = JsonUtils.toJavaList(ss, ParticipantNodeInstVO.class);
        List<String> result = invokeMethod(nodeManager, "searchAllTargetNode", "b1", participantNodeInstVOS.get(0));
        assertEquals(2, result.size());
    }

    /**
     *
     */
    @Test
    public void testSearchAllTargetNodeFromInvitee() {
        String ss = "[{\"initiatorNodeId\":\"b1\",\"invitees\":[{\"inviteeId\":\"a1\"},{\"inviteeId\":\"a2\"}]}]";
        List<ParticipantNodeInstVO> participantNodeInstVOS = JsonUtils.toJavaList(ss, ParticipantNodeInstVO.class);
        List<String> result = invokeMethod(nodeManager, "searchAllTargetNode", "a1", participantNodeInstVOS.get(0));
        assertEquals(1, result.size());
    }

}