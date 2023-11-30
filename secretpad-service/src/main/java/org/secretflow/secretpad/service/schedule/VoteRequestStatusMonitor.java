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

import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.EncryptUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteExecuteEnum;
import org.secretflow.secretpad.service.enums.VoteStatusEnum;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.handler.VoteTypeHandler;
import org.secretflow.secretpad.service.model.approval.VoteRequestMessage;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.util.PushToCenterUtil;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VoteRequestStatusMonitor.
 *
 * @author cml
 * @date 2023/10/24
 */
@Component
public class VoteRequestStatusMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteRequestStatusMonitor.class);

    @Resource
    private VoteRequestRepository voteRequestRepository;

    @Resource
    private Map<VoteTypeEnum, VoteTypeHandler> voteTypeHandlerMap;

    @Resource
    private CertificateService certificateService;

    @Resource
    private EnvService envService;

    @Scheduled(initialDelay = 6000, fixedDelay = 5000)
    public void sync() {
        List<VoteRequestDO> maybeNotSigns = voteRequestRepository.findByStatusAndExecuteStatus(VoteStatusEnum.REVIEWING.getCode(), VoteExecuteEnum.COMMITTED.name());
        sign(maybeNotSigns);
        List<VoteRequestDO> notExecutedTasks = voteRequestRepository.findByStatusAndExecuteStatus(VoteStatusEnum.APPROVED.getCode(), VoteExecuteEnum.COMMITTED.name());
        LOGGER.debug("notExecutedTasks = {} size = {}", notExecutedTasks, notExecutedTasks.size());
        if (!CollectionUtils.isEmpty(notExecutedTasks)) {
            LOGGER.debug("voteRequestDOS not empty,start sync");
            notExecutedTasks.forEach(e -> voteTypeHandlerMap.get(VoteTypeEnum.valueOf(e.getType())).doCallBack(e));
            LOGGER.debug("voteRequestDOS not empty,end sync");
        }
    }

    //center  alice
    public void sign(List<VoteRequestDO> maybeNotSigns) {
        ArrayList<String> embedded = Lists.newArrayList("alice", "bob");
        if (!CollectionUtils.isEmpty(maybeNotSigns)) {
            for (VoteRequestDO voteRequestDO : maybeNotSigns) {
                String initiator = voteRequestDO.getInitiator(); //alice bob carole
                String platformNodeId = envService.getPlatformNodeId();//master-kusica
                if (envService.isCenter()) {
                    //center
                    if (!embedded.contains(initiator)) {
                        continue;
                    }

                } else {
                    //edge
                    if (!initiator.equals(platformNodeId)) {
                        continue;
                    }
                }


                //sign,if signature is blank
                String requestMsg = voteRequestDO.getRequestMsg();
                VoteRequestMessage voteRequestMessage = JsonUtils.toJavaObject(requestMsg, VoteRequestMessage.class);
                String voteRequestBodyBase64 = voteRequestMessage.getBody();
                boolean notSigned = StringUtils.isBlank(voteRequestMessage.getVoteRequestSignature()) && CollectionUtils.isEmpty(voteRequestMessage.getCertChain());
                String voteType = voteRequestDO.getType();
                List<String> executors = voteRequestDO.getExecutors();
                boolean teeNodeRoute = (StringUtils.equals(VoteTypeEnum.NODE_ROUTE.name(), voteType) && executors.contains("tee"));
                LOGGER.debug("before sign check is notSigned = {},teeNodeRoute------------------------------- teeNodeRoute = {}", notSigned, teeNodeRoute);
                if (notSigned && !teeNodeRoute) {
                    Certificate.GenerateKeyCertsResponse generateKeyCertsResponse = certificateService.generateCertByNodeID(initiator);
                    String privateKey = generateKeyCertsResponse.getKey();
                    List<String> certChainList = generateKeyCertsResponse.getCertChainList();
                    List<String> pemChains = certChainList.stream().map(e -> new String(Base64Utils.decode(e))).collect(Collectors.toList());
                    String voteRequestSignature = EncryptUtils.signSHA256withRSA(voteRequestBodyBase64.getBytes(), privateKey);
                    voteRequestMessage.setCertChain(pemChains);
                    voteRequestMessage.setVoteRequestSignature(voteRequestSignature);
                    voteRequestDO.setRequestMsg(JsonUtils.toJSONString(voteRequestMessage));
                    LOGGER.debug("maybeNotSigns sign end");
                    if (envService.isCenter()) {
                        LOGGER.info("this is center , data write in db");
                        //center，direct write db
                        voteRequestRepository.save(voteRequestDO);
                    } else {
                        voteRequestDO.setGmtModified(null);
                        voteRequestDO.setGmtCreate(null);
                        VoteSyncRequest voteSyncRequest = VoteSyncRequest.builder().syncDataType(VoteSyncTypeEnum.VOTE_REQUEST.name()).projectNodesInfo(voteRequestDO).build();
                        PushToCenterUtil.dataPushToCenter(voteSyncRequest);
                    }
                }
            }
        }
    }
}
