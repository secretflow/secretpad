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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Get node result detail request
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetNodeResultDetailRequest {

    /**
     * Node id
     */
    @Schema(description = "node id")
    String nodeId;

    /**
     * Domain data id
     */
    @Schema(description = "domain data id")
    String domainDataId;

    /**
     * Rules for filtering by data type, not filled when listing all outputs
     */
    @Schema(description = "data type，for filtering")
    String dataType;

    /**
     * Rules for producer filtering by data vendor, not filled when listing all outputs
     */
    @Schema(description = "project source")
    String dataVendor;

}
