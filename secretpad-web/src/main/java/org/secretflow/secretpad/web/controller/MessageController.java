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

import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.service.MessageService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.message.*;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MessageController.
 *
 * @author cml
 * @date 2023/09/20
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(MessageController.class);


    /**
     * vote invite reply
     *
     * @param voteReplyRequest
     * @return
     */
    @PostMapping(value = "/reply", consumes = "application/json")
    @DataResource(field = "voteParticipantID", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<Object> reply(@Valid @RequestBody VoteReplyRequest voteReplyRequest) {
        messageService.reply(voteReplyRequest.getAction(), voteReplyRequest.getReason(), voteReplyRequest.getVoteParticipantID(), voteReplyRequest.getVoteID());
        return SecretPadResponse.success();
    }

    /**
     * vote info list
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/list", consumes = "application/json")
    @DataResource(field = "nodeID", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<MessageListVO> list(@Valid @RequestBody MessageListRequest request) {
        LOGGER.info("params = {}", JsonUtils.toJSONString(request));
        MessageListVO messageListVO = messageService.list(request.getIsInitiator(), request.getNodeID(), request.getType(), request.getKeyWord(), request.getIsProcessed(), request.of());
        return SecretPadResponse.success(messageListVO);
    }

    /**
     * vote detail
     *
     * @param messageDetailRequest
     * @return
     */
    @PostMapping(value = "/detail", consumes = "application/json")
    @DataResource(field = "nodeID", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<MessageDetailVO> detail(@Valid @RequestBody MessageDetailRequest messageDetailRequest) {
        MessageDetailVO messageDetailVO = messageService.detail(messageDetailRequest.getIsInitiator(), messageDetailRequest.getNodeID(), messageDetailRequest.getVoteID(), messageDetailRequest.getVoteType());
        return SecretPadResponse.success(messageDetailVO);
    }

    /**
     * the count of vote ,waiting for reply
     *
     * @param messagePendingCountRequest
     * @return
     */
    @PostMapping(value = "/pending", consumes = "application/json")
    @DataResource(field = "nodeID", resourceType = DataResourceTypeEnum.NODE_ID)
    public SecretPadResponse<Long> pending(@Valid @RequestBody MessagePendingCountRequest messagePendingCountRequest) {
        Long pendingCount = messageService.pendingCount(messagePendingCountRequest.getNodeID());
        return SecretPadResponse.success(pendingCount);
    }


}
