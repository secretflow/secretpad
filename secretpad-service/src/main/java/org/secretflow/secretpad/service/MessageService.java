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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.message.MessageDetailVO;
import org.secretflow.secretpad.service.model.message.MessageListVO;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;

/**
 * MessageService.
 *
 * @author cml
 * @date 2023/09/20
 */
public interface MessageService {

    void reply(String action, String reason, @NotBlank String voteParticipantID, @NotBlank String voteID);

    MessageListVO list(Boolean isInitiator, String nodeID, String type, String keyWord, Boolean isProcessed, Pageable of);

    MessageDetailVO detail(Boolean isInitiator, String nodeID, String voteID, String voteType);

    Long pendingCount(String nodeID);

}
