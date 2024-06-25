/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.service.handler.vote;

import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.AbstractVoteConfig;
import org.secretflow.secretpad.service.model.message.AbstractVoteTypeMessage;
import org.secretflow.secretpad.service.model.message.MessageDetailVO;
import org.secretflow.secretpad.service.model.message.PartyVoteStatus;

import java.util.List;

/**
 * VoteTypeHandler.
 *
 * @author cml
 * @date 2023/09/28
 */
public interface VoteTypeHandler {

    MessageDetailVO getVoteMessageDetail(Boolean isInitiator, String nodeID, String voteID);

    List<VoteTypeEnum> supportTypes();

    AbstractVoteTypeMessage getMessageListNecessaryInfo(String voteID);

    void createApproval(String nodeID, AbstractVoteConfig voteConfig);

    List<? extends PartyVoteStatus> getPartyStatusByVoteID(String voteID);

    void doCallBackApproved(VoteRequestDO voteRequestDO);

    void doCallBackRejected(VoteRequestDO voteRequestDO);

    void flushVoteStatus(String voteID);
}
