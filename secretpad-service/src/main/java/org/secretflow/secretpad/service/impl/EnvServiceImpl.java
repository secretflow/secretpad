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

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.dto.EnvDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Service
public class EnvServiceImpl implements EnvService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvServiceImpl.class);
    private static final List<String> EMBEDDED_NODE = Lists.newArrayList("alice", "bob", "tee");
    @Resource
    private NodeRepository nodeRepository;
    @Value("${secretpad.platform-type}")
    private String platformType;
    @Value("${secretpad.node-id}")
    private String nodeId;
    @Resource
    @Setter
    private ProjectNodeRepository projectNodeRepository;

    @Override
    public PlatformTypeEnum getPlatformType() {
        return PlatformTypeEnum.valueOf(platformType);
    }

    @Override
    public String getPlatformNodeId() {
        return nodeId;
    }

    @Override
    public EnvDTO getEnv() {
        EnvDTO envDTO = new EnvDTO();
        envDTO.setPlatformNodeId(nodeId);
        envDTO.setPlatformType(PlatformTypeEnum.valueOf(platformType));
        return envDTO;
    }

    @Override
    public Boolean isCenter() {
        return PlatformTypeEnum.CENTER.name().equals(platformType);
    }

    @Override
    public Boolean isAutonomy() {
        return PlatformTypeEnum.AUTONOMY.name().equals(platformType);
    }

    @Override
    public Boolean isEmbeddedNode(String nodeID) {
        return (EMBEDDED_NODE.contains(nodeID) || DomainConstants.DomainTypeEnum.embedded.name().equals(nodeRepository.findByNodeId(nodeID).getType())) && isCenter();
    }

    @Override
    public Boolean isCurrentNodeEnvironment(String nodeID) {
        LOGGER.debug("platformNodeId = {}", this.nodeId);
        return StringUtils.equals(this.nodeId, nodeID) || isEmbeddedNode(nodeID);
    }

    @Override
    public Boolean isCurrentInstEnvironment(String instID) {
        LOGGER.debug("instId = {}", instID);
        return InstServiceImpl.INST_ID.equals(instID);
    }

    @Override
    public Boolean isP2pEdge() {
        return true;
    }

    @Override
    public Boolean isNodeInCurrentInst(String nodeId) {
        return P2pDataSyncProducerTemplate.nodeIds.contains(nodeId);
    }

    @Override
    public String findLocalNodeId(ProjectJob.JobTask task) {
        String respNode = null;
        if (CollectionUtils.isEmpty(P2pDataSyncProducerTemplate.nodeIds)) {
            return null;
        }
        for (String party : task.getParties()) {
            if (P2pDataSyncProducerTemplate.nodeIds.contains(party)) {
                respNode = party;
            }
        }
        /** if find then return , or mismatch */
        if(StringUtils.isNotEmpty(respNode)){
            return respNode;
        }
        ProjectDO project = GraphContext.getProject();
        if (project == null) {
            return null;
        }
        List<ProjectNodeDO> projectNodeDOS = projectNodeRepository.findByProjectId(Objects.requireNonNull(GraphContext.getProject()).getProjectId());
        if (CollectionUtils.isEmpty(projectNodeDOS)) {
            return null;
        }
        Set<String> nodeIds = projectNodeDOS.stream().map(ProjectNodeDO::getNodeId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(nodeIds)) {
            return null;
        }
        /** if a1 a2 in same inst, but a1 and a2 no domain route,then failed **/
        for (String nodeId : nodeIds) {
            if (P2pDataSyncProducerTemplate.nodeIds.contains(nodeId)) {
                respNode = nodeId;
            }
        }
        return respNode;
    }
}
