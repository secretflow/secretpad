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

import org.secretflow.secretpad.common.constant.CacheConstants;
import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.errorcode.InstErrorCode;
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
import org.secretflow.secretpad.service.InstService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.impl.InstServiceImpl;
import org.secretflow.secretpad.service.model.approval.*;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cml
 * @date 2023/11/24
 */
@Component
public class ProjectCreateMessageHandler extends AbstractAutonomyVoteTypeHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(ProjectCreateMessageHandler.class);


    private final ProjectNodeRepository projectNodeRepository;
    private final ProjectInstRepository projectInstRepository;

    private final CacheManager cacheManager;

    private final InstService instService;


    public ProjectCreateMessageHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, InstRepository instRepository, EnvService envService, ProjectApprovalConfigRepository projectApprovalConfigRepository, ProjectRepository projectRepository, CertificateService certificateService, NodeManager nodeManager, ProjectNodeRepository projectNodeRepository, ProjectInstRepository projectInstRepository, CacheManager cacheManager, InstService instService) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, instRepository, envService, projectRepository, projectApprovalConfigRepository, nodeManager, certificateService);
        this.projectNodeRepository = projectNodeRepository;
        this.projectInstRepository = projectInstRepository;
        this.cacheManager = cacheManager;
        this.instService = instService;
    }


    @Override
    public List<VoteTypeEnum> supportTypes() {
        return Lists.newArrayList(VoteTypeEnum.PROJECT_CREATE);
    }

    @Override
    protected void preCheck(String initiatorId, AbstractVoteConfig voteConfig) {
        try {
            ProjectCreateApprovalConfig projectCreateApprovalConfig = (ProjectCreateApprovalConfig) voteConfig;

            List<ParticipantNodeInstVO> participantNodeInstVOS = projectCreateApprovalConfig.getParticipantNodeInstVOS();

            // Validate participants list is not empty
            List<String> participants = projectCreateApprovalConfig.getParticipants().stream()
                    .filter(participant -> !initiatorId.equals(participant))
                    .toList();
            if (participants.isEmpty()) {
                throw SecretpadException.of(VoteErrorCode.PARTICIPANT_NOT_EXIST);
            }

            // Collect initiator node IDs
            List<String> initiatorNodeIds = participantNodeInstVOS.stream()
                    .map(ParticipantNodeInstVO::getInitiatorNodeId)
                    .toList();

            // Validate that initiator node IDs do not contain duplicates
            if (hasDuplicates(initiatorNodeIds)) {
                throw SecretpadException.of(VoteErrorCode.INITIATOR_NODE_DUPLICATE);
            }

            // Validate that initiator node IDs match the inst of the initiator
            if (!instService.checkNodesInInst(initiatorId, initiatorNodeIds)) {
                throw SecretpadException.of(InstErrorCode.INITIATOR_INST_NODE_MISMATCH);
            }

            // Collect invitee IDs and fetch corresponding inst IDs
            List<String> inviteeIds = participantNodeInstVOS.stream()
                    .flatMap(participant -> participant.getInvitees().stream())
                    .map(ParticipantNodeInstVO.NodeInstVO::getInviteeId)
                    .toList();
            List<String> instIds = nodeRepository.findInstIdsByNodeIds(inviteeIds);

            // Determine if invited nodes match the participating inst
            boolean hasMismatch = instIds.stream().anyMatch(instId -> !participants.contains(instId));
            if (hasMismatch) {
                throw SecretpadException.of(InstErrorCode.INVITEE_INST_NODE_MISMATCH);
            }
        }catch (SecretpadException e){
            LOGGER.error("ProjectCreateMessageHandler preCheck error:{}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("ProjectCreateMessageHandler preCheck error:{}", e.getMessage(), e);
            throw SecretpadException.of(ProjectErrorCode.PROJECT_CREATE_FAILED, e);
        }
    }

    public static <T> boolean hasDuplicates(List<T> list) {
        Set<T> set = new HashSet<>(list);
        return set.size() < list.size();
    }

    @Override
    protected void createVoteConfig(String voteID, String initiatorId, AbstractVoteConfig voteConfig) {
        ProjectCreateApprovalConfig projectCreateApprovalConfig = (ProjectCreateApprovalConfig) voteConfig;
        String projectId = projectCreateApprovalConfig.getProjectId();
        List<String> allParties = projectCreateApprovalConfig.getParticipants();
        List<ParticipantNodeInstVO> participantNodeInstVOS = projectCreateApprovalConfig.getParticipantNodeInstVOS();
        cacheProjectParties(projectId, allParties);
        ProjectApprovalConfigDO projectApprovalConfigDO = ProjectApprovalConfigDO.builder()
                .initiator(initiatorId)
                .participantNodeInfo(participantNodeInstVOS)
                .projectId(projectId)
                .parties(allParties)
                .type(getVoteType())
                .voteID(voteID)
                .build();
        projectApprovalConfigRepository.saveAndFlush(projectApprovalConfigDO);
    }

    private void cacheProjectParties(String projectId, List<String> allParties) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache(CacheConstants.PROJECT_VOTE_PARTIES_CACHE));
        cache.putIfAbsent(projectId, Lists.newArrayList(allParties));
    }

    @Override
    protected String getApprovedAction(String initiatorId, AbstractVoteConfig voteConfig) {
        ProjectCallBackAction approvedCallBackAction = getApprovedCallBackAction(voteConfig);
        return VoteTypeEnum.PROJECT_CREATE.name() + "," + JsonUtils.toJSONString(approvedCallBackAction);
    }

    @Override
    protected String getRejectAction(String initiatorId, AbstractVoteConfig voteConfig) {
        ProjectCallBackAction projectRejectedCallbackAction = getApprovedCallBackAction(voteConfig);
        projectRejectedCallbackAction.getProjectDO().setStatus(ProjectStatusEnum.ARCHIVED.getCode());
        return VoteTypeEnum.PROJECT_ARCHIVE.name() + "," + JsonUtils.toJSONString(projectRejectedCallbackAction);
    }

    @Override
    protected String getVoteType() {
        return supportTypes().get(0).name();
    }

    private ProjectCallBackAction getApprovedCallBackAction(AbstractVoteConfig voteConfig) {
        ProjectCreateApprovalConfig projectCreateApprovalConfig = (ProjectCreateApprovalConfig) voteConfig;
        String projectId = projectCreateApprovalConfig.getProjectId();
        ProjectDO projectDO = openProject(projectId);
        projectDO.setGmtModified(LocalDateTime.now());
        projectRepository.save(projectDO);
        ProjectCallBackAction projectApprovalCallBackAction = new ProjectCallBackAction();
        ProjectDO approvedProjectDO = JsonUtils.deepCopy(projectDO, ProjectDO.class);
        approvedProjectDO.setStatus(ProjectStatusEnum.APPROVED.getCode());
        projectApprovalCallBackAction.setProjectDO(approvedProjectDO);
        List<ProjectInstDO> projectInstDOS = new ArrayList<>();
        for (String inviteParty : projectCreateApprovalConfig.getParticipants()) {
            ProjectInstDO projectInstDO = ProjectInstDO.builder()
                    .upk(new ProjectInstDO.UPK(projectId, inviteParty))
                    .build();
            projectInstDOS.add(projectInstDO);
        }
        List<String> nodeIds = new ArrayList<>();
        List<ProjectNodeDO> projectNodeDOS = new ArrayList<>();
        for (ParticipantNodeInstVO participantNodeInstVO : projectCreateApprovalConfig.getParticipantNodeInstVOS()) {
            nodeIds.add(participantNodeInstVO.getInitiatorNodeId());
            for (ParticipantNodeInstVO.NodeInstVO invitee : participantNodeInstVO.getInvitees()) {
                nodeIds.add(invitee.getInviteeId());
            }
        }
        for (String inviteParty : nodeIds) {
            ProjectNodeDO inviteProjectNodeDO = ProjectNodeDO.builder()
                    .upk(new ProjectNodeDO.UPK(projectId, inviteParty))
                    .build();
            projectNodeDOS.add(inviteProjectNodeDO);
        }
        projectApprovalCallBackAction.setProjectNodeDOS(projectNodeDOS);
        projectApprovalCallBackAction.setProjectInstDOS(projectInstDOS);
        return projectApprovalCallBackAction;
    }

    @Override
    @Transactional
    public void doCallBackApproved(VoteRequestDO voteRequestDO) {
        if (!envService.isCurrentInstEnvironment(voteRequestDO.getInitiator())) {
            LOGGER.info("you may not initiator return");
            return;
        }
        VoteRequestBody voteRequestBody = getVoteRequestBody(voteRequestDO);
        String approvedActionStr = voteRequestBody.getApprovedAction();
        String approvedAction = approvedActionStr.substring(VoteTypeEnum.PROJECT_CREATE.name().length() + 1);
        ProjectCallBackAction projectCallBackAction = JsonUtils.toJavaObject(approvedAction, ProjectCallBackAction.class);
        ProjectDO projectApprovalDO = projectCallBackAction.getProjectDO();
        //create project_node
        List<ProjectNodeDO> projectNodeDOS = projectCallBackAction.getProjectNodeDOS();
        //create project_inst
        List<ProjectInstDO> projectInstDOS = projectCallBackAction.getProjectInstDOS();
        Map<String, String> projectInstMap = projectInstDOS.stream().collect(Collectors.toMap(ProjectInstDO::getNodeId, ProjectInstDO::getProjectId));
        String instId = InstServiceImpl.INST_ID;
        if (projectInstMap.containsKey(instId)) {
            //recheck project status,maybe while do this vote,another user may try to archive this project and it may be archive success
            Optional<ProjectDO> projectDOOptional = projectRepository.findById(projectApprovalDO.getProjectId());
            if (!projectDOOptional.isPresent()) {
                String err = String.format("doCallBack create project Approved error,project [%s] does not exist ", projectApprovalDO.getName());
                LOGGER.error(err);
                voteRequestDO.setMsg(err);
                failed(voteRequestDO);
                return;
            }
            ProjectDO currentDBprojectDO = projectDOOptional.get();
            if (!ProjectStatusEnum.REVIEWING.getCode().equals(currentDBprojectDO.getStatus())) {
                String errorMsg = String.format("project [%s] status Is not reviewing,status Is [%s]", projectApprovalDO.getName(), ProjectStatusEnum.parse(currentDBprojectDO.getStatus()));
                voteRequestDO.setMsg(errorMsg);
                failed(voteRequestDO);
                return;
            }
            projectInstRepository.saveAllAndFlush(projectInstDOS);
            projectNodeRepository.saveAllAndFlush(projectNodeDOS);
            projectRepository.save(projectApprovalDO);
            success(voteRequestDO);
        } else {
            String err = String.format("doCallBackApproved error,voters does not has party : %s", instId);
            LOGGER.error("doCallBackApproved error,voters does not has party : {}", instId);
            voteRequestDO.setMsg(err);
            failed(voteRequestDO);
        }
    }

    private VoteRequestBody getVoteRequestBody(VoteRequestDO voteRequestDO) {
        String requestMsg = voteRequestDO.getRequestMsg();
        VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
        String voteRequestMessageBodyBase64 = voteRequestMessage.getBody();
        String voteRequestBodyStr = new String(Base64Utils.decode(voteRequestMessageBodyBase64));
        return JsonUtils.toJavaObject(voteRequestBodyStr, VoteRequestBody.class);
    }


    @Override
    @Transactional
    public void doCallBackRejected(VoteRequestDO voteRequestDO) {
        if (!envService.isCurrentInstEnvironment(voteRequestDO.getInitiator())) {
            LOGGER.info("not initiator return");
            return;
        }
        VoteRequestBody voteRequestBody = getVoteRequestBody(voteRequestDO);
        String rejectedActionStr = voteRequestBody.getRejectedAction();
        String rejectedAction = rejectedActionStr.substring(VoteTypeEnum.PROJECT_ARCHIVE.name().length() + 1);
        LOGGER.info("rejectedAction = {}", rejectedAction);
        ProjectCallBackAction projectCallBackAction = JsonUtils.toJavaObject(rejectedAction, ProjectCallBackAction.class);
        ProjectDO projectDO = projectCallBackAction.getProjectDO();
        List<ProjectInstDO> projectInstDOS = projectCallBackAction.getProjectInstDOS();
        Map<String, String> projectInstMap = projectInstDOS.stream().collect(Collectors.toMap(ProjectInstDO::getNodeId, ProjectInstDO::getProjectId));
        List<ProjectNodeDO> projectNodeDOS = projectCallBackAction.getProjectNodeDOS();
        String inst_id = InstServiceImpl.INST_ID;
        if (projectInstMap.containsKey(inst_id)) {
            projectRepository.save(projectDO);
            projectDO.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
            projectInstRepository.deleteAll(projectInstDOS);
            projectNodeRepository.deleteAll(projectNodeDOS);
            success(voteRequestDO);
            LOGGER.info("doCallBackRejected success, project {} ARCHIVED", projectDO.getName());
        } else {
            String err = String.format("doCallBackRejected error,voters does not has party : %s", inst_id);
            LOGGER.error("doCallBackRejected error,voters does not has party : {}", inst_id);
            voteRequestDO.setMsg(err);
            failed(voteRequestDO);
        }
    }
}
