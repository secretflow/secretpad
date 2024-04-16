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

import org.secretflow.secretpad.common.constant.CacheConstants;
import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.persistence.entity.ProjectApprovalConfigDO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
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

    private final CacheManager cacheManager;


    public ProjectCreateMessageHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, EnvService envService, ProjectApprovalConfigRepository projectApprovalConfigRepository, ProjectRepository projectRepository, CertificateService certificateService, NodeManager nodeManager, ProjectNodeRepository projectNodeRepository, CacheManager cacheManager) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, envService, projectRepository, projectApprovalConfigRepository, nodeManager, certificateService);
        this.projectNodeRepository = projectNodeRepository;
        this.cacheManager = cacheManager;
    }


    @Override
    public List<VoteTypeEnum> supportTypes() {
        return Lists.newArrayList(VoteTypeEnum.PROJECT_CREATE);
    }

    @Override
    protected void preCheck(String nodeID, AbstractVoteConfig voteConfig) {

    }

    @Override
    protected void createVoteConfig(String voteID, String nodeID, AbstractVoteConfig voteConfig) {
        ProjectCreateApprovalConfig projectCreateApprovalConfig = (ProjectCreateApprovalConfig) voteConfig;
        String projectId = projectCreateApprovalConfig.getProjectId();
        List<String> allParties = projectCreateApprovalConfig.getNodes();
        cacheProjectParties(projectId, allParties);
        ProjectApprovalConfigDO projectApprovalConfigDO = ProjectApprovalConfigDO.builder()
                .initiator(nodeID)
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
    protected String getApprovedAction(String nodeID, AbstractVoteConfig voteConfig) {
        ProjectCallBackAction approvedCallBackAction = getApprovedCallBackAction(voteConfig);
        return VoteTypeEnum.PROJECT_CREATE.name() + "," + JsonUtils.toJSONString(approvedCallBackAction);
    }

    @Override
    protected String getRejectAction(String nodeID, AbstractVoteConfig voteConfig) {
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
        List<ProjectNodeDO> projectNodeDOS = new ArrayList<>();
        for (String inviteParty : projectCreateApprovalConfig.getNodes()) {
            ProjectNodeDO inviteProjectNodeDO = ProjectNodeDO.builder()
                    .upk(new ProjectNodeDO.UPK(projectId, inviteParty))
                    .build();
            projectNodeDOS.add(inviteProjectNodeDO);
        }
        projectApprovalCallBackAction.setProjectNodeDOS(projectNodeDOS);
        return projectApprovalCallBackAction;
    }

    @Override
    @Transactional
    public void doCallBackApproved(VoteRequestDO voteRequestDO) {
        if (!envService.isCurrentNodeEnvironment(voteRequestDO.getInitiator())) {
            LOGGER.info("you may not initiator return");
            return;
        }
        VoteRequestBody voteRequestBody = getVoteRequestBody(voteRequestDO);
        String approvedActionStr = voteRequestBody.getApprovedAction();
        String approvedAction = approvedActionStr.substring(VoteTypeEnum.PROJECT_CREATE.name().length() + 1);
        ProjectCallBackAction projectCallBackAction = JsonUtils.toJavaObject(approvedAction, ProjectCallBackAction.class);
        ProjectDO projectApprovalDO = projectCallBackAction.getProjectDO();
        //创建project_node
        List<ProjectNodeDO> projectNodeDOS = projectCallBackAction.getProjectNodeDOS();
        Map<String, String> projectNodeMap = projectNodeDOS.stream().collect(Collectors.toMap(ProjectNodeDO::getNodeId, ProjectNodeDO::getProjectId));
        String platformNodeId = envService.getPlatformNodeId();
        if (projectNodeMap.containsKey(platformNodeId)) {
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
                String errorMsg = String.format("project [%s] status is not reviewing,status is [%s]", projectApprovalDO.getName(), ProjectStatusEnum.parse(currentDBprojectDO.getStatus()));
                voteRequestDO.setMsg(errorMsg);
                failed(voteRequestDO);
                return;
            }
            projectNodeRepository.saveAllAndFlush(projectNodeDOS);
            projectRepository.save(projectApprovalDO);
            success(voteRequestDO);
        } else {
            String err = String.format("doCallBackApproved error,voters does not has party : %s", platformNodeId);
            LOGGER.error("doCallBackApproved error,voters does not has party : {}", platformNodeId);
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
        if (!envService.isCurrentNodeEnvironment(voteRequestDO.getInitiator())) {
            LOGGER.info("not initiator return");
            return;
        }
        VoteRequestBody voteRequestBody = getVoteRequestBody(voteRequestDO);
        String rejectedActionStr = voteRequestBody.getRejectedAction();
        String rejectedAction = rejectedActionStr.substring(VoteTypeEnum.PROJECT_ARCHIVE.name().length() + 1);
        LOGGER.info("rejectedAction = {}", rejectedAction);
        ProjectCallBackAction projectCallBackAction = JsonUtils.toJavaObject(rejectedAction, ProjectCallBackAction.class);
        ProjectDO projectDO = projectCallBackAction.getProjectDO();
        List<ProjectNodeDO> projectNodeDOS = projectCallBackAction.getProjectNodeDOS();
        Map<String, String> projectNodeMap = projectNodeDOS.stream().collect(Collectors.toMap(ProjectNodeDO::getNodeId, ProjectNodeDO::getProjectId));
        String platformNodeId = envService.getPlatformNodeId();
        if (projectNodeMap.containsKey(platformNodeId)) {
            projectRepository.save(projectDO);
            projectDO.setStatus(ProjectStatusEnum.ARCHIVED.getCode());
            projectNodeRepository.deleteAll(projectNodeDOS);
            success(voteRequestDO);
            LOGGER.info("doCallBackRejected success, project {} ARCHIVED", projectDO.getName());
        } else {
            String err = String.format("doCallBackRejected error,voters does not has party : %s", platformNodeId);
            LOGGER.error("doCallBackRejected error,voters does not has party : {}", platformNodeId);
            voteRequestDO.setMsg(err);
            failed(voteRequestDO);
        }
    }


}
