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
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.VoteReplyBody;
import org.secretflow.secretpad.service.model.approval.VoteReplyMessage;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * VoteInviteStatusMonitor.
 *
 * @author cml
 * @date 2023/10/24
 */
@Component
public class VoteInviteStatusMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoteInviteStatusMonitor.class);
    @Resource
    private VoteInviteRepository voteInviteRepository;

    @Resource
    private VoteRequestRepository voteRequestRepository;

    @Resource
    private NodeManager nodeManager;

    @Scheduled(initialDelay = 6000, fixedDelay = 5000)
    public void sync() {

        List<VoteRequestDO> voteRequestDOS = voteRequestRepository.findByStatus(VoteStatusEnum.REVIEWING.getCode());
        if (!CollectionUtils.isEmpty(voteRequestDOS)) {
            LOGGER.debug("voteRequestDOS not empty,start sync");
            voteRequestDOS.forEach(voteRequestDO -> {
                String voteID = voteRequestDO.getVoteID();
                LOGGER.debug("voteID :{} start sync", voteID);
                List<VoteInviteDO> voteInviteDOS = voteInviteRepository.findByVoteID(voteID);
                LOGGER.debug("-----voteInviteDOS--- = {}, {}", voteInviteDOS, voteInviteDOS.size());
                if (CollectionUtils.isEmpty(voteInviteDOS)) {
                    LOGGER.debug("maybe voteInvite is embedded node!");
                    return;
                }
                String type = voteRequestDO.getType();
                List<String> executors = voteRequestDO.getExecutors();
                boolean teeNodeRoute = (StringUtils.equals(VoteTypeEnum.NODE_ROUTE.name(), type) && executors.contains("tee"));
                LOGGER.debug("before each verify, check is teeNodeRoute------------------------------- teeNodeRoute = {}", teeNodeRoute);
                if (!teeNodeRoute) {
                    for (VoteInviteDO voteInviteDO : voteInviteDOS) {
                        if (!VoteStatusEnum.REVIEWING.name().equals(voteInviteDO.getAction())) {
                            if (!verify(voteInviteDO, voteRequestDO)) {
                                LOGGER.info("in voteID->{} voteInvite participant {} verify fail", voteInviteDO.getUpk().getVoteID(), voteInviteDO.getUpk().getVotePartitionID());
                                String msg = String.format("in voteID-> %s,voteInvite participant %s verify fail", voteInviteDO.getUpk().getVoteID(), voteInviteDO.getUpk().getVotePartitionID());
                                voteRequestDO.setMsg(msg);
                                voteRequestDO.setStatus(VoteStatusEnum.REJECTED.getCode());
                                voteRequestRepository.save(voteRequestDO);
                                voteInviteDO.setAction(VoteStatusEnum.REJECTED.name());
                                voteInviteRepository.save(voteInviteDO);
                                break;
                            }
                        }
                    }
                }
                if (voteInviteDOS.stream().anyMatch(e -> VoteStatusEnum.REJECTED.name().equals(e.getAction()))) {
                    voteRequestDO.setStatus(VoteStatusEnum.REJECTED.getCode());
                    voteRequestRepository.save(voteRequestDO);
                    LOGGER.info("voteID :{} REJECTED", voteID);
                } else if (voteInviteDOS.stream().allMatch(e -> VoteStatusEnum.APPROVED.name().equals(e.getAction()))) {
                    voteRequestDO.setStatus(VoteStatusEnum.APPROVED.getCode());
                    voteRequestRepository.save(voteRequestDO);
                    LOGGER.info("voteID :{} APPROVED", voteID);
                }
                LOGGER.debug("voteID :{} end sync", voteID);
            });
            LOGGER.debug("voteRequestDOS not empty,end sync");
        }
    }

    private boolean verify(VoteInviteDO voteInviteDO, VoteRequestDO voteRequestDO) {
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
            throw SecretpadException.of(SystemErrorCode.ENCODE_ERROR);
        }
        String voter = voteReplyBody.getVoter();
        String cert = nodeManager.getCert(voter);
        String rootCert = Base64Utils.encode(certChain.get(1).getBytes());
        if (!rootCert.equals(cert)) {
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

