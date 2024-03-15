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

import org.secretflow.secretpad.persistence.entity.NodeDO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Node base info view object
 *
 * @author yuexie
 * @date 2023-10-19
 */
@Builder
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NodeBaseInfoVO {

    /**
     * id
     */
    @Schema(description = "nodeId")
    private String nodeId;

    /**
     * nodeName
     */
    @Schema(description = "nodeName")
    private String nodeName;

    /**
     * netAddress
     */
    @Schema(description = "netAddress")
    private String netAddress;


    public static NodeBaseInfoVO from(NodeDO nodeDO) {
        return NodeBaseInfoVO.builder()
                .nodeId(nodeDO.getNodeId())
                .nodeName(nodeDO.getName())
                .netAddress(nodeDO.getNetAddress())
                .build();
    }

}
