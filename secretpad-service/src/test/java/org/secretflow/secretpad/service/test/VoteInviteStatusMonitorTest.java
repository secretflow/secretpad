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

import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.schedule.VoteInviteStatusMonitor;

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

    @Test
    void test() {
        voteInviteStatusMonitor.setVoteRequestRepository(voteRequestRepository);
        voteInviteStatusMonitor.setVoteInviteRepository(voteInviteRepository);
        voteInviteStatusMonitor.setEnvService(envService);

        VoteRequestDO voteRequestDO = new VoteRequestDO();
        voteRequestDO.setType(VoteTypeEnum.PROJECT_CREATE.name());
        voteRequestDO.setExecutors(List.of("alice"));
        voteRequestDO.setInitiator("alice");
        VoteRequestDO.PartyVoteInfo partyVoteInfo = VoteRequestDO.PartyVoteInfo.builder().action(null).nodeId("alice").reason(null).build();
        HashSet<VoteRequestDO.PartyVoteInfo> partyVoteInfos = new HashSet<>();
        partyVoteInfos.add(partyVoteInfo);
        voteRequestDO.setPartyVoteInfos(partyVoteInfos);
        List<VoteRequestDO> voteRequestDOS = List.of(voteRequestDO);
        Mockito.when(voteRequestRepository.findByStatus(Mockito.anyInt())).thenReturn(voteRequestDOS);

        VoteInviteDO voteInviteDO = new VoteInviteDO();
        voteInviteDO.setUpk(new VoteInviteDO.UPK("1", "alice"));
        voteInviteDO.setAction(VoteStatusEnum.APPROVED.name());
        List<VoteInviteDO> voteInviteDOS = List.of(voteInviteDO);
        Mockito.when(voteInviteRepository.findByVoteID(Mockito.any())).thenReturn(voteInviteDOS);

        Mockito.when(envService.isCurrentNodeEnvironment(Mockito.any())).thenReturn(true);

        Mockito.doReturn(false).when(voteInviteStatusMonitor).verify(Mockito.any(), Mockito.any());

        voteInviteStatusMonitor.sync();
    }
}