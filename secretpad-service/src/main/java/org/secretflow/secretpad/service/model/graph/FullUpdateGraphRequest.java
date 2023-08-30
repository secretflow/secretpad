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

package org.secretflow.secretpad.service.model.graph;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Full update graph request
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
public class FullUpdateGraphRequest {
    /**
     * Project id, it can not be blank
     */
    @NotBlank
    private String projectId;
    /**
     * Graph id, it can not be blank
     */
    @NotBlank
    private String graphId;
    /**
     * Graph node information list
     */
    private List<GraphNodeInfo> nodes;
    /**
     * Graph edge list
     */
    private List<GraphEdge> edges;
}
