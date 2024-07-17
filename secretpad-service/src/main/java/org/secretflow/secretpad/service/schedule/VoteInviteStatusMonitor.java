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

package org.secretflow.secretpad.service.schedule;

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.EncryptUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.persistence.entity.VoteInviteDO;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.VoteReplyBody;
import org.secretflow.secretpad.service.model.approval.VoteReplyMessage;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * VoteInviteStatusMonitor.
 *
 * @author cml
 * @date 2023/10/24
 */
@Setter
@Component
public class VoteInviteStatusMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteInviteStatusMonitor.class);
    @Resource
    private VoteInviteRepository voteInviteRepository;

    @Resource
    private VoteRequestRepository voteRequestRepository;

    @Resource
    private NodeManager nodeManager;

    @Resource
    private EnvService envService;

    private static final List<String> AUTONOMY_VOTE = Lists.newArrayList(VoteTypeEnum.PROJECT_CREATE.name(), VoteTypeEnum.PROJECT_ARCHIVE.name());

    private static final List<String> CENTER_VOTE = Lists.newArrayList(VoteTypeEnum.TEE_DOWNLOAD.name(), VoteTypeEnum.NODE_ROUTE.name());


    @Scheduled(initialDelay = 6000, fixedDelay = 1000)
    public void sync() {
        List<VoteRequestDO> voteRequestDOS = voteRequestRepository.findByStatus(VoteStatusEnum.REVIEWING.getCode());
        if (!CollectionUtils.isEmpty(voteRequestDOS)) {
            voteRequestDOS.forEach(voteRequestDO -> {
                String voteID = voteRequestDO.getVoteID();
                List<VoteInviteDO> voteInviteDOS = voteInviteRepository.findByVoteID(voteID);
                if (CollectionUtils.isEmpty(voteInviteDOS)) {
                    LOGGER.debug("maybe voteInvite is embedded node!");
                    return;
                }
                String type = voteRequestDO.getType();
                List<String> executors = voteRequestDO.getExecutors();
                if (CENTER_VOTE.contains(type)) {
                    boolean teeNodeRoute = (StringUtils.equals(VoteTypeEnum.NODE_ROUTE.name(), type) && executors.contains("tee"));
                    if (!teeNodeRoute) {
                        verify(voteRequestDO, voteInviteDOS);
                    }
                } else if (AUTONOMY_VOTE.contains(type)) {
                    String initiator = voteRequestDO.getInitiator();
                    //only initiator can calculate the vote in project create vote
                    if (envService.isCurrentNodeEnvironment(initiator)) {
                        for (String executor : executors) {
                            if (StringUtils.equals(executor, initiator)) {
                                verify(voteRequestDO, voteInviteDOS);
                            }
                        }
                    }
                }
                if (voteInviteDOS.stream().anyMatch(e -> VoteStatusEnum.REJECTED.name().equals(e.getAction()))) {
                    voteRequestDO.setStatus(VoteStatusEnum.REJECTED.getCode());
                } else if (voteInviteDOS.stream().allMatch(e -> VoteStatusEnum.APPROVED.name().equals(e.getAction()))) {
                    voteRequestDO.setStatus(VoteStatusEnum.APPROVED.getCode());
                }
                voteRequestRepository.save(voteRequestDO);
                LOGGER.debug("{} monitor------,voteID = {}", type, voteRequestDO.getVoteID());
            });
        }

    }

    private void verify(VoteRequestDO voteRequestDO, List<VoteInviteDO> voteInviteDOS) {
        for (VoteInviteDO voteInviteDO : voteInviteDOS) {
            if (!VoteStatusEnum.REVIEWING.name().equals(voteInviteDO.getAction())
                    && !verify(voteInviteDO, voteRequestDO)
            ) {
                //verify fail
                LOGGER.info("in voteID->{} voteInvite participant {} verify fail", voteInviteDO.getUpk().getVoteID(), voteInviteDO.getUpk().getVotePartitionID());
                String msg = String.format("in voteID-> %s,voteInvite participant %s verify fail", voteInviteDO.getUpk().getVoteID(), voteInviteDO.getUpk().getVotePartitionID());
                voteRequestDO.setMsg(msg);
                voteRequestDO.setStatus(VoteStatusEnum.REJECTED.getCode());

                voteInviteDO.setReason(String.format("verify fail-> %s", voteInviteDO.getUpk().getVotePartitionID()));
                voteInviteDO.setAction(VoteStatusEnum.REJECTED.name());

                // update voteRequestDO partyVoteInfos same as voteInviteDO
                Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = voteRequestDO.getPartyVoteInfos();
                VoteRequestDO.PartyVoteInfo partyVoteInfo = VoteRequestDO.PartyVoteInfo.builder().action(voteInviteDO.getAction()).nodeId(voteInviteDO.getUpk().getVotePartitionID()).reason(voteInviteDO.getReason()).build();
                partyVoteInfos.remove(partyVoteInfo);
                partyVoteInfos.add(partyVoteInfo);

                voteRequestRepository.save(voteRequestDO);
                voteInviteRepository.save(voteInviteDO);
                break;
            }
        }
    }

    public boolean verify(VoteInviteDO voteInviteDO, VoteRequestDO voteRequestDO) {
        LOGGER.info("start verify!");
        boolean result;
        String voteMsg = voteInviteDO.getVoteMsg();
        VoteReplyMessage voteReplyMessage = JsonUtils.toJavaObject(voteMsg, VoteReplyMessage.class);
        String bodyBase64 = voteReplyMessage.getBody();
        List<String> certChain = voteReplyMessage.getCertChain();
        String signature = voteReplyMessage.getSignature();
        String requestMsg = voteRequestDO.getRequestMsg();
        VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
        String voteRequestSignatureBase64 = voteRequestMessage.getVoteRequestSignature();

        String certString = Base64Utils.encode(certChain.get(0).getBytes());
        try {
            result = EncryptUtils.verifySHA256withRSA((bodyBase64 + voteRequestSignatureBase64).getBytes(), certString, signature);

            if (!result) {
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("center verify vote reply signature error,{}", e.getMessage());
            return false;
        }
        VoteReplyBody voteReplyBody;
        try {
            voteReplyBody = JsonUtils.toJavaObject(new String(Base64Utils.decode(bodyBase64)), VoteReplyBody.class);
        } catch (Exception e) {
            LOGGER.error("verify error", e);
            throw SecretpadException.of(SystemErrorCode.ENCODE_ERROR, e);
        }
        String voter = voteReplyBody.getVoter();
        String cert = nodeManager.getCert(voter);
        String rootCert = Base64Utils.encode(certChain.get(1).getBytes());
        if (!EncryptUtils.compareCertPubKey(cert, rootCert)) {
            LOGGER.info("cert does not match,verify fail");
            return false;
        }
        ArrayList<String> chains = Lists.newArrayList(certString, rootCert);
        result = EncryptUtils.validateCertChain(chains);
        if (!result) {
            LOGGER.info("certChain does not match,verify fail");
            return false;
        }
        LOGGER.info("verify success");
        return true;
    }

}

