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
package org.secretflow.secretpad.service.model.node.p2p;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.service.constant.Constants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Create node request for p2p mode
 *
 * @author xujiening
 * @date 2023/12/01
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class P2pCreateNodeRequest {

    /**
     * Node name, the value cannot be empty and can be the same
     */
    @Schema(description = "node name")
    @Pattern(regexp = Constants.NODE_NAME_PATTERN, message = "node name illegal")
    private String name;

    /**
     * node feature indicates by bit, bit0 - mpc | bit1 - tee | bit2 mpc&tee
     */
    @NotNull
    @Min(0)
    @Max(2)
    @Schema(description = "node feature indicates by bit, bit0 - mpc | bit1 - tee | bit2 mpc&tee")
    private Integer mode = DomainConstants.DomainModeEnum.mpc.code;
    /**
     * nodeId of master
     */
    @Schema(description = "masterNodeId")
    private String masterNodeId;
    /**
     * cert text
     */
    @NotBlank
    @Schema(description = "certText")
    private String certText;

    /**
     * nodeId of collaborative node
     */
    @NotBlank
    @Schema(description = "dstNodeId")
    private String dstNodeId;

    /**
     * net address of platform nodeId
     */
    @Schema(description = "srcNetAddress")
    @Pattern(regexp = Constants.IP_PREFIX_REG, message = "address needs to start with http or https")
    private String srcNetAddress;

    /**
     * the nodeId of initiator
     */
    @Schema(description = "srcNodeId")
    @NotBlank(message = "srcNodeId cannot be empty")
    private String srcNodeId;

    /**
     * net address of collaborative node
     */
    @NotBlank
    @Schema(description = "dstNetAddress")
    @Pattern(regexp = Constants.IP_PREFIX_REG, message = "address needs to start with http or https")
    @Pattern(regexp = Constants.IP_PORT_PATTERN, message = "address not support rule")
    private String dstNetAddress;

    /**
     * the dst node's instId
     */
    @Schema(description = "dstInstId")
    private String dstInstId;

    /**
     * the dst node's instName
     */
    @Schema(description = "dstInstName")
    private String dstInstName;
}
