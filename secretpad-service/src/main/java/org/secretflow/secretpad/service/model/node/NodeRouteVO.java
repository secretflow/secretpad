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

package org.secretflow.secretpad.service.model.node;

import org.secretflow.secretpad.manager.integration.model.NodeRouteDTO;

import lombok.*;

/**
 * Node route view object
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeRouteVO {
    /**
     * The node id of source
     */
    private Long routeId;
    /**
     * srcNodeId
     */
    private String srcNodeId;
    /**
     * dstNodeId
     */
    private String dstNodeId;

    /**
     * srcNetAddress
     */
    private String srcNetAddress;
    /**
     * dstNetAddress
     */
    private String dstNetAddress;

    /**
     * route status  Pending,  Succeeded,  Failed,  Unknown
     */
    private String status;

    public NodeRouteVO(String srcNodeId, String dstNodeId) {
        this.srcNodeId = srcNodeId;
        this.dstNodeId = dstNodeId;
    }

    public static NodeRouteVO fromDto(NodeRouteDTO nodeRouteDTO) {
        NodeRouteVO nodeRouteVO = new NodeRouteVO();
        nodeRouteVO.setRouteId(nodeRouteDTO.getRouteId());
        nodeRouteVO.setSrcNodeId(nodeRouteDTO.getSrcNodeId());
        nodeRouteVO.setDstNodeId(nodeRouteDTO.getDstNodeId());
        nodeRouteVO.setSrcNetAddress(nodeRouteDTO.getSrcNetAddress());
        nodeRouteVO.setDstNetAddress(nodeRouteDTO.getDstNetAddress());
        nodeRouteVO.setStatus(nodeRouteDTO.getStatus());
        return nodeRouteVO;
    }

}
