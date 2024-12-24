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

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.model.message.VoteReplyRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.secretflow.secretpad.common.errorcode.VoteErrorCode.*;
import static org.secretflow.secretpad.service.enums.VoteTypeEnum.PROJECT_CREATE;

/**
 * MessageControllerTest.
 *
 * @author cml
 * @date 2023/11/10
 * @since 4.3
 */
@TestPropertySource(properties = {
        "secretpad.platform-type=AUTONOMY",
        "secretpad.node-id=alice",
})
public class P2pMessageControllerTest extends ControllerTest {

    @MockBean
    private VoteInviteRepository voteInviteRepository;

    @MockBean
    private VoteRequestRepository voteRequestRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private NodeRouteManager nodeRouteManager;

    @Test
    public void replyVoteNotExists() throws Exception {
        assertErrorCode(() -> {
            String requestBody = """
                    {
                      "action": "APPROVED",
                      "reason": "",
                      "voteId": "5470dd07cf4c4281906941fc74dd0440",
                      "voteParticipantId": "nodeId"
                    }
                    """;
            VoteReplyRequest voteReplyRequest = JsonUtils.toJavaObject(requestBody, VoteReplyRequest.class);
            NodeDO fake = FakerUtils.fake(NodeDO.class);
            fake.setInstId("nodeId");
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(fake);
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(JsonUtils.toJSONString(voteReplyRequest));
        }, VOTE_NOT_EXISTS);
    }

    @Test
    public void replyInstNotFound() throws Exception {
        assertErrorCode(() -> {
            String requestBody = """
                    {
                      "action": "APPROVED",
                      "reason": "",
                      "voteId": "5470dd07cf4c4281906941fc74dd0440",
                      "voteParticipantId": "nodeId"
                    }
                    """;
            VoteReplyRequest voteReplyRequest = JsonUtils.toJavaObject(requestBody, VoteReplyRequest.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);
            v.setType(PROJECT_CREATE.name());
            NodeDO fake = FakerUtils.fake(NodeDO.class);
            fake.setInstId("nodeId");
            Mockito.when(voteRequestRepository.findById(voteReplyRequest.getVoteId())).thenReturn(Optional.of(voteRequestDO));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(voteReplyRequest.getVoteId(), voteReplyRequest.getVoteParticipantId()))).thenReturn(Optional.of(v));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(fake);
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(JsonUtils.toJSONString(voteReplyRequest));
        }, VOTE_INITIATOR_INST_NOT_FOUND);
    }

    @Test
    public void replyMasterRouteNotReady() throws Exception {
        assertErrorCode(() -> {
            String requestBody = """
                    {
                      "action": "APPROVED",
                      "reason": "",
                      "voteId": "5470dd07cf4c4281906941fc74dd0440",
                      "voteParticipantId": "nodeId"
                    }
                    """;
            VoteReplyRequest voteReplyRequest = JsonUtils.toJavaObject(requestBody, VoteReplyRequest.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);
            v.setType(PROJECT_CREATE.name());
            NodeDO fake = FakerUtils.fake(NodeDO.class);
            fake.setInstId("nodeId");
            Mockito.when(voteRequestRepository.findById(voteReplyRequest.getVoteId())).thenReturn(Optional.of(voteRequestDO));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(voteReplyRequest.getVoteId(), voteReplyRequest.getVoteParticipantId()))).thenReturn(Optional.of(v));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(FakerUtils.fake(NodeDO.class));
            Mockito.when(nodeRepository.findByInstId(Mockito.anyString())).thenReturn(List.of(fake));
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(JsonUtils.toJSONString(voteReplyRequest));
        }, VOTE_MASTER_ROUTE_NOT_READY);
    }

    @Test
    public void reply() throws Exception {
        assertErrorCode(() -> {
            String requestBody = """
                    {
                      "action": "APPROVED",
                      "reason": "",
                      "voteId": "5470dd07cf4c4281906941fc74dd0440",
                      "voteParticipantId": "nodeId"
                    }
                    """;
            VoteReplyRequest voteReplyRequest = JsonUtils.toJavaObject(requestBody, VoteReplyRequest.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);
            v.setType(PROJECT_CREATE.name());
            Mockito.when(voteRequestRepository.findById(voteReplyRequest.getVoteId())).thenReturn(Optional.of(voteRequestDO));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(voteReplyRequest.getVoteId(), voteReplyRequest.getVoteParticipantId()))).thenReturn(Optional.of(v));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(FakerUtils.fake(NodeDO.class));
            Mockito.when(nodeRepository.findByInstId(Mockito.anyString())).thenReturn(List.of(FakerUtils.fake(NodeDO.class)));
            Mockito.when(nodeRouteManager.checkDomainRouterExistsInKuscia(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(JsonUtils.toJSONString(voteReplyRequest));
        }, VOTE_MASTER_ROUTE_NOT_READY);
    }
}
