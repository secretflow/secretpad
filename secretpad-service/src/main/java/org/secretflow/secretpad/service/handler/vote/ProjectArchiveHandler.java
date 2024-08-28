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

import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.impl.InstServiceImpl;
import org.secretflow.secretpad.service.model.approval.*;
import org.secretflow.secretpad.service.model.message.ProjectArchivePartyVoteStatus;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author cml
 * @date 2023/11/24
 */
@Component
public class ProjectArchiveHandler extends AbstractAutonomyVoteTypeHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectArchiveHandler.class);


    private final ProjectNodeRepository projectNodeRepository;
    private final ProjectInstRepository projectInstRepository;


    public ProjectArchiveHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, InstRepository instRepository, EnvService envService, ProjectRepository projectRepository, ProjectApprovalConfigRepository projectApprovalConfigRepository, ProjectNodeRepository projectNodeRepository, CertificateService certificateService, NodeManager nodeManager, ProjectInstRepository projectInstRepository) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, instRepository, envService, projectRepository, projectApprovalConfigRepository, nodeManager, certificateService);
        this.projectNodeRepository = projectNodeRepository;
        this.projectInstRepository = projectInstRepository;
    }


    @Override
    public List<VoteTypeEnum> supportTypes() {
        return Lists.newArrayList(VoteTypeEnum.PROJECT_ARCHIVE);
    }

    @Override
    protected void preCheck(String nodeID, AbstractVoteConfig voteConfig) {
        ProjectArchiveConfig projectArchiveConfig = (ProjectArchiveConfig) voteConfig;
        String projectId = projectArchiveConfig.getProjectId();
        ProjectDO projectDO = openProject(projectId);
        if (!ProjectStatusEnum.APPROVED.getCode().equals(projectDO.getStatus())) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_CAN_NOT_CREATE_ARCHIVE_VOTE);
        }
        //check this project archive record
        List<ProjectApprovalConfigDO> projectApprovalConfigDOS = projectApprovalConfigRepository.findArchiveRecordByProjectId(projectId, VoteTypeEnum.PROJECT_ARCHIVE.name());
        if (!CollectionUtils.isEmpty(projectApprovalConfigDOS)) {
            projectApprovalConfigDOS.stream().forEach(e -> {
                Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(e.getVoteID());
                Integer status = voteRequestDOOptional.get().getStatus();
                if (VoteStatusEnum.REVIEWING.getCode().equals(status)) {
                    throw SecretpadException.of(VoteErrorCode.PROJECT_ARCHIVE_VOTE_ALREADY_EXIST, projectDO.getName());
                }
            });
        }
        //determine the voters of the archive,by the project create approval record
        Optional<ProjectApprovalConfigDO> projectCreateApprovalConfigDOOptional = projectApprovalConfigRepository.findByProjectIdAndType(projectId, VoteTypeEnum.PROJECT_CREATE.name());
        if (!projectCreateApprovalConfigDOOptional.isPresent()) {
            throw SecretpadException.of(VoteErrorCode.PROJECT_VOTE_NOT_EXISTS, projectDO.getName());
        }
    }

    @Override
    protected void createVoteConfig(String voteID, String initiatorId, AbstractVoteConfig voteConfig) {
        ProjectArchiveConfig projectArchiveConfig = (ProjectArchiveConfig) voteConfig;
        String projectId = projectArchiveConfig.getProjectId();
        Optional<ProjectApprovalConfigDO> approvalConfigDO = projectApprovalConfigRepository.findByProjectId(projectId);
        List<ParticipantNodeInstVO> participantNodeInstVOS = approvalConfigDO.get().getParticipantNodeInfo();
        Map<String, NodeDO> nodeInfoMap = nodeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(NodeDO::getNodeId, node -> node));
        for (ParticipantNodeInstVO participantNodeInstVO : participantNodeInstVOS) {
            String initiatorNodeId = participantNodeInstVO.getInitiatorNodeId();
            NodeDO initiatorNode = nodeInfoMap.get(initiatorNodeId);
            if (initiatorNode.getNodeId().equals(initiatorId)) {
                break;
            }
            for (ParticipantNodeInstVO.NodeInstVO inviteeVO : participantNodeInstVO.getInvitees()) {
                String inviteeInstId = nodeInfoMap.get(inviteeVO.getInviteeId()).getInstId();
                if (inviteeInstId.equals(initiatorId)) {
                    String inviteeId = inviteeVO.getInviteeId();
                    inviteeVO.setInviteeId(initiatorNodeId);
                    participantNodeInstVO.setInitiatorNodeId(inviteeId);
                }
            }
        }
        ProjectApprovalConfigDO projectApprovalConfigDO = ProjectApprovalConfigDO.builder()
                .projectId(projectId)
                .participantNodeInfo(participantNodeInstVOS)
                .type(getVoteType())
                .voteID(voteID)
                .initiator(initiatorId)
                .build();
        projectApprovalConfigRepository.saveAndFlush(projectApprovalConfigDO);
    }


    @Override
    protected List<String> getVoters(String initiatorId, AbstractVoteConfig voteConfig) {
        ProjectArchiveConfig projectArchiveConfig = (ProjectArchiveConfig) voteConfig;
        String projectId = projectArchiveConfig.getProjectId();
        Optional<ProjectApprovalConfigDO> projectCreateApprovalConfigDOOptional = projectApprovalConfigRepository.findByProjectIdAndType(projectId, VoteTypeEnum.PROJECT_CREATE.name());
        List<String> parties = projectCreateApprovalConfigDOOptional.get().getParties();
        List<String> voters = JsonUtils.deepCopyList(parties, String.class);
        voters.remove(initiatorId);
        return voters;
    }

    @Override
    protected String getApprovedAction(String initiatorId, AbstractVoteConfig voteConfig) {
        ProjectArchiveConfig projectArchiveConfig = (ProjectArchiveConfig) voteConfig;
        String projectId = projectArchiveConfig.getProjectId();
        ProjectDO projectDO = openProject(projectId);
        ProjectDO approvedProjectDO = JsonUtils.deepCopy(projectDO, ProjectDO.class);
        approvedProjectDO.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
        ProjectCallBackAction projectArchiveCallBackAction = new ProjectCallBackAction();
        projectArchiveCallBackAction.setProjectDO(approvedProjectDO);
        //here may be empty,as the project may not agree to create success
        List<ProjectNodeDO> projectNodeDOS = projectNodeRepository.findByProjectId(projectId);
        List<ProjectInstDO> projectInstDOS = projectInstRepository.findByProjectId(projectId);
        projectArchiveCallBackAction.setProjectInstDOS(projectInstDOS);
        projectArchiveCallBackAction.setProjectNodeDOS(projectNodeDOS);
        return VoteTypeEnum.PROJECT_CREATE.name() + "," + JsonUtils.toJSONString(projectArchiveCallBackAction);
    }

    @Override
    protected String getRejectAction(String initiatorId, AbstractVoteConfig voteConfig) {
        return "NONE";
    }

    @Override
    protected String getVoteType() {
        return supportTypes().get(0).name();
    }

    @Override
    public void doCallBackApproved(VoteRequestDO voteRequestDO) {
        if (!envService.isCurrentInstEnvironment(voteRequestDO.getInitiator())) {
            LOGGER.info("not initiator return");
            return;
        }
        VoteRequestBody voteRequestBody = getVoteRequestBody(voteRequestDO);
        String approvedActionStr = voteRequestBody.getApprovedAction();
        String approvedAction = approvedActionStr.substring(VoteTypeEnum.PROJECT_CREATE.name().length() + 1);
        ProjectCallBackAction projectCallBackAction = JsonUtils.toJavaObject(approvedAction, ProjectCallBackAction.class);
        ProjectDO projectArchiveDO = projectCallBackAction.getProjectDO();
        List<ProjectNodeDO> projectNodeDOS = projectCallBackAction.getProjectNodeDOS();
        List<ProjectInstDO> projectInstDOS = projectCallBackAction.getProjectInstDOS();
        Map<String, String> projectInstMap = projectInstDOS.stream().collect(Collectors.toMap(ProjectInstDO::getNodeId, ProjectInstDO::getProjectId));
        Optional<ProjectDO> currentDBProjectDO = projectRepository.findById(projectArchiveDO.getProjectId());
        if (!currentDBProjectDO.isPresent()) {
            String err = String.format("doCallBack archive project Approved error,project [%s] does not exist ", projectArchiveDO.getName());
            LOGGER.error(err);
            voteRequestDO.setMsg(err);
            failed(voteRequestDO);
            return;
        }
        String inst_id = InstServiceImpl.INST_ID;
        if (projectInstMap.containsKey(inst_id)) {
            ProjectDO dbProjectDO = currentDBProjectDO.get();
            if (ProjectStatusEnum.APPROVED.getCode().equals(dbProjectDO.getStatus())) {
                projectInstRepository.deleteAll(projectInstDOS);
                projectNodeRepository.deleteAll(projectNodeDOS);
                LOGGER.info("archive project,delete project inst success");
            }
            projectRepository.save(projectArchiveDO);
            LOGGER.info("archive project,update project status success");
            success(voteRequestDO);
        } else {
            String err = String.format("doCallBackApproved error,voters does not has party : %s", inst_id);
            LOGGER.error("doCallBackApproved error,voters does not has party : {}", inst_id);
            voteRequestDO.setMsg(err);
            failed(voteRequestDO);
        }
    }

    @Override
    public void flushVoteStatus(String voteID) {
        Optional<ProjectApprovalConfigDO> projectApprovalConfigDO = projectApprovalConfigRepository.findById(voteID);
        String projectId = projectApprovalConfigDO.get().getProjectId();
        Optional<ProjectDO> projectDOOptional = projectRepository.findById(projectId);
        ProjectDO projectDO = projectDOOptional.get();
        projectDO.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
        projectRepository.save(projectDO);
    }

    @Override
    public List<ProjectArchivePartyVoteStatus> getPartyStatusByVoteID(String voteID) {
        //find project archive record
        Optional<ProjectApprovalConfigDO> projectProjectArchiveConfigDOOptional = projectApprovalConfigRepository.findById(voteID);
        Assert.isTrue(projectProjectArchiveConfigDOOptional.isPresent(), voteID + "not exists in ProjectApprovalConfig");
        ProjectApprovalConfigDO projectApprovalConfigDO = projectProjectArchiveConfigDOOptional.get();

        //find project create record by archive record project id
        Optional<ProjectApprovalConfigDO> optionalProjectCreateApprovalConfigDO = projectApprovalConfigRepository.findByProjectIdAndType(projectApprovalConfigDO.getProjectId(), VoteTypeEnum.PROJECT_CREATE.name());
        ProjectApprovalConfigDO projectCreateConfigDO = optionalProjectCreateApprovalConfigDO.get();
        Optional<VoteRequestDO> requestDOOptional = voteRequestRepository.findById(projectCreateConfigDO.getVoteID());

        //find project create requestDO by project create config's voteId
        VoteRequestDO projectCreateVoteRequestDO = requestDOOptional.get();
        Set<VoteRequestDO.PartyVoteInfo> projectCreateVoteRequestDOPartyVoteInfos = projectCreateVoteRequestDO.getPartyVoteInfos();
        //find project create party vote info,it decides we can step into project or not
        Map<String, VoteRequestDO.PartyVoteInfo> projectCreateStagePartyInfoMap = projectCreateVoteRequestDOPartyVoteInfos.stream().collect(Collectors.toMap(e -> e.getPartyId(), Function.identity()));
        //now we start find project archive party info
        Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(voteID);
        VoteRequestDO projectArchiveVoteRequestDO = voteRequestDOOptional.get();
        Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = projectArchiveVoteRequestDO.getPartyVoteInfos();
        List<ProjectArchivePartyVoteStatus> partyVoteStatusList = new ArrayList<>();
        List<InstDO> instDOS = instRepository.findByInstIdIn(partyVoteInfos.stream().map(e -> e.getPartyId()).collect(Collectors.toList()));
        Map<String, String> instMap = instDOS.stream().collect(Collectors.toMap(e -> e.getInstId(), e -> e.getName()));
        for (VoteRequestDO.PartyVoteInfo partyVoteInfo : partyVoteInfos) {
            ProjectArchivePartyVoteStatus partyVoteStatus = new ProjectArchivePartyVoteStatus();
            partyVoteStatus.setReason(partyVoteInfo.getReason());
            partyVoteStatus.setAction(partyVoteInfo.getAction());
            partyVoteStatus.setParticipantID(partyVoteInfo.getPartyId());
            partyVoteStatus.setParticipantName(instMap.get(partyVoteInfo.getPartyId()));
            //this is original project create stage's voter's vote action info
            partyVoteStatus.setProjectCreateVoteAction(projectCreateStagePartyInfoMap.get(partyVoteInfo.getPartyId()).getAction());
            partyVoteStatusList.add(partyVoteStatus);
        }
        return partyVoteStatusList;
    }

    private VoteRequestBody getVoteRequestBody(VoteRequestDO voteRequestDO) {
        String requestMsg = voteRequestDO.getRequestMsg();
        VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
        String voteRequestMessageBodyBase64 = voteRequestMessage.getBody();
        String voteRequestBodyStr = new String(Base64Utils.decode(voteRequestMessageBodyBase64));
        return JsonUtils.toJavaObject(voteRequestBodyStr, VoteRequestBody.class);
    }


}
