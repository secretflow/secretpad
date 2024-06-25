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

import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.EncryptUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectApprovalConfigDO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.model.approval.*;
import org.secretflow.secretpad.service.model.message.*;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenmingliang
 * @date 2023/12/21
 */
public abstract class AbstractAutonomyVoteTypeHandler extends AbstractVoteTypeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAutonomyVoteTypeHandler.class);
    protected final ProjectApprovalConfigRepository projectApprovalConfigRepository;


    protected final NodeManager nodeManager;

    protected AbstractAutonomyVoteTypeHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, EnvService envService, ProjectRepository projectRepository, ProjectApprovalConfigRepository projectApprovalConfigRepository, NodeManager nodeManager, CertificateService certificateService) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, envService, certificateService, projectRepository);
        this.projectApprovalConfigRepository = projectApprovalConfigRepository;
        this.nodeManager = nodeManager;
    }

    @Override
    public MessageDetailVO getVoteMessageDetail(Boolean isInitiator, String nodeID, String voteID) {
        Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(voteID);
        if (voteRequestDOOptional.isEmpty()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        VoteRequestDO voteRequestDO = voteRequestDOOptional.get();
        String requestMsg = voteRequestDO.getRequestMsg();
        VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
        String voteRequestMessageBodyBase64 = voteRequestMessage.getBody();
        VoteRequestBody voteRequestBody = JsonUtils.toJavaObject(new String(Base64Utils.decode(voteRequestMessageBodyBase64)), VoteRequestBody.class);
        verify(voteRequestMessage, voteRequestBody);
        String voteRequestID = voteRequestBody.getVoteRequestID();
        if (!StringUtils.equals(voteRequestID, voteRequestID)) {
            throw SecretpadException.of(VoteErrorCode.VOTE_CHECK_FAILED);
        }
        Optional<ProjectApprovalConfigDO> projectApprovalConfigDOOptional = projectApprovalConfigRepository.findById(voteID);
        if (projectApprovalConfigDOOptional.isEmpty()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        ProjectApprovalConfigDO projectApprovalConfigDO = projectApprovalConfigDOOptional.get();
        String projectId = projectApprovalConfigDO.getProjectId();
        Optional<ProjectDO> projectDOOptional = projectRepository.findById(projectId);
        if (projectDOOptional.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        ProjectDO projectDO = projectDOOptional.get();
        String projectName = projectDO.getName();

        ProjectCreateMessageDetail projectCreateMessageDetail = ProjectCreateMessageDetail.builder()
                .projectName(projectName)
                .initiatorNodeID(voteRequestBody.getInitiator())
                .initiatorNodeName(nodeRepository.findByNodeId(voteRequestBody.getInitiator()).getName())
                .partyVoteStatuses(getPartyStatusByVoteID(voteID))
                .computeFunc(projectDO.getComputeFunc())
                .computeMode(projectDO.getComputeMode())
                .projectDesc(projectDO.getDescription())
                .gmtCreated(DateTimes.toRfc3339(projectDO.getGmtCreate()))
                .build();
        String messageName = projectName;
        String type = projectApprovalConfigDO.getType();
        String status = VoteStatusEnum.parse(voteRequestDO.getStatus());
        projectCreateMessageDetail.setMessageName(messageName);
        projectCreateMessageDetail.setType(type);
        projectCreateMessageDetail.setStatus(status);
        return projectCreateMessageDetail;
    }

    @Override
    protected List<String> getVoters(String nodeID, AbstractVoteConfig voteConfig) {
        ProjectCreateApprovalConfig projectCreateApprovalConfig = (ProjectCreateApprovalConfig) voteConfig;
        //all parties contain initiator and invitor
        List<String> allParties = projectCreateApprovalConfig.getNodes();
        List<String> voters = JsonUtils.deepCopyList(allParties, String.class);
        //invitor
        voters.remove(nodeID);
        return voters;
    }

    @Override
    public AbstractVoteTypeMessage getMessageListNecessaryInfo(String voteID) {
        Optional<ProjectApprovalConfigDO> projectApprovalConfigDOOptional = projectApprovalConfigRepository.findById(voteID);
        if (projectApprovalConfigDOOptional.isEmpty()) {
            throw SecretpadException.of(VoteErrorCode.PROJECT_VOTE_NOT_EXISTS);
        }
        ProjectApprovalConfigDO projectApprovalConfigDO = projectApprovalConfigDOOptional.get();
        Optional<ProjectDO> projectDOOptional = projectRepository.findById(projectApprovalConfigDO.getProjectId());
        ProjectDO projectDO = projectDOOptional.get();
        return ProjectApprovalCustomizedMessage.builder()
                .computeFunc(projectDO.getComputeFunc())
                .projectId(projectDO.getProjectId())
                .computeMode(projectDO.getComputeMode())
                .build();
    }

    @Override
    public List<? extends PartyVoteStatus> getPartyStatusByVoteID(String voteID) {
        Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(voteID);
        VoteRequestDO voteRequestDO = voteRequestDOOptional.get();
        Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = voteRequestDO.getPartyVoteInfos();
        List<PartyVoteStatus> partyVoteStatusList = new ArrayList<>();
        List<NodeDO> nodeDOS = nodeRepository.findByNodeIdIn(partyVoteInfos.stream().map(VoteRequestDO.PartyVoteInfo::getNodeId).collect(Collectors.toList()));
        Map<String, String> nodeMap = nodeDOS.stream().collect(Collectors.toMap(NodeDO::getNodeId, NodeDO::getName));

        for (VoteRequestDO.PartyVoteInfo partyVoteInfo : partyVoteInfos) {
            PartyVoteStatus partyVoteStatus = PartyVoteStatus.builder()
                    .reason(partyVoteInfo.getReason())
                    .action(partyVoteInfo.getAction())
                    .nodeID(partyVoteInfo.getNodeId())
                    .nodeName(nodeMap.get(partyVoteInfo.getNodeId()))
                    .build();
            partyVoteStatusList.add(partyVoteStatus);
        }
        return partyVoteStatusList;
    }

    @Override
    protected List<String> getExecutors(String nodeID, AbstractVoteConfig voteConfig) {
        List<String> executors = Lists.newArrayList(nodeID);
        executors.add(nodeID);
        return executors;
    }

    @Override
    protected String getInviteDesc(String nodeID, AbstractVoteConfig voteConfig) {
        String projectId = "";
        if (voteConfig instanceof ProjectCreateApprovalConfig projectCreateApprovalConfig) {
            projectId = projectCreateApprovalConfig.getProjectId();
        }
        if (voteConfig instanceof ProjectArchiveConfig projectArchiveConfig) {
            projectId = projectArchiveConfig.getProjectId();

        }
        ProjectDO projectDO = openProject(projectId);
        return projectDO.getName();
    }

    @Override
    protected String getRequestDesc(String nodeID, AbstractVoteConfig voteConfig) {
        return getInviteDesc(nodeID, voteConfig);
    }

    private void verifyCert(String initiator, List<String> certChain) {
        String cert = nodeManager.getCert(initiator);
        String rootCert = certChain.get(1);
        if (!rootCert.equals(cert)) {
            LOGGER.info("cert does not match,verify fail");
            throw SecretpadException.of(SystemErrorCode.VERIFY_SIGNATURE_ERROR);
        }
        boolean success = EncryptUtils.validateCertChain(certChain);
        LOGGER.info("validateCertChain success");
        if (!success) {
            throw SecretpadException.of(SystemErrorCode.VERIFY_SIGNATURE_ERROR);
        }
    }

    public boolean verifySignature(VoteRequestMessage voteRequestMessage) throws Exception {
        String voteRequestMessageBodyBase64 = voteRequestMessage.getBody();
        List<String> certChain = voteRequestMessage.getCertChain();
        String voteRequestSignature = voteRequestMessage.getVoteRequestSignature();
        return EncryptUtils.verifySHA256withRSA(voteRequestMessageBodyBase64.getBytes(), certChain.get(0), voteRequestSignature);
    }

    private void verify(VoteRequestMessage voteRequestMessage, VoteRequestBody voteRequestBody) {
        String initiator = voteRequestBody.getInitiator();
        List<String> certChain = voteRequestMessage.getCertChain();
        try {
            verifySignature(voteRequestMessage);
            verifyCert(initiator, certChain);
            LOGGER.info("verifySignature success");

        } catch (Exception e) {
            LOGGER.error("verify error", e);
            throw SecretpadException.of(SystemErrorCode.VERIFY_SIGNATURE_ERROR, e);
        }
    }
}
