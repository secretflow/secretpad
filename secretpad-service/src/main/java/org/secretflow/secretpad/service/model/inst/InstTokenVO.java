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

package org.secretflow.secretpad.service.model.inst;

import org.secretflow.secretpad.common.enums.NodeInstTokenStateEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * inst node
 */
@Builder
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InstTokenVO {
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
     * inst token
     */
    @Schema(description = "token")
    private String instToken;


    /**
     * ?? time or modify time
     */
    @Schema(description = "createTime")
    private String createTime;

    /**
     * inst token state
     */
    @Schema(description = "instTokenState")
    private NodeInstTokenStateEnum instTokenState;

}
