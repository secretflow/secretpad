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

import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Node route data transfer object
 *
 * @author : xiaonan.fhn
 * @date 2023/05/23
 */
@Builder
@Setter
@Getter
public class NodeRouteDTO {

    private Long routeId;

    /**
     * Node id of source
     */
    private String srcNodeId;

    /**
     * Node id of destination
     */
    private String dstNodeId;

    private String srcNetAddress;
    private String dstNetAddress;

    private String status;

    /**
     * Convert NodeRouteDO to NodeRouteDTO
     *
     * @param nodeRouteDO
     * @return NodeRouteDTO
     */
    public static NodeRouteDTO fromDo(NodeRouteDO nodeRouteDO) {
        return NodeRouteDTO.builder().routeId(nodeRouteDO.getId()).srcNodeId(nodeRouteDO.getSrcNodeId())
                .dstNodeId(nodeRouteDO.getDstNodeId()).srcNetAddress(nodeRouteDO.getSrcNetAddress())
                .dstNetAddress(nodeRouteDO.getDstNetAddress())
                .status(DomainRouterConstants.DomainRouterStatusEnum.Unknown.name()).build();
    }

}
