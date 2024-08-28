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

package org.secretflow.secretpad.persistence.datasync.producer.p2p;

import org.secretflow.secretpad.common.constant.CacheConstants;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.producer.PaddingNodeService;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.NodeInstDTO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectApprovalConfigRepository;
import org.secretflow.secretpad.persistence.repository.ProjectInstRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yutu
 * @date 2023/12/14
 */
@Slf4j
@RequiredArgsConstructor
public class P2pPaddingNodeServiceImpl implements PaddingNodeService {

    private final ProjectInstRepository projectInstRepository;

    private final ProjectApprovalConfigRepository projectApprovalConfigRepository;

    private final VoteRequestRepository voteRequestRepository;

    private final CacheManager cacheManager;

    private final NodeRepository nodeRepository;

    private Map<String, String> inst_Node = new ConcurrentHashMap<>();

    @Override
    public void paddingNodes(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        String projectId = event.getProjectId();
        List<String> nodeIds = new ArrayList<>();
        if (event.getSource() instanceof VoteRequestDO || event.getSource() instanceof VoteInviteDO) {
            nodeIds = event.getNodeIds();
        }
        if (StringUtils.isNotEmpty(projectId)) {
            List<ProjectInstDO> pis = projectInstRepository.findByUpkProjectId(projectId);
            if (!CollectionUtils.isEmpty(pis)) {
                for (ProjectInstDO p : pis) {
                    nodeIds.add(p.getUpk().getInstId());
                }
            }

            Optional<ProjectApprovalConfigDO> projectApprovalConfigDOOptional = projectApprovalConfigRepository.findByProjectIdAndType(projectId, "PROJECT_CREATE");
            if (projectApprovalConfigDOOptional.isPresent()) {
                nodeIds.addAll(projectApprovalConfigDOOptional.get().getNodeIds());
            } else {
                Cache cache = Objects.requireNonNull(cacheManager.getCache(CacheConstants.PROJECT_VOTE_PARTIES_CACHE));
                if (Objects.nonNull(cache.get(projectId))) {
                    ArrayList<String> parties = (ArrayList) cache.get(projectId).get();
                    if (!CollectionUtils.isEmpty(parties)) {
                        log.info("cache hit,projectId ={}, parties ={}", projectId, parties);
                        nodeIds.addAll(parties);
                    }
                }
            }
        }
        List<String> collect = nodeIds.stream().distinct().collect(Collectors.toList());
        event.setNodeIds(collect);
        List<NodeInstDTO> nodeDOList = nodeRepository.findInstMasterNodeId();
        for (NodeInstDTO nodeInstDto : nodeDOList) {
            inst_Node.put(nodeInstDto.getInstId(), nodeInstDto.getMasterNodeId());
        }
    }

    @Override
    public void compensate(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        BaseAggregationRoot source = event.getSource();
        if (source instanceof VoteInviteDO) {
            VoteInviteDO voteInviteDO = (VoteInviteDO) event.getSource();
            Optional<VoteRequestDO> voteRequestDOOptional = voteRequestRepository.findById(voteInviteDO.getUpk().getVoteID());
            if (voteRequestDOOptional.isPresent()) {
                VoteRequestDO voteRequestDO = voteRequestDOOptional.get();
                Set<VoteRequestDO.PartyVoteInfo> partyVoteInfos = voteRequestDO.getPartyVoteInfos();
                for (VoteRequestDO.PartyVoteInfo partyVoteInfo : partyVoteInfos) {
                    if (voteInviteDO.getUpk().getVotePartitionID().equals(partyVoteInfo.getPartyId())) {
                        log.debug("inst -> {} compensate action {}", partyVoteInfo.getPartyId(), voteInviteDO.getAction());
                        partyVoteInfo.setAction(voteInviteDO.getAction());
                        partyVoteInfo.setReason(voteInviteDO.getReason());
                        voteRequestDO.setGmtModified(LocalDateTime.now());
                        voteRequestRepository.save(voteRequestDO);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void supInstInfo(AccountsDO accountsDO) {
        Assert.notNull(accountsDO, "accountsDO is null");
        Assert.notNull(accountsDO.getInstId(), "instId IS null");
        P2pDataSyncProducerTemplate.instId = accountsDO.getInstId();
    }

    @Override
    public String turnInstToRouteId(String instId) {
        log.info("inst_Node = {} from {} to {}", inst_Node, instId, inst_Node.get(instId));
        return inst_Node.get(instId);
    }
}