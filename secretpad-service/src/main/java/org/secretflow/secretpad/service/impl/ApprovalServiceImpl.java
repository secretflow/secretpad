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

import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.ApprovalService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.handler.VoteTypeHandler;
import org.secretflow.secretpad.service.model.approval.AbstractVoteConfig;
import org.secretflow.secretpad.service.model.approval.Participant;
import org.secretflow.secretpad.service.model.approval.PullStatusVO;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ApprovalServiceImpl.
 *
 * @author cml
 * @date 2023/09/19
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApprovalServiceImpl.class);

    private final VoteRequestRepository voteRequestRepository;

    private final TeeDownLoadAuditConfigRepository teeDownLoadAuditConfigRepository;

    private final VoteInviteRepository voteInviteRepository;

    private final NodeRepository nodeRepository;

    private final Map<VoteTypeEnum, VoteTypeHandler> voteTypeHandlerMap;

    private final ProjectJobRepository projectJobRepository;

    private final ProjectNodeRepository projectNodeRepository;

    private final EnvService envService;

    public ApprovalServiceImpl(VoteRequestRepository voteRequestRepository,
                               TeeDownLoadAuditConfigRepository teeDownLoadAuditConfigRepository,
                               NodeRepository nodeRepository,
                               Map<VoteTypeEnum, VoteTypeHandler> voteTypeHandlerMap,
                               VoteInviteRepository voteInviteRepository,
                               ProjectJobRepository projectJobRepository,
                               ProjectNodeRepository projectNodeRepository,
                               EnvService envService) {
        this.voteRequestRepository = voteRequestRepository;
        this.teeDownLoadAuditConfigRepository = teeDownLoadAuditConfigRepository;
        this.voteInviteRepository = voteInviteRepository;
        this.nodeRepository = nodeRepository;
        this.voteTypeHandlerMap = voteTypeHandlerMap;
        this.projectJobRepository = projectJobRepository;
        this.projectNodeRepository = projectNodeRepository;
        this.envService = envService;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createApproval(String nodeID, AbstractVoteConfig voteConfig, String voteType) {
        identityVerification(nodeID);
        //check node exists
        NodeDO nodeDO = nodeRepository.findByNodeId(nodeID);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("Cannot find node by nodeId {}.", nodeID);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        voteTypeHandlerMap.get(VoteTypeEnum.valueOf(voteType)).createApproval(nodeID, voteConfig);
    }

    @Override
    public PullStatusVO pullStatus(String projectID, String jobID, String taskID, String resourceID, String resourceType) {
        //todo interim plan:，all parties in project participate the vote. now we can not determine the job result belongs.
        List<ProjectNodeDO> projectNodeDOS = projectNodeRepository.findByProjectId(projectID);
        List<String> nodeIds = projectNodeDOS.stream().map(e -> e.getUpk().getNodeId()).collect(Collectors.toList());
        List<NodeDO> nodeDOS = nodeRepository.findByNodeIdIn(nodeIds);
        Map<String, String> nodeMap = nodeDOS.stream().collect(Collectors.toMap(e -> e.getNodeId(), e -> e.getName()));

        Optional<ProjectJobDO> projectJobDOOptional = projectJobRepository.findById(new ProjectJobDO.UPK(projectID, jobID));
        if (!projectJobDOOptional.isPresent()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
        }
        String graphID = projectJobDOOptional.get().getGraphId();
        //check if this resource has audit records

        List<TeeDownLoadAuditConfigDO> teeDownLoadAuditConfigDOS = teeDownLoadAuditConfigRepository.findByResourceIDAndResourceTypeAndTaskID(resourceID, resourceType, taskID);
        List<Participant> participants = new ArrayList<>();
        if (CollectionUtils.isEmpty(teeDownLoadAuditConfigDOS)) {
            projectNodeDOS.forEach(party -> {
                Participant participant = Participant.builder()
                        .status(VoteStatusEnum.NOT_INITIATED.name())
                        .nodeID(party.getUpk().getNodeId())
                        .nodeName(nodeMap.get(party.getUpk().getNodeId()))
                        .build();
                participants.add(participant);
            });
            return PullStatusVO.builder()
                    .taskID(taskID)
                    .jobID(jobID)
                    .graphID(graphID)
                    .resourceType(resourceType)
                    .resourceID(resourceID)
                    .parties(participants).build();
        }

        //total votes of current resource
        List<String> voteIds = teeDownLoadAuditConfigDOS.stream().map(e -> e.getVoteID()).collect(Collectors.toList());
        for (String party : nodeIds) {
            //get latest vote,filter by node id
            Optional<VoteRequestDO> optionalVoteRequestDO = voteRequestRepository.findFirstByInitiatorAndVoteIDInOrderByGmtCreateDESC(party, voteIds, VoteTypeEnum.TEE_DOWNLOAD.name());
            if (!optionalVoteRequestDO.isPresent()) {
                Participant participant = Participant.builder()
                        .status(VoteStatusEnum.NOT_INITIATED.name())
                        .nodeID(party)
                        .nodeName(nodeMap.get(party))
                        .build();
                participants.add(participant);
            } else {
                List<Participant.VoteInfo> voteInfos = new ArrayList<>();
                List<VoteInviteDO> voteInviteDOS = voteInviteRepository.findByVoteID(optionalVoteRequestDO.get().getVoteID());
                voteInviteDOS.forEach(invite -> {
                    Participant.VoteInfo voteInfo = Participant.VoteInfo.builder()
                            .action(invite.getAction())
                            .reason(invite.getReason())
                            .nodeID(invite.getUpk().getVotePartitionID())
                            .voteID(invite.getUpk().getVoteID())
                            .build();
                    voteInfos.add(voteInfo);
                });
                Participant participant = Participant.builder()
                        .status(VoteStatusEnum.parse(optionalVoteRequestDO.get().getStatus()))
                        .nodeID(party)
                        .nodeName(nodeMap.get(party))
                        .voteInfos(voteInfos)
                        .build();
                participants.add(participant);
            }

        }
        return PullStatusVO.builder()
                .taskID(taskID)
                .jobID(jobID)
                .graphID(graphID)
                .resourceType(resourceType)
                .resourceID(resourceID)
                .parties(participants)
                .build();
    }

    private void identityVerification(String nodeId) {
        if (envService.isAutonomy() && !StringUtils.equals(UserContext.getUser().getPlatformNodeId(), nodeId)) {
            throw SecretpadException.of(VoteErrorCode.VOTE_CHECK_FAILED);
        }
    }
}
