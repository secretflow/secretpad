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

package org.secretflow.secretpad.service.model.datasync.vote;

import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

/**
 * VoteInviteDataSyncRequest.
 * represent VoteInviteDO
 *
 * @author cml
 * @date 2023/11/03
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoteInviteDataSyncRequest implements ProjectNodesInfo {
    
    private String voteID;

    private String votePartitionID;

    private String initiator;

    private String voteMsg;

    private String action;

    private String type;

    private String reason;

    private String desc;

    @JsonIgnore
    @Override
    public String getProjectId() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        return null;
    }

    public static VoteInviteDO parse2DO(VoteInviteDataSyncRequest voteInviteDataSyncRequest) {
        VoteInviteDO voteInviteDO = new VoteInviteDO();
        VoteInviteDO.UPK upk = new VoteInviteDO.UPK(voteInviteDataSyncRequest.getVoteID(), voteInviteDataSyncRequest.getVotePartitionID());
        voteInviteDO.setUpk(upk);
        voteInviteDO.setInitiator(voteInviteDataSyncRequest.getInitiator());
        voteInviteDO.setVoteMsg(voteInviteDataSyncRequest.getVoteMsg());
        voteInviteDO.setAction(voteInviteDataSyncRequest.getAction());
        voteInviteDO.setReason(voteInviteDataSyncRequest.getReason());
        voteInviteDO.setType(voteInviteDataSyncRequest.getType());
        voteInviteDO.setDesc(voteInviteDataSyncRequest.getDesc());
        return voteInviteDO;
    }

    public static VoteInviteDataSyncRequest parse2VO(VoteInviteDO voteInviteDO) {
        VoteInviteDataSyncRequest request = new VoteInviteDataSyncRequest();
        request.setVoteMsg(voteInviteDO.getVoteMsg());
        request.setAction(voteInviteDO.getAction());
        request.setReason(voteInviteDO.getReason());
        request.setType(voteInviteDO.getType());
        request.setDesc(voteInviteDO.getDesc());
        request.setVoteID(voteInviteDO.getUpk().getVoteID());
        request.setVotePartitionID(voteInviteDO.getUpk().getVotePartitionID());
        request.setInitiator(voteInviteDO.getInitiator());
        return request;
    }
}
