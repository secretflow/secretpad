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

import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.service.model.node.NodeSimpleInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Graph node status view object
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
@NoArgsConstructor
public class GraphNodeStatusVO {
    /**
     * Graph node id
     */
    private String graphNodeId;
    /**
     * Graph node task id
     */
    private String taskId;

    /**
     * Graph node job id
     */
    private String jobId;
    /**
     * Graph node task status
     */
    private GraphNodeTaskStatus status;

    /**
     * task progress
     */
    private Float progress;

    /**
     * Graph node job parties
     */
    private List<NodeSimpleInfo> parties;
}
