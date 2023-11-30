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

import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.enums.VoteExecuteEnum;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.AbstractVoteConfig;
import org.secretflow.secretpad.service.model.approval.NodeRouteVoteConfig;
import org.secretflow.secretpad.service.model.approval.VoteRequestBody;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;
import org.secretflow.secretpad.service.model.message.AbstractVoteTypeMessage;
import org.secretflow.secretpad.service.model.message.MessageDetailVO;
import org.secretflow.secretpad.service.model.message.NodeRoteMessageDetail;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * NodeRouteMessageHandler.
 *
 * @author cml
 * @date 2023/09/28
 */
@Component
public class NodeRouteMessageHandler extends AbstractVoteTypeHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(NodeRouteMessageHandler.class);

    private final NodeRouteAuditConfigRepository nodeRouteAuditConfigRepository;

    private final NodeRouterService nodeRouterService;

    private final NodeRouteRepository nodeRouteRepository;


    public NodeRouteMessageHandler(VoteInviteRepository voteInviteRepository, NodeRepository nodeRepository, EnvService envService, NodeRouteAuditConfigRepository nodeRouteAuditConfigRepository, VoteRequestRepository voteRequestRepository, NodeRouterService nodeRouterService, NodeRouteRepository nodeRouteRepository) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, envService);
        this.nodeRouteAuditConfigRepository = nodeRouteAuditConfigRepository;
        this.nodeRouterService = nodeRouterService;
        this.nodeRouteRepository = nodeRouteRepository;
    }

    @Override
    public MessageDetailVO getVoteMessageDetail(Boolean isInitiator, String nodeID, String voteID) {
        Optional<NodeRouteApprovalConfigDO> nodeRouteAuditConfigDOOptional = nodeRouteAuditConfigRepository.findById(voteID);
        if (!nodeRouteAuditConfigDOOptional.isPresent()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        NodeRouteApprovalConfigDO nodeRouteApprovalConfigDO = nodeRouteAuditConfigDOOptional.get();
        String messageName;
        String computeNodeID;
        String computeNodeName;
        String initiatorNodeID;
        String initiatorNodeName;
        String url;
        String status;
        String reason;
        if (isInitiator) {
            Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(voteID);
            if (!voteRequestDOOptional.isPresent()) {
                throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
            }
            String desNodeID = nodeRouteApprovalConfigDO.getDesNodeID();
            messageName = voteRequestDOOptional.get().getDesc();
            Optional<VoteInviteDO> voteInviteDOOptional = voteInviteRepository.findById(new VoteInviteDO.UPK(voteID, desNodeID));
            if (!voteInviteDOOptional.isPresent()) {
                throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
            }
            computeNodeID = voteInviteDOOptional.get().getUpk().getVotePartitionID();
            NodeDO nodeDO = nodeRepository.findByNodeId(computeNodeID);
            computeNodeName = nodeDO.getName();
            initiatorNodeID = voteRequestDOOptional.get().getInitiator();
            initiatorNodeName = nodeRepository.findByNodeId(initiatorNodeID).getName();
            url = nodeRouteApprovalConfigDO.getDesNodeAddr();
            status = VoteStatusEnum.parse(voteRequestDOOptional.get().getStatus());
            reason = voteInviteDOOptional.get().getReason();
        } else {
            Optional<VoteInviteDO> voteInviteDOOptional = voteInviteRepository.findById(new VoteInviteDO.UPK(voteID, nodeID));
            if (!voteInviteDOOptional.isPresent()) {
                throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
            }
            messageName = voteInviteDOOptional.get().getDesc();
            Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(voteID);
            if (!voteRequestDOOptional.isPresent()) {
                throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
            }
            VoteRequestDO voteRequestDO = voteRequestDOOptional.get();
            initiatorNodeID = voteRequestDO.getInitiator();
            status = VoteStatusEnum.parse(voteRequestDO.getStatus());
            NodeDO nodeDO = nodeRepository.findByNodeId(initiatorNodeID);
            initiatorNodeName = nodeDO.getName();
            computeNodeID = voteInviteDOOptional.get().getUpk().getVotePartitionID();
            computeNodeName = nodeRepository.findByNodeId(computeNodeID).getName();
            url = nodeRouteApprovalConfigDO.getSrcNodeAddr();
            reason = voteInviteDOOptional.get().getReason();
        }
        NodeRoteMessageDetail messageDetail = NodeRoteMessageDetail.builder()
                .url(url)
                .nodeName(computeNodeName)
                .nodeID(computeNodeID)
                .initiatorNodeID(initiatorNodeID)
                .initiatorNodeName(initiatorNodeName)
                .reason(reason)
                .build();
        messageDetail.setMessageName(messageName);
        messageDetail.setStatus(status);
        messageDetail.setType(VoteTypeEnum.NODE_ROUTE.name());
        return messageDetail;
    }

    @Override
    public List<VoteTypeEnum> supportTypes() {
        return Lists.newArrayList(VoteTypeEnum.NODE_ROUTE);
    }

    @Override
    public AbstractVoteTypeMessage getMessageListNecessaryInfo(String voteID) {
        return null;
    }

    @Override
    public void createApproval(String nodeID, AbstractVoteConfig voteConfig) {
        checkDataPermissions(nodeID);
        NodeRouteVoteConfig nodeRouteVoteConfig = (NodeRouteVoteConfig) voteConfig;
        String srcNodeID = nodeRouteVoteConfig.getSrcNodeId();
        checkDataPermissions(nodeID, srcNodeID);
        String desNodeID = nodeRouteVoteConfig.getDesNodeId();
        //check node route exists
        Optional<NodeRouteDO> nodeRouteDOOptional = nodeRouteRepository.findBySrcNodeIdAndDstNodeId(desNodeID, srcNodeID);
        if (nodeRouteDOOptional.isPresent()) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_ALREADY_EXISTS, desNodeID + " -> " + srcNodeID);
        }
        //generate unique vote id
        String voteID = UUIDUtils.newUUID();
        List<String> executors = new ArrayList<>();

        NodeDO descNodeNO = nodeRepository.findByNodeId(desNodeID);
        String requestDesc = descNodeNO.getName();
        NodeDO srcNodeDO = nodeRepository.findByNodeId(srcNodeID);
        String inviteDesc = srcNodeDO.getName();
        executors.add(srcNodeID);
        executors.add(desNodeID);
        NodeRouteApprovalConfigDO nodeRouteApprovalConfigDO = NodeRouteApprovalConfigDO.builder()
                .desNodeID(desNodeID)
                .srcNodeID(srcNodeID)
                .desNodeAddr(nodeRouteVoteConfig.getDesNodeAddr())
                .srcNodeAddr(nodeRouteVoteConfig.getSrcNodeAddr())
                .isSingle(false)
                .voteID(voteID)
                .allParticipants(Lists.newArrayList(nodeID, nodeRouteVoteConfig.getDesNodeId()))
                .build();
        nodeRouteAuditConfigRepository.saveAndFlush(nodeRouteApprovalConfigDO);
        VoteRequestBody voteRequestBody = VoteRequestBody.builder()
                .rejectedAction("NODE")
                .approvedAction("NODE_ROUTE," + nodeRouteApprovalConfigDO.getDesNodeID())
                .type(VoteTypeEnum.NODE_ROUTE.name())
                .approvedThreshold(1)
                .initiator(nodeID)
                .voteRequestID(voteID)
                .voteCounter("master")
                .voters(Lists.newArrayList(desNodeID))
                .executors(executors)
                .build();
        VoteRequestMessage voteRequestMessage = VoteRequestMessage.builder()
                .body(Base64Utils.encode(JsonUtils.toJSONString(voteRequestBody).getBytes()))
                .build();
        String voteMsg = JsonUtils.toJSONString(voteRequestMessage);
        //vote participant info
        List<VoteInviteDO> voteInviteDOS = new ArrayList<>();

        VoteInviteDO voteInviteDO = VoteInviteDO.builder()
                .upk(VoteInviteDO.UPK.builder().voteID(voteID).votePartitionID(desNodeID).build())
                .initiator(nodeID)
                .desc(inviteDesc)
                .action(VoteStatusEnum.REVIEWING.name())
                .voteMsg(voteMsg)
                .type(VoteTypeEnum.NODE_ROUTE.name())
                .build();
        voteInviteDOS.add(voteInviteDO);

        voteInviteRepository.saveAllAndFlush(voteInviteDOS);


        //vote initiator info
        VoteRequestDO voteRequestDO = VoteRequestDO.builder()
                .voteID(voteID)
                .initiator(nodeID)
                .type(VoteTypeEnum.NODE_ROUTE.name())
                .voters(Lists.newArrayList(desNodeID))
                .voteCounter("master")
                .requestMsg(voteMsg)
                .executeStatus(VoteExecuteEnum.COMMITTED.name())
                .executors(executors)
                .approvedThreshold(1)
                .status(VoteStatusEnum.REVIEWING.getCode())
                .desc(requestDesc)
                .build();
        voteRequestRepository.saveAndFlush(voteRequestDO);
    }

    @Override
    @Transactional
    public void doCallBack(VoteRequestDO voteRequestDO) {
        LOGGER.info("start do callback of node route,voteID = {}", voteRequestDO.getVoteID());
        String voteID = voteRequestDO.getVoteID();
        Optional<NodeRouteApprovalConfigDO> nodeRouteApprovalConfigDOOptional = nodeRouteAuditConfigRepository.findById(voteID);
        if (!nodeRouteApprovalConfigDOOptional.isPresent()) {
            LOGGER.error("node route callback fail,could not find nodeRouteApprovalConfigDO by voteID");
            return;
        }
        LOGGER.debug("query nodeRouteAuditConfigRepository end, result = {}", JsonUtils.toJSONString(nodeRouteApprovalConfigDOOptional.get()));
        NodeRouteApprovalConfigDO nodeRouteApprovalConfigDO = nodeRouteApprovalConfigDOOptional.get();
        //"node route create",the executor's destination only can be itself,and both sides should participate in
        List<String> voteExecutors = voteRequestDO.getExecutors();
        String initiator = voteRequestDO.getInitiator();
        LOGGER.info("initiator = {}", initiator);
        LOGGER.info("voteExecutors = {}", voteExecutors);
        boolean isObserver = true;
        for (String voteExecutor : voteExecutors) {
            if (envService.isCurrentNodeEnvironment(voteExecutor)) {
                isObserver = false;
                //judge current node is initiator or invitor
                //if initiator, srcNode ，dstNode，srcAddr，dstAddr remain unChanged
                CreateNodeRouterRequest createNodeRouterRequest;
                if (StringUtils.equals(voteExecutor, initiator)) {
                    createNodeRouterRequest = CreateNodeRouterRequest.builder()
                            .srcNodeId(nodeRouteApprovalConfigDO.getDesNodeID())
                            .dstNodeId(nodeRouteApprovalConfigDO.getSrcNodeID())
                            .srcNetAddress(nodeRouteApprovalConfigDO.getDesNodeAddr())
                            .dstNetAddress(nodeRouteApprovalConfigDO.getSrcNodeAddr())
                            .routeType(DomainRouterConstants.DomainRouterTypeEnum.HalfDuplex.name())
                            .build();

                } else {
                    //（srcNode ，dstNode），（srcAddr，dstAddr） interchange
                    createNodeRouterRequest = CreateNodeRouterRequest.builder()
                            .srcNodeId(nodeRouteApprovalConfigDO.getSrcNodeID())
                            .dstNodeId(nodeRouteApprovalConfigDO.getDesNodeID())
                            .srcNetAddress(nodeRouteApprovalConfigDO.getSrcNodeAddr())
                            .dstNetAddress(nodeRouteApprovalConfigDO.getDesNodeAddr())
                            .routeType(DomainRouterConstants.DomainRouterTypeEnum.HalfDuplex.name())
                            .build();
                }
                LOGGER.info("this is {}!", envService.getPlatformType().name());
                LOGGER.info("node route creator : {}", voteExecutor);
                createUserContext(voteExecutor);
                createNodeRoute(voteRequestDO, createNodeRouterRequest);
                removeUserContext();
            }
        }
        if (isObserver) {
            LOGGER.info("current is observer,do nothing");
            observer(voteRequestDO);
        }
    }

    private void createUserContext(String srcNodeID) {
        UserContextDTO dto = new UserContextDTO();
        dto.setName(srcNodeID);
        UserContext.setBaseUser(dto);
    }

    private void removeUserContext() {
        UserContext.remove();
    }

    public void createNodeRoute(VoteRequestDO voteRequestDO, CreateNodeRouterRequest createNodeRouterRequest) {
        LOGGER.info("srcNode = {}", createNodeRouterRequest.getSrcNodeId());
        LOGGER.info("destNode = {}", createNodeRouterRequest.getDstNodeId());
        LOGGER.info("srcAddr = {}", createNodeRouterRequest.getSrcNetAddress());
        LOGGER.info("destAddr = {}", createNodeRouterRequest.getDstNetAddress());
        LOGGER.info("check node route exists");

        try {
            if (checkNodeRouteExists(createNodeRouterRequest.getSrcNodeId(), createNodeRouterRequest.getDstNodeId()) || doCreate(createNodeRouterRequest)) {
                success(voteRequestDO);
            }
        } catch (Exception e) {
            LOGGER.error("createNodeRoute error ", e);
            voteRequestDO.setMsg(e.getMessage());
            failed(voteRequestDO);
        }
    }

    public boolean doCreate(CreateNodeRouterRequest createNodeRouterRequest) {
        LOGGER.info("do Create Node route");
        nodeRouterService.createNodeRouter(createNodeRouterRequest);
        LOGGER.info("node create end ....");
        return true;
    }

    private boolean checkNodeRouteExists(String sourceNodeId, String destNodeId) {
        Optional<NodeRouteDO> nodeRouteDOOptional = nodeRouteRepository.findBySrcNodeIdAndDstNodeId(sourceNodeId, destNodeId);
        if (nodeRouteDOOptional.isPresent()) {
            LOGGER.info("node route already exists,unnecessary repeated create,return,sourceID->{},destID->{}", sourceNodeId, destNodeId);
            return true;
        }
        return false;
    }

    private void checkDataPermissions(String nodeId) {
        UserContextDTO user = UserContext.getUser();
        if (user.getPlatformType().equals(PlatformTypeEnum.EDGE)) {
            if (!user.getOwnerId().equals(nodeId)) {
                throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "no Permissions");
            }
        }
    }

    private void checkDataPermissions(String nodeId, String srcNodeID) {
        if (!StringUtils.equals(nodeId, srcNodeID)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "no Permissions");
        }
    }
}
