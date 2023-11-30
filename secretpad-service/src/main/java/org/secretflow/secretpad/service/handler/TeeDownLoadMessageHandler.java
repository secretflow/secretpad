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

import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.enums.VoteExecuteEnum;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.*;
import org.secretflow.secretpad.service.model.datatable.TableColumnVO;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.message.*;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TeeDownLoadMessageHandler.
 *
 * @author cml
 * @date 2023/09/28
 */
@Component
public class TeeDownLoadMessageHandler extends AbstractVoteTypeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeeDownLoadMessageHandler.class);

    private final TeeDownLoadAuditConfigRepository teeDownLoadAuditConfigRepository;

    private final ProjectDatatableRepository projectDatatableRepository;

    private final ProjectRepository projectRepository;

    private final ProjectNodeRepository projectNodeRepository;

    private final ProjectJobTaskRepository jobTaskRepository;
    private final ProjectService projectService;

    private final DatatableService datatableService;

    private final EnvService envService;

    public TeeDownLoadMessageHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, EnvService envService, TeeDownLoadAuditConfigRepository teeDownLoadAuditConfigRepository, ProjectDatatableRepository projectDatatableRepository, ProjectRepository projectRepository, ProjectNodeRepository projectNodeRepository, ProjectJobTaskRepository jobTaskRepository, ProjectService projectService, DatatableService datatableService, EnvService envService1) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, envService);
        this.teeDownLoadAuditConfigRepository = teeDownLoadAuditConfigRepository;
        this.projectDatatableRepository = projectDatatableRepository;
        this.projectRepository = projectRepository;
        this.projectNodeRepository = projectNodeRepository;
        this.jobTaskRepository = jobTaskRepository;
        this.projectService = projectService;
        this.datatableService = datatableService;
        this.envService = envService1;
    }

    @Override
    public MessageDetailVO getVoteMessageDetail(Boolean isInitiator, String nodeID, String voteID) {

        Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findByVoteID(voteID);
        if (!voteRequestDOOptional.isPresent()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        Optional<TeeDownLoadAuditConfigDO> teeDownLoadAuditConfigDOOptional = teeDownLoadAuditConfigRepository.findById(voteID);
        if (!teeDownLoadAuditConfigDOOptional.isPresent()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        TeeDownLoadAuditConfigDO teeDownLoadAuditConfigDO = teeDownLoadAuditConfigDOOptional.get();
        VoteRequestDO voteRequestDO = voteRequestDOOptional.get();
        String fedTableID = voteRequestDO.getDesc();
        LOGGER.info("resourceType = {}", teeDownLoadAuditConfigDO.getResourceType());
        List<TableColumnVO> columnVOList = null;
        if (ResultKind.FedTable.getName().equals(teeDownLoadAuditConfigDO.getResourceType())) {
            //nodeID is tee，get from ProjectTaskDO
            LOGGER.info("projectID = {}, jobID = {}, taskID = {}", teeDownLoadAuditConfigDO.getProjectId(), teeDownLoadAuditConfigDO.getJobId(), teeDownLoadAuditConfigDO.getTaskID());
            Optional<ProjectTaskDO> projectTaskDOOptional = jobTaskRepository.findById(new ProjectTaskDO.UPK(teeDownLoadAuditConfigDO.getProjectId(), teeDownLoadAuditConfigDO.getJobId(), teeDownLoadAuditConfigDO.getTaskID()));
            if (!projectTaskDOOptional.isPresent()) {
                throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
            }
            ProjectTaskDO projectTaskDO = projectTaskDOOptional.get();
            List<String> parties = projectTaskDO.getParties();
            nodeID = parties.get(0);
            Assert.isTrue(!CollectionUtils.isEmpty(parties), "projectTaskDO parties is empty");
            Assert.isTrue(parties.size() == 1, "tee task parties must single party");
            LOGGER.info("projectID = {}, nodeID = {}, datatableID = {}", teeDownLoadAuditConfigDO.getProjectId(), nodeID, fedTableID);
            Optional<ProjectDatatableDO> projectDatatableDOOptional = projectDatatableRepository.findById(new ProjectDatatableDO.UPK(teeDownLoadAuditConfigDO.getProjectId(), nodeID, fedTableID));
            Assert.isTrue(projectDatatableDOOptional.isPresent(), "fedTable not exists");
            ProjectDatatableDO projectDatatableDO = projectDatatableDOOptional.get();
            columnVOList = projectDatatableDO.getTableConfig().stream().map(e -> TableColumnVO.from(e)).collect(Collectors.toList());
        }


        Optional<ProjectDO> projectDOOptional = projectRepository.findById(teeDownLoadAuditConfigDO.getProjectId());
        if (!projectDOOptional.isPresent()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        ProjectDO projectDO = projectDOOptional.get();

        ProjectSimpleVO projectSimpleVO = ProjectSimpleVO.builder()
                .description(projectDO.getDescription())
                .computeMode(projectDO.getComputeMode())
                .projectName(projectDO.getName())
                .gmtCreated(DateTimes.toRfc3339(projectDO.getGmtCreate()))
                .build();
        LOGGER.info("nodeManager.getNodeResult, nodeID = {}, fedTableID = {}", nodeID, fedTableID);
        GraphDetailVO graphDetailVO = projectService.getProjectJob(projectDO.getProjectId(), teeDownLoadAuditConfigDO.getJobId()).getGraph();
        TeeDownLoadMessageDetail teeDownLoadMessageDetail = TeeDownLoadMessageDetail.builder()
                .project(projectSimpleVO)
                .partyVoteStatuses(getPartyStatusByVoteID(voteID))
                .tableColumns(columnVOList)
                .graphName(graphDetailVO.getName())
                .graphDetailVO(graphDetailVO)
                .build();
        teeDownLoadMessageDetail.setMessageName(fedTableID);
        teeDownLoadMessageDetail.setStatus(VoteStatusEnum.parse(voteRequestDO.getStatus()));
        teeDownLoadMessageDetail.setTaskID(teeDownLoadAuditConfigDO.getTaskID());
        teeDownLoadMessageDetail.setResourceID(teeDownLoadMessageDetail.getResourceID());
        teeDownLoadMessageDetail.setType(VoteTypeEnum.TEE_DOWNLOAD.name());
        teeDownLoadMessageDetail.setParties(Lists.newArrayList(nodeID));
        return teeDownLoadMessageDetail;
    }

    @Override
    public List<VoteTypeEnum> supportTypes() {
        return Lists.newArrayList(VoteTypeEnum.TEE_DOWNLOAD);
    }

    @Override
    public AbstractVoteTypeMessage getMessageListNecessaryInfo(String voteID) {
        Optional<TeeDownLoadAuditConfigDO> teeDownLoadAuditConfigDOOptional = teeDownLoadAuditConfigRepository.findById(voteID);
        if (!teeDownLoadAuditConfigDOOptional.isPresent()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        return TeeListMessage.builder().projectID(teeDownLoadAuditConfigDOOptional.get().getProjectId()).build();
    }

    @Override
    public void createApproval(String nodeID, AbstractVoteConfig voteConfig) {
        //generate unique vote id
        String voteID = UUIDUtils.newUUID();
        List<String> executors = new ArrayList<>();
        executors.add(nodeID);
        TeeDownLoadVoteConfig teeDownLoadVoteConfig = (TeeDownLoadVoteConfig) voteConfig;
        String requestDesc = teeDownLoadVoteConfig.getResourceID();

        List<ProjectNodeDO> projectNodeDOS = projectNodeRepository.findByProjectId(teeDownLoadVoteConfig.getProjectID());
        List<String> nodeIds = projectNodeDOS.stream().map(e -> e.getUpk().getNodeId()).collect(Collectors.toList());
        TeeDownLoadAuditConfigDO teeDownLoadAuditConfigDO = TeeDownLoadAuditConfigDO.builder()
                .jobId(teeDownLoadVoteConfig.getJobID())
                .taskID(teeDownLoadVoteConfig.getTaskID())
                .resourceID(teeDownLoadVoteConfig.getResourceID())
                .resourceType(teeDownLoadVoteConfig.getResourceType())
                .voteID(voteID)
                .projectId(teeDownLoadVoteConfig.getProjectID())
                .graphId(teeDownLoadVoteConfig.getGraphID())
                .allParticipants(Lists.newArrayList(nodeIds))
                .build();
        teeDownLoadAuditConfigRepository.saveAndFlush(teeDownLoadAuditConfigDO);
        if (!nodeIds.contains(nodeID)) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_INST_NOT_EXISTS);
        }
        List<String> copyList = JsonUtils.deepCopyList(nodeIds, String.class);
        copyList.remove(nodeID);
        VoteRequestBody voteRequestBody = VoteRequestBody.builder()
                .rejectedAction("NODE")
                .approvedAction("TEE_DOWNLOAD," + teeDownLoadAuditConfigDO.getResourceID())
                .type(VoteTypeEnum.TEE_DOWNLOAD.name())
                .approvedThreshold(copyList.size())
                .initiator(nodeID)
                .voteRequestID(voteID)
                .voteCounter("master")
                .voters(copyList)
                .executors(Lists.newArrayList(nodeID))
                .build();
        VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder()
                .body(Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes()))
                .build();
        String voteMsg = JsonUtils.toJSONString(voteRequestMessage);
        //vote participant info
        List<VoteInviteDO> voteInviteDOS = new ArrayList<>();
        String finalInviteDesc = requestDesc;
        copyList.forEach(voter -> {
            VoteInviteDO voteInviteDO = VoteInviteDO.builder()
                    .upk(VoteInviteDO.UPK.builder().voteID(voteID).votePartitionID(voter).build())
                    .initiator(nodeID)
                    .desc(finalInviteDesc)
                    .type(VoteTypeEnum.TEE_DOWNLOAD.name())
                    .action(VoteStatusEnum.REVIEWING.name())
                    .voteMsg(voteMsg)
                    .build();
            voteInviteDOS.add(voteInviteDO);
        });
        voteInviteRepository.saveAllAndFlush(voteInviteDOS);


        //vote initiator info
        VoteRequestDO voteRequestDO = VoteRequestDO.builder()
                .voteID(voteID)
                .initiator(nodeID)
                .type(VoteTypeEnum.TEE_DOWNLOAD.name())
                .voters(copyList)
                .voteCounter("master")
                .requestMsg(voteMsg)
                .executeStatus(VoteExecuteEnum.COMMITTED.name())
                .executors(executors)
                .approvedThreshold(copyList.size())
                .status(VoteStatusEnum.REVIEWING.getCode())
                .desc(requestDesc)
                .build();
        voteRequestRepository.saveAndFlush(voteRequestDO);
    }

    @Override
    public void doCallBack(VoteRequestDO voteRequestDO) {

        String requestMsg = voteRequestDO.getRequestMsg();
        VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
        List<VoteInviteDO> voteInviteDOS = voteInviteRepository.findByVoteID(voteRequestDO.getVoteID());
        List<VoteReplyMessage> replyMessages = new ArrayList<>();
        voteInviteDOS.forEach(e -> {
            replyMessages.add(JsonUtils.toJavaObject(e.getVoteMsg(), VoteReplyMessage.class));
        });
        VoteResult voteResult = VoteResult.builder().voteRequest(voteRequestMessage).voteInvite(replyMessages).build();
        Optional<TeeDownLoadAuditConfigDO> optionalTeeDownLoadAuditConfigDO = teeDownLoadAuditConfigRepository.findById(voteRequestDO.getVoteID());
        TeeDownLoadAuditConfigDO teeDownLoadAuditConfigDO = optionalTeeDownLoadAuditConfigDO.get();
        String resourceID = teeDownLoadAuditConfigDO.getResourceID();
        String projectId = teeDownLoadAuditConfigDO.getProjectId();
        String jobId = teeDownLoadAuditConfigDO.getJobId();
        String taskId = teeDownLoadAuditConfigDO.getTaskID();
        String resourceType = teeDownLoadAuditConfigDO.getResourceType();
        String initiator = voteRequestDO.getInitiator();
        LOGGER.info("platform is {}", envService.getPlatformType().name());
        if (envService.isCenter()) {
            doDownLoad(JsonUtils.toJSONString(voteResult), resourceID, initiator, voteRequestDO, projectId, jobId, taskId, resourceType);
        } else {
            success(voteRequestDO);
        }
    }

    private void doDownLoad(String voteResult, String resourceID, String initiator, VoteRequestDO voteRequestDO, String projectId, String jobId, String taskId, String resourceType) {
        LOGGER.info("voteResult = {}", voteResult);
        LOGGER.info("resourceID = {}", resourceID);
        LOGGER.info("initiator = {}", initiator);
        try {
            LOGGER.info("start do tee downLoad callback");
            datatableService.pullResultFromTeeNode(initiator, resourceID, null, null, null, voteResult, projectId, jobId, taskId, resourceType);
            success(voteRequestDO);
            LOGGER.info("end do tee downLoad callback,success");
        } catch (Exception e) {
            LOGGER.info("end do tee downLoad callback,success,failed,err = {}", e);
            voteRequestDO.setMsg(e.getMessage());
            failed(voteRequestDO);
        }

    }
}
