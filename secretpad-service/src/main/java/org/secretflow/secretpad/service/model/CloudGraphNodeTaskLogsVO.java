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

package org.secretflow.secretpad.service.model;

import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.service.model.graph.GraphNodeTaskLogsVO;
import org.secretflow.secretpad.service.model.node.NodeSimpleInfo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/04/18
 */
@Getter
@Setter
public class CloudGraphNodeTaskLogsVO extends GraphNodeTaskLogsVO {

    private Boolean config;

    private List<NodeSimpleInfo> nodeParties;

    public CloudGraphNodeTaskLogsVO(GraphNodeTaskStatus status, List<String> logs, Boolean config) {
        super(status, logs);
        this.config = config;
    }

    public CloudGraphNodeTaskLogsVO(GraphNodeTaskStatus status, List<String> logs, Boolean config, List<NodeSimpleInfo> nodeParties) {
        super(status, logs);
        this.config = config;
        this.nodeParties = nodeParties;
    }

    public static CloudGraphNodeTaskLogsVO buildUnReadyResult() {
        return new CloudGraphNodeTaskLogsVO(null, null, false);
    }

    public static CloudGraphNodeTaskLogsVO buildReadyResult() {
        return new CloudGraphNodeTaskLogsVO(null, null, true);
    }

    public static CloudGraphNodeTaskLogsVO buildQueryNodePartiesResult(List<NodeSimpleInfo> nodeParties) {
        return new CloudGraphNodeTaskLogsVO(null, null, true, nodeParties);
    }

}
