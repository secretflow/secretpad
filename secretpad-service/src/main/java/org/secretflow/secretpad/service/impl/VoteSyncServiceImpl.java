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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.persistence.repository.TeeNodeDatatableManagementRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.VoteSyncService;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.model.datasync.vote.TeeNodeDatatableManagementSyncRequest;
import org.secretflow.secretpad.service.model.datasync.vote.VoteInviteDataSyncRequest;

import org.springframework.stereotype.Service;

/**
 * VoteSyncServiceImpl.
 *
 * @author cml
 * @date 2023/11/02
 */
@Service
public class VoteSyncServiceImpl implements VoteSyncService {
    private final VoteRequestRepository voteRequestRepository;

    private final VoteInviteRepository voteInviteRepository;

    private final NodeRouteRepository nodeRouteRepository;

    private final TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository;

    public VoteSyncServiceImpl(VoteRequestRepository voteRequestRepository, VoteInviteRepository voteInviteRepository,
                               NodeRouteRepository nodeRouteRepository, TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository) {
        this.voteRequestRepository = voteRequestRepository;
        this.voteInviteRepository = voteInviteRepository;
        this.nodeRouteRepository = nodeRouteRepository;
        this.teeNodeDatatableManagementRepository = teeNodeDatatableManagementRepository;
    }

    @Override
    public void sync(String syncDataType, ProjectNodesInfo projectNodesInfo) {
        if (VoteSyncTypeEnum.VOTE_REQUEST.name().equals(syncDataType)) {
            voteRequestRepository.save((VoteRequestDO) projectNodesInfo);
        } else if (VoteSyncTypeEnum.VOTE_INVITE.name().equals(syncDataType)) {
            VoteInviteDataSyncRequest voteInviteDataSyncRequest = (VoteInviteDataSyncRequest) projectNodesInfo;
            voteInviteRepository.save(VoteInviteDataSyncRequest.parse2DO(voteInviteDataSyncRequest));
        } else if (VoteSyncTypeEnum.NODE_ROUTE.name().equals(syncDataType)) {
            nodeRouteRepository.save((NodeRouteDO) projectNodesInfo);
        } else if (VoteSyncTypeEnum.TEE_NODE_DATATABLE_MANAGEMENT.name().equals(syncDataType)) {
            TeeNodeDatatableManagementSyncRequest teeNodeDatatableManagementSyncRequest =
                    (TeeNodeDatatableManagementSyncRequest) projectNodesInfo;
            TeeNodeDatatableManagementDO mngDO = TeeNodeDatatableManagementSyncRequest.parse2DO(teeNodeDatatableManagementSyncRequest);
            teeNodeDatatableManagementRepository.save(mngDO);
        }
    }
}
