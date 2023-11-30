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

package org.secretflow.secretpad.service.handler;

import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteExecuteEnum;
import org.secretflow.secretpad.service.model.message.PartyVoteStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AbstractVoteTypeHandler.
 *
 * @author cml
 * @date 2023/10/09
 */
public abstract class AbstractVoteTypeHandler implements VoteTypeHandler {


    protected final VoteInviteRepository voteInviteRepository;

    protected final VoteRequestRepository voteRequestRepository;

    protected final NodeRepository nodeRepository;

    protected final EnvService envService;


    public AbstractVoteTypeHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, EnvService envService) {
        this.voteInviteRepository = voteInviteRepository;
        this.voteRequestRepository = voteRequestRepository;
        this.nodeRepository = nodeRepository;
        this.envService = envService;
    }

    @Override
    public List<PartyVoteStatus> getPartyStatusByVoteID(String voteID) {
        List<VoteInviteDO> voteInviteDOList = voteInviteRepository.findByVoteID(voteID);
        List<String> partyNodes = voteInviteDOList.stream().map(e -> e.getUpk().getVotePartitionID()).collect(Collectors.toList());
        List<NodeDO> nodeDOS = nodeRepository.findByNodeIdIn(partyNodes);
        Map<String, String> nodeMap = nodeDOS.stream().collect(Collectors.toMap(e -> e.getNodeId(), e -> e.getName()));
        List<PartyVoteStatus> partyVoteStatusList = new ArrayList<>();
        voteInviteDOList.stream().forEach(e -> {
            PartyVoteStatus partyVoteStatus = PartyVoteStatus.builder()
                    .reason(e.getReason())
                    .action(e.getAction())
                    .nodeID(e.getUpk().getVotePartitionID())
                    .nodeName(nodeMap.get(e.getUpk().getVotePartitionID()))
                    .build();
            partyVoteStatusList.add(partyVoteStatus);
        });
        return partyVoteStatusList;
    }

    protected void success(VoteRequestDO voteRequestDO) {
        voteRequestDO.setExecuteStatus(VoteExecuteEnum.SUCCESS.name());
        voteRequestRepository.saveAndFlush(voteRequestDO);
    }

    protected void failed(VoteRequestDO voteRequestDO) {
        voteRequestDO.setExecuteStatus(VoteExecuteEnum.FAILED.name());
        voteRequestRepository.saveAndFlush(voteRequestDO);
    }

    protected void observer(VoteRequestDO voteRequestDO) {
        voteRequestDO.setExecuteStatus(VoteExecuteEnum.OBSERVER.name());
        voteRequestRepository.saveAndFlush(voteRequestDO);
    }
}
