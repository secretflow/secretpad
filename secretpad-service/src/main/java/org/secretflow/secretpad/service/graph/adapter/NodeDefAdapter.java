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

import org.secretflow.secretpad.common.constant.ComponentConstants;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import com.secretflow.spec.v1.ComponentDef;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.util.ObjectUtils;

/**
 * NodeDefAdapter
 *
 * @author yutu
 * @date 2023/11/30
 */
public interface NodeDefAdapter {
    /**
     * nodeDef adapter
     *
     * @param nodeDef       nodeDef
     * @param graphNodeInfo graphNodeInfo
     * @return JobTask
     */
    ProjectJob.JobTask adapter(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, ProjectJob.JobTask task);

    default ComponentDef buildIoReadData() {
        ComponentDef componentDef = ComponentService.SF_HIDE_COMPONENTS.get(ComponentConstants.IO_READ_DATA);
        if (ObjectUtils.isEmpty(componentDef)) {
            ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_READ_DATA, ComponentDef.newBuilder()
                    .setDomain("io")
                    .setName("read_data")
                    .setVersion("0.0.1").build());
            componentDef = ComponentService.SF_HIDE_COMPONENTS.get(ComponentConstants.IO_READ_DATA);
        }
        return componentDef;
    }

    default ComponentDef buildIoWriteData() {
        ComponentDef componentDef = ComponentService.SF_HIDE_COMPONENTS.get(ComponentConstants.IO_WRITE_DATA);
        if (ObjectUtils.isEmpty(componentDef)) {
            ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_WRITE_DATA, ComponentDef.newBuilder()
                    .setDomain("io")
                    .setName("write_data")
                    .setVersion("0.0.1").build());
            componentDef = ComponentService.SF_HIDE_COMPONENTS.get(ComponentConstants.IO_WRITE_DATA);
        }
        return componentDef;
    }

    default ComponentDef buildIoIdentity() {
        ComponentDef componentDef = ComponentService.SF_HIDE_COMPONENTS.get(ComponentConstants.IO_IDENTITY);
        if (ObjectUtils.isEmpty(componentDef)) {
            ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_IDENTITY, ComponentDef.newBuilder()
                    .setDomain("io")
                    .setName("identity")
                    .setVersion("0.0.1").build());
            componentDef = ComponentService.SF_HIDE_COMPONENTS.get(ComponentConstants.IO_IDENTITY);
        }
        return componentDef;
    }
}