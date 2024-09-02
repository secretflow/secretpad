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
import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.approval.VoteRequestBody;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;
import org.secretflow.secretpad.service.model.project.ArchiveProjectRequest;
import org.secretflow.secretpad.service.model.project.CreateProjectRequest;
import org.secretflow.secretpad.service.model.project.ProjectParticipantsRequest;
import org.secretflow.secretpad.service.model.project.UpdateProjectRequest;
import org.secretflow.secretpad.web.controller.p2p.P2PProjectController;
import org.secretflow.secretpad.web.utils.FakerUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;

/**
 * @author chenmingliang
 * @date 2024/01/04
 */
public class P2PProjectControllerTest extends ControllerTest {

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    @MockBean
    private ProjectInstRepository projectInstRepository;
    @MockBean
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;
    @MockBean
    private VoteRequestRepository voteRequestRepository;
    @MockBean
    private InstRepository instRepository;
    @MockBean
    private ProjectGraphRepository projectGraphDORepository;
    @MockBean
    private ProjectJobRepository projectJobRepository;
    @MockBean
    private NodeRepository nodeRepository;


    @Test
    void createProject() throws Exception {
        assertResponse(() -> {
            CreateProjectRequest request = new CreateProjectRequest();
            request.setName("test");
            request.setDescription("test project");
            request.setComputeMode("mpc");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_CREATE));

            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "createP2PProject", CreateProjectRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void listP2PProject() throws Exception {
        assertResponse(() -> {
            List<ProjectInstDO> projectInstDOList = Collections.singletonList(ProjectInstDO.builder().upk(new ProjectInstDO.UPK("proj1", "inst1")).build());
            Mockito.when(projectInstRepository.findByInstId("owner_id")).thenReturn(projectInstDOList);
            List<ProjectApprovalConfigDO> projectApprovalConfigDOS = Arrays.asList(
                    ProjectApprovalConfigDO.builder().projectId("project_id_1").voteID("vote_id_1").build(),
                    ProjectApprovalConfigDO.builder().projectId("project_id_2").voteID("vote_id_2").build()
            );
            Mockito.when(projectApprovalConfigRepository.listProjectApprovalConfigByType("PROJECT_CREATE")).thenReturn(projectApprovalConfigDOS);
            Mockito.when(projectApprovalConfigRepository.findByType("PROJECT_CREATE")).thenReturn(projectApprovalConfigDOS);
            List<ProjectDO> projects = Arrays.asList(
                    new ProjectDO("project_id_1", "Project 1", "Desc 1", "COMPUTE_MODE_1", "ccc", new ProjectInfoDO("tee_domain_1"), "owner_id_1", 0),
                    new ProjectDO("project_id_2", "Project 2", "Desc 2", "COMPUTE_MODE_2", "ccc", new ProjectInfoDO("tee_domain_2"), "owner_id_2", 2)
            );
            Mockito.when(projectRepository.findAllById(anySet())).thenReturn(projects);
            HashSet<VoteRequestDO.PartyVoteInfo> partyVoteInfos1 = new HashSet<>();
            HashSet<VoteRequestDO.PartyVoteInfo> partyVoteInfos2 = new HashSet<>();
            partyVoteInfos1.add(VoteRequestDO.PartyVoteInfo.builder().partyId("inst_id_1").action("ss").reason("xxx").build());
            partyVoteInfos2.add(VoteRequestDO.PartyVoteInfo.builder().partyId("inst_id_2").action("ss").reason("xxx").build());
            VoteRequestDO voteRequestDO1 = FakerUtils.fake(VoteRequestDO.class);
            VoteRequestDO voteRequestDO2 = FakerUtils.fake(VoteRequestDO.class);
            voteRequestDO1.setVoteID("vote_id_1");
            voteRequestDO1.setPartyVoteInfos(partyVoteInfos1);
            voteRequestDO2.setVoteID("vote_id_2");
            voteRequestDO2.setPartyVoteInfos(partyVoteInfos2);
            List<VoteRequestDO> voteRequestDOS = Arrays.asList(
                    voteRequestDO1, voteRequestDO2
            );
            Mockito.when(voteRequestRepository.findAllById(anyCollection())).thenReturn(voteRequestDOS);
            Mockito.when(projectNodeRepository.findProjectionByProjectId(anyString())).thenReturn(Collections.emptyList());
            Mockito.when(projectInstRepository.findProjectionByProjectId(anyString())).thenReturn(Collections.emptyList());
            Mockito.when(projectGraphDORepository.countByProjectId(anyString())).thenReturn(0);
            Mockito.when(projectJobRepository.countByProjectId(anyString())).thenReturn(0);
            Mockito.when(nodeRepository.findByNodeIdIn(anyList())).thenReturn(Collections.emptyList());
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "listP2PProject"));

        });
    }

    /**
     * listP2PProject with PROJECT_VOTE_NOT_EXISTS
     *
     * @return
     */

    @Test
    void listP2PProject_PROJECT_VOTE_NOT_EXISTS() throws Exception {
        assertErrorCode(() -> {
            List<ProjectInstDO> projectInstDOList = Collections.singletonList(ProjectInstDO.builder().upk(new ProjectInstDO.UPK("proj1", "inst1")).build());
            Mockito.when(projectInstRepository.findByInstId("owner_id")).thenReturn(projectInstDOList);
            List<ProjectApprovalConfigDO> projectApprovalConfigDOS = Arrays.asList(
                    ProjectApprovalConfigDO.builder().projectId("project_id_1").voteID("vote_id_1").build(),
                    ProjectApprovalConfigDO.builder().projectId("project_id_2").voteID("vote_id_2").build()
            );
            Mockito.when(projectApprovalConfigRepository.listProjectApprovalConfigByType("PROJECT_CREATE")).thenReturn(projectApprovalConfigDOS);
            Mockito.when(projectApprovalConfigRepository.findByType("PROJECT_CREATE")).thenReturn(projectApprovalConfigDOS);
            List<ProjectDO> projects = Arrays.asList(
                    new ProjectDO("project_id_3", "Project 1", "Desc 1", "COMPUTE_MODE_1", "ccc", new ProjectInfoDO("tee_domain_1"), "owner_id_1", 0),
                    new ProjectDO("project_id_4", "Project 2", "Desc 2", "COMPUTE_MODE_2", "ccc", new ProjectInfoDO("tee_domain_2"), "owner_id_2", 2)
            );
            Mockito.when(projectRepository.findAllById(anySet())).thenReturn(projects);
            HashSet<VoteRequestDO.PartyVoteInfo> partyVoteInfos1 = new HashSet<>();
            HashSet<VoteRequestDO.PartyVoteInfo> partyVoteInfos2 = new HashSet<>();
            partyVoteInfos1.add(VoteRequestDO.PartyVoteInfo.builder().partyId("inst_id_1").action("ss").reason("xxx").build());
            partyVoteInfos2.add(VoteRequestDO.PartyVoteInfo.builder().partyId("inst_id_2").action("ss").reason("xxx").build());
            VoteRequestDO voteRequestDO1 = FakerUtils.fake(VoteRequestDO.class);
            VoteRequestDO voteRequestDO2 = FakerUtils.fake(VoteRequestDO.class);
            voteRequestDO1.setVoteID("vote_id_1");
            voteRequestDO1.setPartyVoteInfos(partyVoteInfos1);
            voteRequestDO2.setVoteID("vote_id_2");
            voteRequestDO2.setPartyVoteInfos(partyVoteInfos2);
            List<VoteRequestDO> voteRequestDOS = Arrays.asList(
                    voteRequestDO1, voteRequestDO2
            );
            Mockito.when(voteRequestRepository.findAllById(anyCollection())).thenReturn(voteRequestDOS);
            Mockito.when(projectNodeRepository.findProjectionByProjectId(anyString())).thenReturn(Collections.emptyList());
            Mockito.when(projectInstRepository.findProjectionByProjectId(anyString())).thenReturn(Collections.emptyList());
            Mockito.when(projectGraphDORepository.countByProjectId(anyString())).thenReturn(0);
            Mockito.when(projectJobRepository.countByProjectId(anyString())).thenReturn(0);
            Mockito.when(nodeRepository.findByNodeIdIn(anyList())).thenReturn(Collections.emptyList());
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "listP2PProject"));

        }, VoteErrorCode.PROJECT_VOTE_NOT_EXISTS);
    }

    private ProjectNodeDO buildProjectNodeDO() {
        return ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK(PROJECT_ID, "alice")).build();
    }

    private ProjectDO buildProjectDO() {
        return ProjectDO.builder().projectId(PROJECT_ID).ownerId("test").build();
    }

    @Test
    void updateProject() throws Exception {
        assertResponseWithEmptyData(() -> {
            UpdateProjectRequest request = FakerUtils.fake(UpdateProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(buildProjectDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "updateProject", UpdateProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void projectArchive() throws Exception {
        assertResponseWithEmptyData(() -> {
            ArchiveProjectRequest request = FakerUtils.fake(ArchiveProjectRequest.class);
            request.setProjectId(PROJECT_ID);
            ProjectDO projectDO = buildProjectDO();
            projectDO.setStatus(ProjectStatusEnum.REVIEWING.getCode());
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ARCHIVE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(anyString())).thenReturn(Optional.of(projectDO));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "projectArchive", ArchiveProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * Project Participants test
     *
     * @throws Exception
     */
    @Test
    void projectParticipants() throws Exception {
        assertResponse(() -> {

            String voteId = "vote123";
            ProjectParticipantsRequest request = new ProjectParticipantsRequest(voteId);
            VoteRequestDO voteRequestDO = new VoteRequestDO();
            VoteRequestDO.PartyVoteInfo partyVoteInfo = new VoteRequestDO.PartyVoteInfo("inst_id", "action", "reason");
            voteRequestDO.setPartyVoteInfos(Collections.singleton(partyVoteInfo));
            VoteRequestBody voteRequestBody = VoteRequestBody.builder().initiator("alice").build();
            String voteRequestBodyBase64 = Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes());
            VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder().body(voteRequestBodyBase64).build();
            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteRequestRepository.findById(voteId)).thenReturn(Optional.of(voteRequestDO));
            ParticipantNodeInstVO participantNodeInstVO = new ParticipantNodeInstVO();
            participantNodeInstVO.setInitiatorNodeId("alice");
            participantNodeInstVO.setInitiatorNodeName("alicename");
            participantNodeInstVO.setInvitees(List.of(new ParticipantNodeInstVO.NodeInstVO("bob", "bobname", "bob-inst", "bob-inst-name")));

            List<ParticipantNodeInstVO> participantNodeInstVOS = new ArrayList<>();
            participantNodeInstVOS.add(participantNodeInstVO);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId("project123");
            projectApprovalConfigDO.setParticipantNodeInfo(participantNodeInstVOS);
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(NodeDO.builder().name("alice-inst").instId("inst_id").build());
            Mockito.when(projectApprovalConfigRepository.findById(voteId)).thenReturn(Optional.of(projectApprovalConfigDO));
            Mockito.when(instRepository.findByInstIdIn(anyList())).thenReturn(List.of(InstDO.builder().instId("inst_id").name("inst_name").build()));
            Mockito.when(instRepository.findByInstId("inst_id")).thenReturn(InstDO.builder().instId("inst_id").name("inst_name").build());
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("Test Project");
            projectDO.setDescription("Test Description");
            projectDO.setComputeFunc("Test Compute Func");
            projectDO.setComputeMode("Test Compute Mode");
            Mockito.when(instRepository.findByInstId(voteRequestBody.getInitiator())).thenReturn(InstDO.builder().instId("inst_id").name("inst_name").build());
            Mockito.when(projectRepository.findById("project123")).thenReturn(Optional.of(projectDO));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "projectParticipants", ProjectParticipantsRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    /**
     * Project Participants test error VOTE_NOT_EXISTS
     *
     * @throws Exception
     */
    @Test
    void projectParticipantsError_VOTE_NOT_EXISTS() throws Exception {
        assertErrorCode(() -> {
            String voteId = "vote123";
            ProjectParticipantsRequest request = new ProjectParticipantsRequest(voteId);
            VoteRequestDO voteRequestDO = new VoteRequestDO();
            VoteRequestDO.PartyVoteInfo partyVoteInfo = new VoteRequestDO.PartyVoteInfo("inst_id", "action", "reason");
            voteRequestDO.setPartyVoteInfos(Collections.singleton(partyVoteInfo));
            VoteRequestBody voteRequestBody = VoteRequestBody.builder().initiator("alice").build();
            String voteRequestBodyBase64 = Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes());
            VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder().body(voteRequestBodyBase64).build();
            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteRequestRepository.findById(voteId)).thenReturn(Optional.empty());
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId("project123");
            projectApprovalConfigDO.setParticipantNodeInfo(new ArrayList<>());
            Mockito.when(projectApprovalConfigRepository.findById(voteId)).thenReturn(Optional.of(projectApprovalConfigDO));
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("Test Project");
            projectDO.setDescription("Test Description");
            projectDO.setComputeFunc("Test Compute Func");
            projectDO.setComputeMode("Test Compute Mode");
            Mockito.when(projectRepository.findById("project123")).thenReturn(Optional.of(projectDO));

            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "projectParticipants", ProjectParticipantsRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, VoteErrorCode.VOTE_NOT_EXISTS);
    }

    /**
     * Project Participants test error  ProjectApprovalConfig VOTE_NOT_EXISTS
     *
     * @throws Exception
     */
    @Test
    void projectParticipantsError_PROJECT_APPROVAL_NOT_EXISTS() throws Exception {
        assertErrorCode(() -> {
            String voteId = "vote123";
            ProjectParticipantsRequest request = new ProjectParticipantsRequest(voteId);
            VoteRequestDO voteRequestDO = new VoteRequestDO();
            VoteRequestDO.PartyVoteInfo partyVoteInfo = new VoteRequestDO.PartyVoteInfo("inst_id", "action", "reason");
            voteRequestDO.setPartyVoteInfos(Collections.singleton(partyVoteInfo));
            VoteRequestBody voteRequestBody = VoteRequestBody.builder().initiator("alice").build();
            String voteRequestBodyBase64 = Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes());
            VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder().body(voteRequestBodyBase64).build();
            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteRequestRepository.findById(voteId)).thenReturn(Optional.of(voteRequestDO));
            ParticipantNodeInstVO participantNodeInstVO = new ParticipantNodeInstVO();
            participantNodeInstVO.setInitiatorNodeId("alice");
            participantNodeInstVO.setInitiatorNodeName("alicename");
            participantNodeInstVO.setInvitees(List.of(new ParticipantNodeInstVO.NodeInstVO("bob", "bobname", "bob-inst", "bob-inst-name")));

            List<ParticipantNodeInstVO> participantNodeInstVOS = new ArrayList<>();
            participantNodeInstVOS.add(participantNodeInstVO);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId("project123");
            projectApprovalConfigDO.setParticipantNodeInfo(participantNodeInstVOS);
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(NodeDO.builder().name("alice-inst").instId("inst_id").build());
            Mockito.when(projectApprovalConfigRepository.findById(voteId)).thenReturn(Optional.empty());
            Mockito.when(instRepository.findByInstIdIn(anyList())).thenReturn(List.of(InstDO.builder().instId("inst_id").name("inst_name").build()));
            Mockito.when(instRepository.findByInstId("inst_id")).thenReturn(InstDO.builder().instId("inst_id").name("inst_name").build());
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("Test Project");
            projectDO.setDescription("Test Description");
            projectDO.setComputeFunc("Test Compute Func");
            projectDO.setComputeMode("Test Compute Mode");
            Mockito.when(instRepository.findByInstId(voteRequestBody.getInitiator())).thenReturn(InstDO.builder().instId("inst_id").name("inst_name").build());
            Mockito.when(projectRepository.findById("project123")).thenReturn(Optional.empty());

            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "projectParticipants", ProjectParticipantsRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, VoteErrorCode.VOTE_NOT_EXISTS);
    }


    /**
     * Project Participants test error PROJECT_NOT_EXISTS
     *
     * @throws Exception
     */

    @Test
    void projectParticipantsError_PROJECT_NOT_EXISTS() throws Exception {
        assertErrorCode(() -> {
            String voteId = "vote123";
            ProjectParticipantsRequest request = new ProjectParticipantsRequest(voteId);
            VoteRequestDO voteRequestDO = new VoteRequestDO();
            VoteRequestDO.PartyVoteInfo partyVoteInfo = new VoteRequestDO.PartyVoteInfo("inst_id", "action", "reason");
            voteRequestDO.setPartyVoteInfos(Collections.singleton(partyVoteInfo));
            VoteRequestBody voteRequestBody = VoteRequestBody.builder().initiator("alice").build();
            String voteRequestBodyBase64 = Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes());
            VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder().body(voteRequestBodyBase64).build();
            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteRequestRepository.findById(voteId)).thenReturn(Optional.of(voteRequestDO));
            ParticipantNodeInstVO participantNodeInstVO = new ParticipantNodeInstVO();
            participantNodeInstVO.setInitiatorNodeId("alice");
            participantNodeInstVO.setInitiatorNodeName("alicename");
            participantNodeInstVO.setInvitees(List.of(new ParticipantNodeInstVO.NodeInstVO("bob", "bobname", "bob-inst", "bob-inst-name")));

            List<ParticipantNodeInstVO> participantNodeInstVOS = new ArrayList<>();
            participantNodeInstVOS.add(participantNodeInstVO);
            ProjectApprovalConfigDO projectApprovalConfigDO = new ProjectApprovalConfigDO();
            projectApprovalConfigDO.setProjectId("project123");
            projectApprovalConfigDO.setParticipantNodeInfo(participantNodeInstVOS);
            Mockito.when(nodeRepository.findByNodeId(anyString())).thenReturn(NodeDO.builder().name("alice-inst").instId("inst_id").build());
            Mockito.when(projectApprovalConfigRepository.findById(voteId)).thenReturn(Optional.of(projectApprovalConfigDO));
            Mockito.when(instRepository.findByInstIdIn(anyList())).thenReturn(List.of(InstDO.builder().instId("inst_id").name("inst_name").build()));
            Mockito.when(instRepository.findByInstId("inst_id")).thenReturn(InstDO.builder().instId("inst_id").name("inst_name").build());
            ProjectDO projectDO = new ProjectDO();
            projectDO.setName("Test Project");
            projectDO.setDescription("Test Description");
            projectDO.setComputeFunc("Test Compute Func");
            projectDO.setComputeMode("Test Compute Mode");
            Mockito.when(instRepository.findByInstId(voteRequestBody.getInitiator())).thenReturn(InstDO.builder().instId("inst_id").name("inst_name").build());
            Mockito.when(projectRepository.findById("project123")).thenReturn(Optional.empty());

            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "projectParticipants", ProjectParticipantsRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

}
