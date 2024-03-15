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
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteApprovalConfigDO;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteAuditConfigRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.message.MessageDetailRequest;
import org.secretflow.secretpad.service.model.message.MessageListRequest;
import org.secretflow.secretpad.service.model.message.MessagePendingCountRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

/**
 * MessageControllerTest.
 *
 * @author cml
 * @date 2023/11/10
 * @since 4.3
 */
public class MessageControllerTest extends ControllerTest {

    @MockBean
    private VoteInviteRepository voteInviteRepository;

    @MockBean
    private VoteRequestRepository voteRequestRepository;

    @MockBean
    private NodeRouteAuditConfigRepository nodeRouteAuditConfigRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private CertificateService certificateService;

    /*@Test
    public void replyErr() throws Exception {
        assertErrorCode(() -> {
            VoteReplyRequest voteReplyRequest = new VoteReplyRequest();
            voteReplyRequest.setAction(VoteStatusEnum.APPROVED.name());
            voteReplyRequest.setVoteID(UUIDUtils.newUUID());
            voteReplyRequest.setVoteParticipantID("alice");
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteRequestMessage voteRequestMessage = new VoteRequestMessage();
            voteRequestMessage.setBody("body");
            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(voteReplyRequest.getVoteID(), voteReplyRequest.getVoteParticipantID()))).thenReturn(Optional.of(v));
            Mockito.when(voteRequestRepository.findById(voteReplyRequest.getVoteID())).thenReturn(Optional.of(voteRequestDO));
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(JsonUtils.toJSONString(voteReplyRequest));
        }, VoteErrorCode.VOTE_SIGNATURE_SYNCHRONIZING);
    }

    @Test
    public void replyErr1() throws Exception {
        assertErrorCode(() -> {
            VoteReplyRequest voteReplyRequest = new VoteReplyRequest();
            voteReplyRequest.setAction(VoteStatusEnum.APPROVED.name());
            voteReplyRequest.setVoteID(UUIDUtils.newUUID());
            voteReplyRequest.setVoteParticipantID("alice");
            String string = JsonUtils.toJSONString(voteReplyRequest);
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteRequestMessage voteRequestMessage = new VoteRequestMessage();
            voteRequestMessage.setBody("aaa");
            voteRequestMessage.setVoteRequestSignature("aaa==");
            MockedStatic<Base64Utils> base64UtilsMockedStatic = Mockito.mockStatic(Base64Utils.class);
            base64UtilsMockedStatic.when(() -> Base64Utils.decode(Mockito.anyString())).thenReturn("aaa".getBytes());
            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(voteReplyRequest.getVoteID(), voteReplyRequest.getVoteParticipantID()))).thenReturn(Optional.of(v));
            Mockito.when(voteRequestRepository.findById(voteReplyRequest.getVoteID())).thenReturn(Optional.of(voteRequestDO));

            MockedStatic<JsonUtils> jsonUtilsMockedStatic = Mockito.mockStatic(JsonUtils.class);
            VoteRequestBody voteRequestBody = FakerUtils.fake(VoteRequestBody.class);
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJavaObject(Mockito.anyString(), Mockito.eq(VoteRequestBody.class))).thenReturn(voteRequestBody);
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJSONString(Mockito.any())).thenReturn("{}");
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJavaObject(Mockito.anyString(), Mockito.eq(VoteRequestMessage.class))).thenReturn(voteRequestMessage);
            SecretPadResponse secretPadResponse = new SecretPadResponse();
            secretPadResponse.setStatus(SecretPadResponse.SecretPadResponseStatus.builder().code(VoteErrorCode.VOTE_CHECK_FAILED.getCode()).build());


            jsonUtilsMockedStatic.when(() -> JsonUtils.toJavaObject(Mockito.anyString(), Mockito.eq(SecretPadResponse.class))).thenReturn(secretPadResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(string);
        }, VoteErrorCode.VOTE_CHECK_FAILED);
    }

    @Test
    public void replySuccess() throws Exception {
        assertResponseWithEmptyData(() -> {
            VoteReplyRequest voteReplyRequest = new VoteReplyRequest();
            voteReplyRequest.setAction(VoteStatusEnum.APPROVED.name());
            voteReplyRequest.setVoteID(UUIDUtils.newUUID());
            voteReplyRequest.setVoteParticipantID("bob");
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteRequestMessage voteRequestMessage = new VoteRequestMessage();
            voteRequestMessage.setBody("aaa");
            voteRequestMessage.setVoteRequestSignature("aaa===");
            String string = JsonUtils.toJSONString(voteReplyRequest);

            voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(voteReplyRequest.getVoteID(), voteReplyRequest.getVoteParticipantID()))).thenReturn(Optional.of(v));
            Mockito.when(voteRequestRepository.findById(voteReplyRequest.getVoteID())).thenReturn(Optional.of(voteRequestDO));
            Certificate.GenerateKeyCertsResponse generateKeyCertsResponse = Certificate.GenerateKeyCertsResponse.newBuilder().addCertChain(Base64Utils.encode("alice".getBytes())).addCertChain(Base64Utils.encode("bob".getBytes())).build();
            Mockito.when(certificateService.generateCertByNodeID("bob")).thenReturn(generateKeyCertsResponse);
            MockedStatic<EncryptUtils> mockStatic = Mockito.mockStatic(EncryptUtils.class);
            mockStatic.when(() -> EncryptUtils.signSHA256withRSA(Mockito.any(byte[].class), Mockito.anyString())).thenReturn("s");


            MockedStatic<JsonUtils> jsonUtilsMockedStatic = Mockito.mockStatic(JsonUtils.class);
            VoteRequestBody voteRequestBody = FakerUtils.fake(VoteRequestBody.class);
            voteRequestBody.setVoters(Lists.newArrayList("alice", "bob"));
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJavaObject(Mockito.anyString(), Mockito.eq(VoteRequestBody.class))).thenReturn(voteRequestBody);
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJSONString(Mockito.any())).thenReturn("{}");
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJavaObject(Mockito.anyString(), Mockito.eq(VoteRequestMessage.class))).thenReturn(voteRequestMessage);
            SecretPadResponse secretPadResponse = new SecretPadResponse();
            secretPadResponse.setStatus(SecretPadResponse.SecretPadResponseStatus.builder().code(0).build());
            jsonUtilsMockedStatic.when(() -> JsonUtils.toJavaObject(Mockito.anyString(), Mockito.eq(SecretPadResponse.class))).thenReturn(secretPadResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "reply", VoteReplyRequest.class))
                    .content(string);
        });
    }
*/
    @Test
    public void list() throws Exception {
        assertResponse(() -> {
            MessageListRequest messageListRequest = FakerUtils.fake(MessageListRequest.class);
            messageListRequest.setNodeID("alice");
            messageListRequest.setPage(1);
            messageListRequest.setSize(10);
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "list", MessageListRequest.class))
                    .content(JsonUtils.toJSONString(messageListRequest));
        });
    }

    @Test
    public void listCase2() throws Exception {
        assertResponse(() -> {
            MessageListRequest messageListRequest = FakerUtils.fake(MessageListRequest.class);
            messageListRequest.setIsInitiator(false);
            messageListRequest.setIsProcessed(false);
            messageListRequest.setNodeID("alice");
            messageListRequest.setPage(1);
            messageListRequest.setSize(10);
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "list", MessageListRequest.class))
                    .content(JsonUtils.toJSONString(messageListRequest));
        });
    }

    @Test
    public void detail() throws Exception {
        assertResponse(() -> {
            MessageDetailRequest messageListRequest = FakerUtils.fake(MessageDetailRequest.class);
            messageListRequest.setNodeID("alice");
            messageListRequest.setVoteType(VoteTypeEnum.NODE_ROUTE.name());
            NodeRouteApprovalConfigDO nodeRouteApprovalConfigDO = FakerUtils.fake(NodeRouteApprovalConfigDO.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);

            Mockito.when(nodeRouteAuditConfigRepository.findById(messageListRequest.getVoteID())).thenReturn(Optional.of(nodeRouteApprovalConfigDO));
            Mockito.when(voteRequestRepository.findById(messageListRequest.getVoteID())).thenReturn(Optional.of(voteRequestDO));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(messageListRequest.getVoteID(), nodeRouteApprovalConfigDO.getDesNodeID()))).thenReturn(Optional.of(v));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(FakerUtils.fake(NodeDO.class));
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "detail", MessageDetailRequest.class))
                    .content(JsonUtils.toJSONString(messageListRequest));
        });
    }


    @Test
    public void pendingCount() throws Exception {
        assertResponse(() -> {
            MessagePendingCountRequest messagePendingCountRequest = new MessagePendingCountRequest();
            messagePendingCountRequest.setNodeID("alice");
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "pending", MessagePendingCountRequest.class))
                    .content(JsonUtils.toJSONString(messagePendingCountRequest));
        });
    }
}
