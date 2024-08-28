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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.schedule.VoteInviteStatusMonitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;

/**
 * @author yutu
 * @date 2024/07/05
 */
@ExtendWith(MockitoExtension.class)
public class VoteInviteStatusMonitorTest {
    @Spy
    VoteInviteStatusMonitor voteInviteStatusMonitor;

    @Mock
    private VoteRequestRepository voteRequestRepository;
    @Mock
    private VoteInviteRepository voteInviteRepository;
    @Mock
    private EnvService envService;

    @Mock
    private NodeRepository nodeRepository;

    @BeforeEach
    public void setup() {
        UserContextDTO userContextDTO = UserContextDTO.builder()
                .ownerId("alice")
                .platformType(PlatformTypeEnum.AUTONOMY)
                .build();
        UserContext.setBaseUser(userContextDTO);
    }

    @Test
    void test() {
        voteInviteStatusMonitor.setVoteRequestRepository(voteRequestRepository);
        voteInviteStatusMonitor.setVoteInviteRepository(voteInviteRepository);
        voteInviteStatusMonitor.setEnvService(envService);
        voteInviteStatusMonitor.setNodeRepository(nodeRepository);
        VoteRequestDO voteRequestDO = new VoteRequestDO();
        voteRequestDO.setType(VoteTypeEnum.PROJECT_CREATE.name());
        voteRequestDO.setExecutors(List.of("alice"));
        voteRequestDO.setVoteID("vote");
        voteRequestDO.setInitiator("alice");
        VoteRequestDO.PartyVoteInfo partyVoteInfo = VoteRequestDO.PartyVoteInfo.builder().action(null).partyId("alice").reason(null).build();
        HashSet<VoteRequestDO.PartyVoteInfo> partyVoteInfos = new HashSet<>();
        partyVoteInfos.add(partyVoteInfo);
        voteRequestDO.setPartyVoteInfos(partyVoteInfos);
        List<VoteRequestDO> voteRequestDOS = List.of(voteRequestDO);
        Mockito.when(voteRequestRepository.findByStatus(Mockito.anyInt())).thenReturn(voteRequestDOS);
        NodeDO alice1 = NodeDO.builder().instId("alice").nodeId("alice1").build();
        NodeDO alice2 = NodeDO.builder().instId("alice").nodeId("alice2").build();
        Mockito.when(nodeRepository.findByInstId("alice")).thenReturn(List.of(alice1,alice2));
        VoteInviteDO voteInviteDO = new VoteInviteDO();
        voteInviteDO.setUpk(new VoteInviteDO.UPK("1", "alice"));
        voteInviteDO.setAction(VoteStatusEnum.APPROVED.name());
        List<VoteInviteDO> voteInviteDOS = List.of(voteInviteDO);
        Mockito.when(voteInviteRepository.findByVoteID(Mockito.any())).thenReturn(voteInviteDOS);
        voteInviteStatusMonitor.sync();
    }
}