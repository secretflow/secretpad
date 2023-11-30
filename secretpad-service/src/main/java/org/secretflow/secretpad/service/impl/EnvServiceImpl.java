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
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.dto.EnvDTO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.EnvService;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Service
public class EnvServiceImpl implements EnvService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvServiceImpl.class);

    @Resource
    private NodeRepository nodeRepository;
    @Value("${secretpad.platform-type}")
    private String plaformType;

    @Value("${secretpad.node-id}")
    private String nodeId;

    private static final List<String> EMBEDDED_NODE = Lists.newArrayList("alice", "bob", "tee");

    @Override
    public PlatformTypeEnum getPlatformType() {
        return PlatformTypeEnum.valueOf(plaformType);
    }

    @Override
    public String getPlatformNodeId() {
        return nodeId;
    }

    @Override
    public EnvDTO getEnv() {
        EnvDTO envDTO = new EnvDTO();
        envDTO.setPlatformNodeId(nodeId);
        envDTO.setPlatformType(PlatformTypeEnum.valueOf(plaformType));
        return envDTO;
    }

    @Override
    public Boolean isCenter() {
        return PlatformTypeEnum.CENTER.name().equals(plaformType);
    }

    @Override
    public Boolean isEmbeddedNode(String nodeID) {
        return EMBEDDED_NODE.contains(nodeID) || (DomainConstants.DomainTypeEnum.embedded.name().equals(nodeRepository.findByNodeId(nodeID).getType()) && isCenter());
    }

    @Override
    public Boolean isCurrentNodeEnvironment(String nodeID) {
        LOGGER.debug("platformNodeId = {}", this.nodeId);
        return StringUtils.equals(this.nodeId, nodeID) || isEmbeddedNode(nodeID);
    }
}
