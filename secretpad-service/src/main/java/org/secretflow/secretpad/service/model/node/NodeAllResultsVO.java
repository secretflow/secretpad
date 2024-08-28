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

package org.secretflow.secretpad.service.model.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Node results view object with node id and node name
 *
 * @author : guanxi
 * @date 2024/07/14
 */
@Builder
@Setter
@Getter
@ToString
public class NodeAllResultsVO {

    @Schema(description = "node results view object")
    private NodeResultsVO nodeResultsVO;

    /**
     * Node id the result belongs to
     */
    @Schema(description = "node id")
    private String nodeId;

    /**
     * Node name the result belongs to
     */
    @Schema(description = "node name")
    private String nodeName;

}
