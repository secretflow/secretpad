/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenmingliang
 * @date 2024/07/18
 */
@Component
@Slf4j
public class AutonomyNodeRouteUtil {


    private static NodeRouteRepository nodeRouteRepository;

    private static NodeRepository nodeRepository;

    private static NodeRouteManager nodeRouteManager;

    public static Map<String, List<AutonomySourceNodeRouteInfo>> getAutonomySelfDstNodeRouteInfoMap() {
        Map<String, List<AutonomySourceNodeRouteInfo>> autonomySelfDstNodeRouteInfoMap = new HashMap<>();
        String instId = UserContext.getUser().getOwnerId();
        List<NodeDO> nodeDOS = nodeRepository.findByInstId(instId);
        List<String> nodeIds = nodeDOS.stream().map(NodeDO::getNodeId).collect(Collectors.toList());
        List<NodeRouteDO> dstRouteDOList = nodeRouteRepository.findByDstNodeIdIn(nodeIds);
        if (!CollectionUtils.isEmpty(dstRouteDOList)) {
            for (NodeRouteDO nodeRouteDO : dstRouteDOList) {
                String dstNodeId = nodeRouteDO.getDstNodeId();
                String srcNodeId = nodeRouteDO.getSrcNodeId();
                log.info("AutonomyNodeRouteUtil checkNodeRouteReady: srcNodeId: {}, dstNodeId: {}", srcNodeId, dstNodeId);
                boolean ready = nodeRouteManager.checkNodeRouteReady(srcNodeId, dstNodeId, dstNodeId);
                AutonomySourceNodeRouteInfo autonomySourceNodeRouteInfo = new AutonomySourceNodeRouteInfo();
                autonomySourceNodeRouteInfo.setSourceNodeId(srcNodeId);
                autonomySourceNodeRouteInfo.setSourceToDstIsAvailable(ready);
                if (autonomySelfDstNodeRouteInfoMap.containsKey(dstNodeId)) {
                    autonomySelfDstNodeRouteInfoMap.get(dstNodeId).add(autonomySourceNodeRouteInfo);
                } else {
                    List<AutonomySourceNodeRouteInfo> autonomySourceNodeRouteInfos = new ArrayList<>();
                    autonomySourceNodeRouteInfos.add(autonomySourceNodeRouteInfo);
                    autonomySelfDstNodeRouteInfoMap.put(dstNodeId, autonomySourceNodeRouteInfos);
                }
            }
        }
        return autonomySelfDstNodeRouteInfoMap;
    }

    @Autowired
    public void setNodeRouteRepository(NodeRouteRepository nodeRouteRepository) {
        AutonomyNodeRouteUtil.nodeRouteRepository = nodeRouteRepository;
    }

    @Autowired
    public void setNodeRepository(NodeRepository nodeRepository) {
        AutonomyNodeRouteUtil.nodeRepository = nodeRepository;
    }

    @Autowired
    public void setNodeRouteManager(NodeRouteManager nodeRouteManager) {
        AutonomyNodeRouteUtil.nodeRouteManager = nodeRouteManager;
    }

    @Getter
    @Setter
    public static class AutonomySourceNodeRouteInfo {
        private String sourceNodeId;
        private boolean sourceToDstIsAvailable;
    }
}
