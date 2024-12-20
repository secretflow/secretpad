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

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.VoteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.MessageService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.handler.vote.VoteTypeHandler;
import org.secretflow.secretpad.service.model.approval.VoteReplyBody;
import org.secretflow.secretpad.service.model.approval.VoteReplyMessage;
import org.secretflow.secretpad.service.model.approval.VoteRequestBody;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.message.*;
import org.secretflow.secretpad.service.util.DbSyncUtil;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MessageServiceImplement.
 *
 * @author cml
 * @date 2023/09/20
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final VoteInviteRepository voteInviteRepository;

    private final VoteRequestRepository voteRequestRepository;

    private final NodeRepository nodeRepository;

    private final InstRepository instRepository;

    private final Map<VoteTypeEnum, VoteTypeHandler> voteTypeHandlerMap;


    private final VoteRequestCustomRepository voteRequestCustomRepository;

    private final VoteInviteCustomRepository voteInviteCustomRepository;

    private final EnvService envService;

    private final CertificateService certificateService;
    private final NodeRouteManager nodeRouteManager;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reply(String action, String reason, String voteParticipantId, String voteId) {
        identityVerification(voteParticipantId);
        Optional<VoteInviteDO> voteInviteDOOptional = voteInviteRepository.findById(new VoteInviteDO.UPK(voteId, voteParticipantId));
        if (voteInviteDOOptional.isEmpty()) {
            LOGGER.error("Cannot find vote info by voteID {} and voteParticipantID {}.", voteId, voteParticipantId);
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        VoteInviteDO voteInviteDO = voteInviteDOOptional.get();
        String voter = voteTypeHandlerMap.get(VoteTypeEnum.valueOf(voteInviteDO.getType())).getVoter(voteParticipantId);
        Optional<VoteRequestDO> optionalVoteRequestDO = voteRequestRepository.findById(voteId);
        if (optionalVoteRequestDO.isEmpty()) {
            throw SecretpadException.of(VoteErrorCode.VOTE_NOT_EXISTS);
        }
        // Check whether the route exists, master node routing and invitation node routing, double check
        checkVoteRoute(voteInviteDO);
        VoteRequestDO voteRequestDO = optionalVoteRequestDO.get();
        String requestMsg = voteRequestDO.getRequestMsg();
        String teeAction = VoteStatusEnum.APPROVED.name().equals(action) ? "APPROVE" : "REJECT";
        LOGGER.debug("requestMsg = {},voteID = {}", requestMsg, voteId);
        VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
        String voteType = voteRequestDO.getType();
        List<String> executors = voteRequestDO.getExecutors();
        boolean teeNodeRoute = (StringUtils.equals(VoteTypeEnum.NODE_ROUTE.name(), voteType) && executors.contains("tee"));
        if (!teeNodeRoute) {
            if (StringUtils.isBlank(voteRequestMessage.getVoteRequestSignature())) {
                throw SecretpadException.of(VoteErrorCode.VOTE_SIGNATURE_SYNCHRONIZING);
            }
        }

        VoteRequestBody voteRequestBody;
        voteRequestBody = JsonUtils.toJavaObject(new String(Base64Utils.decode(voteRequestMessage.getBody())), VoteRequestBody.class);
        if (!voteRequestBody.getVoters().contains(voteParticipantId)) {
            throw SecretpadException.of(VoteErrorCode.VOTE_CHECK_FAILED);
        }
        //tee node route can not sign in tee node,so skip
        if (teeNodeRoute) {
            voteInviteDO.setAction(action);
        } else {
            Certificate.GenerateKeyCertsResponse generateKeyCertsResponse = certificateService.generateCertByNodeID(voter);
            String privateKey = generateKeyCertsResponse.getKey();
            List<String> certChainList = generateKeyCertsResponse.getCertChainList();
            VoteReplyBody voteReplyBodyBaseInfo = VoteReplyBody.builder()
                    .voteRequestID(voteId)
                    .voter(voter)
                    .action(teeAction)
                    .build();
            String voteStr = JsonUtils.toJSONString(voteReplyBodyBaseInfo);
            String invitorBase64Signature = Base64Utils.encode(voteStr.getBytes());
            String initiatorBase64Signature = voteRequestMessage.getVoteRequestSignature();
            String sign = EncryptUtils.signSHA256withRSA((invitorBase64Signature + initiatorBase64Signature).getBytes(), privateKey);
            List<String> decodeChain = certChainList.stream().map(e -> new String(Base64Utils.decode(e))).collect(Collectors.toList());
            VoteReplyMessage voteReplyMessage = VoteReplyMessage.builder()
                    .body(invitorBase64Signature)
                    .certChain(decodeChain)
                    .signature(sign)
                    .build();
            voteInviteDO.setVoteMsg(JsonUtils.toJSONString(voteReplyMessage));
            voteInviteDO.setAction(action);
        }
        voteInviteDO.setReason(reason);

        PlatformTypeEnum platformType = envService.getPlatformType();
        DbSyncRequest dbSyncRequest = DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.VOTE_INVITE.name()).projectNodesInfo(voteInviteDO).build();
        switch (platformType) {
            //center,it is embedded node
            case CENTER -> voteInviteRepository.saveAndFlush(voteInviteDO);
            //send to center, center sync data to edge
            case EDGE -> DbSyncUtil.dbDataSyncToCenter(dbSyncRequest);
            //p2p,according to node id rule sync
            case AUTONOMY -> {
                voteInviteRepository.saveAndFlush(voteInviteDO);
                Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = voteRequestDO.getPartyVoteInfos();
                VoteRequestDO.PartyVoteInfo partyVoteInfo = VoteRequestDO.PartyVoteInfo.builder().action(voteInviteDO.getAction()).partyId(voteInviteDO.getUpk().getVotePartitionID()).reason(voteInviteDO.getReason()).build();
                partyVoteInfos.remove(partyVoteInfo);
                partyVoteInfos.add(partyVoteInfo);
                if (partyVoteInfos.stream().allMatch(e -> StringUtils.equals(e.getAction(), VoteStatusEnum.APPROVED.name()))) {
                    voteTypeHandlerMap.get(VoteTypeEnum.valueOf(voteType)).flushVoteStatus(voteId);
                }
                voteRequestDO.setGmtModified(LocalDateTime.now());
                voteRequestRepository.saveAndFlush(voteRequestDO);
            }
        }
    }

    private void identityVerification(String ownerId) {
        if (envService.isAutonomy()) {
            if (!StringUtils.equals(UserContext.getUser().getOwnerId(), ownerId)) {
                throw SecretpadException.of(VoteErrorCode.VOTE_CHECK_FAILED);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageListVO list(Boolean isInitiator, String ownerId, String type, String keyWord, Boolean isProcessed, Pageable page) {
        identityVerification(ownerId);
        if (!isInitiator) {
            List<VoteInviteDO> voteInviteDOS = voteInviteCustomRepository.pageQuery(ownerId, isProcessed, type, keyWord, page);
            Long total = voteInviteCustomRepository.queryCount(ownerId, isProcessed, type, keyWord);
            List<MessageVO> messageVOS = PageUtils.convert(voteInviteDOS, this::convert2VO);
            return MessageListVO.newInstance(messageVOS, page.getPageNumber() + 1, page.getPageSize(), total);
        } else {
            List<VoteRequestDO> voteRequestDOS = voteRequestCustomRepository.pageQuery(ownerId, type, keyWord, isProcessed, page);
            Long total = voteRequestCustomRepository.queryCount(ownerId, type, keyWord);
            List<MessageVO> messageVOS = PageUtils.convert(voteRequestDOS, this::convert2VO);
            return MessageListVO.newInstance(messageVOS, page.getPageNumber() + 1, page.getPageSize(), total);
        }
    }

    @Override
    public MessageDetailVO detail(Boolean isInitiator, String ownerId, String voteID, String voteType) {
        identityVerification(ownerId);
        VoteTypeHandler voteTypeHandler = voteTypeHandlerMap.get(VoteTypeEnum.valueOf(voteType));
        return voteTypeHandler.getVoteMessageDetail(isInitiator, ownerId, voteID);
    }

    @Override
    public Long pendingCount(String ownerId) {
        identityVerification(ownerId);
        return voteInviteRepository.queryPendingCount(ownerId, VoteStatusEnum.REVIEWING.name());
    }


    private MessageVO convert2VO(VoteInviteDO inviteDO) {
        Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findByVoteID(inviteDO.getUpk().getVoteID());
        if (voteRequestDOOptional.isEmpty()) {
            LOGGER.error("Cannot find vote request  by voteID {}", inviteDO.getUpk().getVoteID());
            return null;
        }
        String initiator = voteRequestDOOptional.get().getInitiator();
        String initiatorNodeName;
        if (envService.getPlatformType().equals(PlatformTypeEnum.AUTONOMY)) {
            initiatorNodeName = instRepository.findByInstId(initiator).getName();
        } else {
            initiatorNodeName = nodeRepository.findByNodeId(initiator).getName();
        }
        return MessageVO.builder()
                .createTime(DateTimes.toRfc3339(inviteDO.getGmtCreate()))
                .voteID(inviteDO.getUpk().getVoteID())
                .type(inviteDO.getType())
                .messageName(inviteDO.getDesc())
                .status(inviteDO.getAction())
                .initiatingTypeMessage(ReceiverMessage.builder().participantID(inviteDO.getUpk().getVotePartitionID()).reason(inviteDO.getReason()).initiatorNodeID(initiator).initiatorNodeName(initiatorNodeName).build())
                .voteTypeMessage(voteTypeHandlerMap.get(VoteTypeEnum.valueOf(inviteDO.getType())).getMessageListNecessaryInfo(inviteDO.getUpk().getVoteID()))
                .build();
    }

    private MessageVO convert2VO(VoteRequestDO requestDO) {
        String voteID = requestDO.getVoteID();
        List<? extends PartyVoteStatus> partyVoteStatuses = voteTypeHandlerMap.get(VoteTypeEnum.valueOf(requestDO.getType())).getPartyStatusByVoteID(voteID);
        InitiatorMessage initiatorMessage = InitiatorMessage.builder()
                .partyVoteStatuses(partyVoteStatuses)
                .build();
        return MessageVO.builder()
                .createTime(DateTimes.toRfc3339(requestDO.getGmtCreate()))
                .voteID(voteID)
                .type(requestDO.getType())
                .messageName(requestDO.getDesc())
                .status(VoteStatusEnum.parse(requestDO.getStatus()))
                .initiatingTypeMessage(initiatorMessage)
                .voteTypeMessage(voteTypeHandlerMap.get(VoteTypeEnum.valueOf(requestDO.getType())).getMessageListNecessaryInfo(requestDO.getVoteID()))
                .build();
    }

    private void checkVoteRoute(VoteInviteDO voteInviteDO) {
        if (envService.isAutonomy()) {
            String initiator = voteInviteDO.getInitiator();
            List<NodeDO> byInstId = nodeRepository.findByInstId(initiator);
            if (CollectionUtils.isEmpty(byInstId)) {
                throw SecretpadException.of(VoteErrorCode.VOTE_INITIATOR_INST_NOT_FOUND, "initiator inst not found");
            }
            String dstMasterNodeId = byInstId.get(0).getMasterNodeId();
            String myMasterNodeId = envService.getPlatformNodeId();
            if (!nodeRouteManager.checkNodeRouteReady(myMasterNodeId, dstMasterNodeId, myMasterNodeId)) {
                throw SecretpadException.of(VoteErrorCode.VOTE_MASTER_ROUTE_NOT_READY, myMasterNodeId + "->" + dstMasterNodeId);
            }
        }
    }

}
