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

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.service.constant.Constants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * Create node request
 *
 * @author : xiaonan.fhn
 * @date 2023/5/15
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateNodeRequest {

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
}
