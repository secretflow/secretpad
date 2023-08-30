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

import org.secretflow.secretpad.manager.integration.model.NodeTokenDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yutu
 * @date 2023/08/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeTokenVO implements Serializable {
    /**
     * token
     */
    private String token;
    /**
     * tokenStatus
     */
    private String tokenStatus;

    /**
     * lastTransitionTime
     */
    private String lastTransitionTime;

    public static NodeTokenVO fromDto(NodeTokenDTO nodeTokenDTO) {
        NodeTokenVO nodeTokenVO = new NodeTokenVO();
        nodeTokenVO.setToken(nodeTokenDTO.getToken());
        nodeTokenVO.setTokenStatus(nodeTokenDTO.getTokenStatus());
        nodeTokenVO.setLastTransitionTime(nodeTokenDTO.getLastTransitionTime());
        return nodeTokenVO;
    }
}