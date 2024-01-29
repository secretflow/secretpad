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

package org.secretflow.secretpad.service.graph.adapter;

import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * @author yutu
 * @date 2023/11/30
 */
@Slf4j
@Component
public class NodeDefAdapterFactory {

    @Resource
    private Map<String, NodeDefAdapter> nodeDefAdapterMap;

    public ProjectJob.JobTask process(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, ProjectJob.JobTask task) {
        log.info("- nodeDefAdapter nodeDef : {}", nodeDef);
        NodeDefAdapter nodeDefAdapter = nodeDefAdapterMap.get(nodeDef.getName());
        return ObjectUtils.isEmpty(nodeDefAdapter) ? null : nodeDefAdapter.adapter(nodeDef, graphNodeInfo, task);
    }
}