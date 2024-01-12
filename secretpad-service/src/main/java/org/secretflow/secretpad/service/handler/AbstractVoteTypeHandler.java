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

import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.EncryptUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteExecuteEnum;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.model.approval.AbstractVoteConfig;
import org.secretflow.secretpad.service.model.approval.VoteRequestBody;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.model.message.PartyVoteStatus;

import com.google.common.collect.Lists;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.springframework.util.CollectionUtils;

import java.util.*;
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

    protected final CertificateService certificateService;

    protected final ProjectRepository projectRepository;


    public AbstractVoteTypeHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, EnvService envService, CertificateService certificateService, ProjectRepository projectRepository) {
        this.voteInviteRepository = voteInviteRepository;
        this.voteRequestRepository = voteRequestRepository;
        this.nodeRepository = nodeRepository;
        this.envService = envService;
        this.certificateService = certificateService;
        this.projectRepository = projectRepository;
    }

    @Override
    public List<? extends PartyVoteStatus> getPartyStatusByVoteID(String voteID) {
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

    @Override
    public void doCallBackRejected(VoteRequestDO voteRequestDO) {
        success(voteRequestDO);
    }

    @Override
    public void flushVoteStatus(String voteID) {

    }

    /**
     * create approval, most of the implementation classes has the same behavior
     *
     * @param nodeID
     * @param voteConfig
     */
    @Override
    public void createApproval(String nodeID, AbstractVoteConfig voteConfig) {

        //step1 check it own special pre-check logic
        preCheck(nodeID, voteConfig);
        //step2 generate unit uuid
        String voteID = generateVoteID();
        //step3 create its own type of approval config
        createVoteConfig(voteID, nodeID, voteConfig);
        //step4 create voteRequestMsg
        String voteMsg = generateVoteRequestMsg(nodeID, voteConfig, voteID);
        //step5 create vote invite
        createVoteInvite(voteID, nodeID, voteConfig, voteMsg);
        //step6 create vote request
        createVoteRequest(voteID, nodeID, voteConfig, voteMsg);

    }

    protected String generateVoteRequestMsg(String nodeID, AbstractVoteConfig voteConfig, String voteID) {
        List<String> voters = getVoters(nodeID, voteConfig);
        VoteRequestBody voteRequestBody = VoteRequestBody.builder()
                .rejectedAction(getRejectAction(nodeID, voteConfig))
                .approvedAction(getApprovedAction(nodeID, voteConfig))
                .type(getVoteType())
                .approvedThreshold(voters.size())
                .initiator(nodeID)
                .voteRequestID(voteID)
                .voteCounter(nodeID)
                .voters(voters)
                .executors(Lists.newArrayList(nodeID))
                .build();
        Certificate.GenerateKeyCertsResponse generateKeyCertsResponse = certificateService.generateCertByNodeID(nodeID);
        String voteRequestBodyBase64 = Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes());
        String key = generateKeyCertsResponse.getKey();
        VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder()
                .body(voteRequestBodyBase64)
                .voteRequestSignature(EncryptUtils.signSHA256withRSA(voteRequestBodyBase64.getBytes(), key))
                .certChain(generateKeyCertsResponse.getCertChainList())
                .build();
        return JsonUtils.toJSONString(voteRequestMessage);
    }


    private String generateVoteID() {
        return UUIDUtils.newUUID();
    }

    protected abstract void preCheck(String nodeID, AbstractVoteConfig voteConfig);

    protected abstract void createVoteConfig(String voteID, String nodeID, AbstractVoteConfig voteConfig);

    private void createVoteInvite(String voteID, String nodeID, AbstractVoteConfig voteConfig, String voteMsg) {
        //vote participant info
        List<VoteInviteDO> voteInviteDOS = new ArrayList<>();
        for (String voter : getVoters(nodeID, voteConfig)) {
            VoteInviteDO voteInviteDO = VoteInviteDO.builder()
                    .upk(VoteInviteDO.UPK.builder().voteID(voteID).votePartitionID(voter).build())
                    .initiator(nodeID)
                    .desc(getInviteDesc(nodeID, voteConfig))
                    .type(getVoteType())
                    .action(VoteStatusEnum.REVIEWING.name())
                    .voteMsg(voteMsg)
                    .build();
            voteInviteDOS.add(voteInviteDO);
        }
        voteInviteRepository.saveAllAndFlush(voteInviteDOS);
    }

    protected Set<VoteRequestDO.PartyVoteInfo> getPartyVoteInfos(List<String> voters, String initiator) {
        Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = new HashSet<>();
        //vote invitor info
        for (String voter : voters) {
            partyVoteInfos.add(VoteRequestDO.PartyVoteInfo.builder().nodeId(voter).action(VoteStatusEnum.REVIEWING.name()).build());
        }
        //vote initiator info
        partyVoteInfos.add(VoteRequestDO.PartyVoteInfo.builder().nodeId(initiator).action(VoteStatusEnum.APPROVED.name()).build());
        return partyVoteInfos;
    }


    protected abstract String getApprovedAction(String nodeID, AbstractVoteConfig voteConfig);

    protected abstract String getRejectAction(String nodeID, AbstractVoteConfig voteConfig);

    protected abstract List<String> getExecutors(String nodeId, AbstractVoteConfig voteConfig);


    /**
     * @return the inviters,not contains initiator
     */
    protected abstract List<String> getVoters(String nodeID, AbstractVoteConfig voteConfig);

    /**
     * @return voteType
     */
    protected abstract String getVoteType();

    /**
     * @return initiator message name
     */
    protected abstract String getRequestDesc(String nodeID, AbstractVoteConfig voteConfig);

    /**
     * @return invitor message name
     */
    protected abstract String getInviteDesc(String nodeID, AbstractVoteConfig voteConfig);


    private void createVoteRequest(String voteID, String nodeID, AbstractVoteConfig voteConfig, String voteMsg) {
        List<String> voters = getVoters(nodeID, voteConfig);
        Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = getPartyVoteInfos(voters, nodeID);
        //vote initiator info
        VoteRequestDO voteRequestDO = VoteRequestDO.builder()
                .voteID(voteID)
                .initiator(nodeID)
                .type(getVoteType())
                .voters(voters)
                .voteCounter(nodeID)
                .requestMsg(voteMsg)
                .executeStatus(VoteExecuteEnum.COMMITTED.name())
                .executors(getExecutors(nodeID, voteConfig))
                .approvedThreshold(voters.size())
                .status(VoteStatusEnum.REVIEWING.getCode())
                .desc(getRequestDesc(nodeID, voteConfig))
                .partyVoteInfos(partyVoteInfos)
                .build();
        voteRequestRepository.saveAndFlush(voteRequestDO);
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

    protected VoteSyncRequest createVoteSyncRequest(VoteRequestDO voteRequestDO, List<VoteInviteDO> voteInviteDOS, ProjectApprovalConfigDO projectApprovalConfigDO, ProjectDO projectDO, List<ProjectNodeDO> projectNodeDOS, List<ProjectInstDO> projectInstDOS) {
        List<DbSyncRequest> dbSyncRequests = new ArrayList<>();
        dbSyncRequests.add(DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.VOTE_REQUEST.name()).projectNodesInfo(voteRequestDO).build());
        for (VoteInviteDO voteInviteDO : voteInviteDOS) {
            dbSyncRequests.add(DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.VOTE_INVITE.name()).projectNodesInfo(voteInviteDO).build());
        }
        dbSyncRequests.add(DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.PROJECT_APPROVAL_CONFIG.name()).projectNodesInfo(projectApprovalConfigDO).build());
        dbSyncRequests.add(DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.PROJECT.name()).projectNodesInfo(projectDO).build());
        if (!CollectionUtils.isEmpty(projectNodeDOS)) {
            for (ProjectNodeDO projectNodeDO : projectNodeDOS) {
                dbSyncRequests.add(DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.PROJECT_NODE.name()).projectNodesInfo(projectNodeDO).build());
            }
        }
        if (!CollectionUtils.isEmpty(projectInstDOS)) {
            for (ProjectInstDO projectInstDO : projectInstDOS) {
                dbSyncRequests.add(DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.PROJECT_INST.name()).projectNodesInfo(projectInstDO).build());
            }
        }
        return VoteSyncRequest.builder().dbSyncRequests(dbSyncRequests).build();
    }

    protected ProjectDO openProject(String projectId) {
        Optional<ProjectDO> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        return projectOpt.get();
    }
}
