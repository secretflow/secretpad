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

import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.VoteSyncService;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.datasync.vote.TeeNodeDatatableManagementSyncRequest;

import org.springframework.stereotype.Service;

import java.util.List;

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

    private final ProjectRepository projectRepository;

    private final ProjectNodeRepository projectNodeRepository;

    private final ProjectApprovalConfigRepository projectApprovalConfigRepository;

    public VoteSyncServiceImpl(VoteRequestRepository voteRequestRepository, VoteInviteRepository voteInviteRepository,
                               NodeRouteRepository nodeRouteRepository, TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository,
                               ProjectRepository projectRepository, ProjectNodeRepository projectNodeRepository,
                               ProjectApprovalConfigRepository projectApprovalConfigRepository) {
        this.voteRequestRepository = voteRequestRepository;
        this.voteInviteRepository = voteInviteRepository;
        this.nodeRouteRepository = nodeRouteRepository;
        this.teeNodeDatatableManagementRepository = teeNodeDatatableManagementRepository;
        this.projectRepository = projectRepository;
        this.projectNodeRepository = projectNodeRepository;
        this.projectApprovalConfigRepository = projectApprovalConfigRepository;
    }

    @Override
    public void sync(List<DbSyncRequest> dbSyncRequests) {
        for (DbSyncRequest dbSyncRequest : dbSyncRequests) {
            String syncDataType = dbSyncRequest.getSyncDataType();
            ProjectNodesInfo projectNodesInfo = dbSyncRequest.getProjectNodesInfo();
            switch (VoteSyncTypeEnum.valueOf(syncDataType)) {
                case VOTE_REQUEST -> voteRequestRepository.save((VoteRequestDO) projectNodesInfo);
                case VOTE_INVITE -> voteInviteRepository.save((VoteInviteDO) projectNodesInfo);
                case NODE_ROUTE -> nodeRouteRepository.save((NodeRouteDO) projectNodesInfo);
                case TEE_NODE_DATATABLE_MANAGEMENT -> {
                    TeeNodeDatatableManagementSyncRequest teeNodeDatatableManagementSyncRequest =
                            (TeeNodeDatatableManagementSyncRequest) projectNodesInfo;
                    TeeNodeDatatableManagementDO mngDO = TeeNodeDatatableManagementSyncRequest.parse2DO(teeNodeDatatableManagementSyncRequest);
                    teeNodeDatatableManagementRepository.save(mngDO);
                }
                case PROJECT -> projectRepository.save((ProjectDO) projectNodesInfo);
                case PROJECT_NODE -> projectNodeRepository.save((ProjectNodeDO) projectNodesInfo);
                case PROJECT_APPROVAL_CONFIG ->
                        projectApprovalConfigRepository.save((ProjectApprovalConfigDO) projectNodesInfo);
            }

        }

    }
}
