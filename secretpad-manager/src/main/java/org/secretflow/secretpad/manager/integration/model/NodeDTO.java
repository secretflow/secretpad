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

package org.secretflow.secretpad.manager.integration.model;

import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.persistence.entity.NodeDO;

import lombok.*;

import java.util.List;

/**
 * Node data transfer object
 *
 * @author jiezi
 * @date 2023/05/16
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NodeDTO {

    /**
     * Node id
     */
    private String nodeId;

    /**
     * Node name
     */
    private String nodeName;

    /**
     * Node authorization
     */
    private String auth;

    /**
     * Node description
     */
    private String description;
    private String controlNodeId;
    private String masterNodeId;
    private String netAddress;
    private String token;
    private String type;
    private Integer mode;
    private String cert;
    private String certText;
    private String tokenStatus;
    private String nodeRole;
    private String nodeStatus;
    private String gmtCreate;
    private String gmtModified;
    private List<NodeInstanceDTO> nodeInstances;

    /**
     * Convert NodeDO to NodeDTO
     *
     * @param nodeDO nodeDO
     * @return NodeDTO
     */
    public static NodeDTO fromDo(NodeDO nodeDO) {
        if (nodeDO == null) {
            return new NodeDTO();
        }
        NodeDTO nodeDTO = new NodeDTO();
        nodeDTO.setNodeId(nodeDO.getNodeId());
        nodeDTO.setAuth(nodeDO.getAuth());
        nodeDTO.setDescription(nodeDO.getDescription());
        nodeDTO.setControlNodeId(nodeDO.getControlNodeId());
        nodeDTO.setNetAddress(nodeDO.getNetAddress());
        nodeDTO.setToken(nodeDO.getToken());
        nodeDTO.setType(nodeDO.getType());
        nodeDTO.setGmtCreate(DateTimes.toRfc3339(nodeDO.getGmtCreate()));
        nodeDTO.setGmtModified(DateTimes.toRfc3339(nodeDO.getGmtModified()));
        nodeDTO.setNodeName(nodeDO.getName());
        nodeDTO.setMode(nodeDO.getMode());
        nodeDTO.setMasterNodeId(nodeDO.getMasterNodeId());
        return nodeDTO;
    }

}
