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

package org.secretflow.secretpad.manager.integration.model;

import org.secretflow.secretpad.common.util.DateTimes;

import lombok.Builder;
import lombok.Data;
import org.secretflow.v1alpha1.kusciaapi.Domain;

import java.io.Serializable;

/**
 * NodeInstanceDTO
 *
 * @author yutu
 * @date 2023/08/07
 */
@Data
@Builder
public class NodeInstanceDTO implements Serializable {
    /**
     * domain name
     */
    private String name;
    /**
     * domain status  Ready,  NotReady
     */
    private String status;
    /**
     * Agent version
     */
    private String version;
    /**
     * lastHeartbeatTime，RFC3339（e.g. 2006-01-02T15:04:05Z）
     */
    private String lastHeartbeatTime;
    /**
     * lastTransitionTime，RFC3339（e.g. 2006-01-02T15:04:05Z）
     */
    private String lastTransitionTime;

    public static NodeInstanceDTO formDomainNodeStatus(Domain.NodeStatus nodeStatus) {
        return NodeInstanceDTO.builder().name(nodeStatus.getName()).status(nodeStatus.getStatus())
                .version(nodeStatus.getVersion())
                .lastHeartbeatTime(DateTimes.rfc3339ToGmt8(nodeStatus.getLastHeartbeatTime()))
                .lastTransitionTime(DateTimes.rfc3339ToGmt8(nodeStatus.getLastTransitionTime()))
                .build();
    }
}