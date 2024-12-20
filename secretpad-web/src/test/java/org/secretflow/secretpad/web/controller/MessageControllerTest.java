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
import org.secretflow.secretpad.common.util.PageUtils;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteApprovalConfigDO;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteAuditConfigRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.message.MessageDetailRequest;
import org.secretflow.secretpad.service.model.message.MessageListRequest;
import org.secretflow.secretpad.service.model.message.MessagePendingCountRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
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


    @Test
    public void list() throws Exception {
        assertResponse(() -> {
            MessageListRequest messageListRequest = FakerUtils.fake(MessageListRequest.class);
            messageListRequest.setOwnerId("alice");
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
            messageListRequest.setOwnerId("alice");
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
            messageListRequest.setOwnerId("alice");
            messageListRequest.setVoteType(VoteTypeEnum.NODE_ROUTE.name());
            NodeRouteApprovalConfigDO nodeRouteApprovalConfigDO = FakerUtils.fake(NodeRouteApprovalConfigDO.class);
            VoteRequestDO voteRequestDO = FakerUtils.fake(VoteRequestDO.class);
            VoteInviteDO v = FakerUtils.fake(VoteInviteDO.class);

            Mockito.when(nodeRouteAuditConfigRepository.findById(messageListRequest.getVoteId())).thenReturn(Optional.of(nodeRouteApprovalConfigDO));
            Mockito.when(voteRequestRepository.findById(messageListRequest.getVoteId())).thenReturn(Optional.of(voteRequestDO));
            Mockito.when(voteInviteRepository.findById(new VoteInviteDO.UPK(messageListRequest.getVoteId(), nodeRouteApprovalConfigDO.getDesNodeID()))).thenReturn(Optional.of(v));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(FakerUtils.fake(NodeDO.class));
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "detail", MessageDetailRequest.class))
                    .content(JsonUtils.toJSONString(messageListRequest));
        });
    }

    @Test
    public void pendingCount() throws Exception {
        assertResponse(() -> {
            MessagePendingCountRequest messagePendingCountRequest = new MessagePendingCountRequest();
            messagePendingCountRequest.setOwnerId("alice");
            return MockMvcRequestBuilders.post(getMappingUrl(MessageController.class, "pending", MessagePendingCountRequest.class))
                    .content(JsonUtils.toJSONString(messagePendingCountRequest));
        });
    }

    @Test
    public void testConvertEmptyInputAndNonEmptyFunction() {
        List<Integer> input = Arrays.asList(1, 2, 3);
        List<String> output = PageUtils.convert(input, e -> null);
        Assertions.assertEquals(0, output.size());
    }
}
