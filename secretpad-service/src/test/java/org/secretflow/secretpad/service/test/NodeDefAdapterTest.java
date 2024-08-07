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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.common.constant.ComponentConstants;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.graph.adapter.NodeDefAdapter;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import org.junit.jupiter.api.Test;
import org.secretflow.proto.pipeline.Pipeline;

/**
 * @author yutu
 * @date 2024/07/09
 */
public class NodeDefAdapterTest {

    @Test
    public void test() {
        NodeDefAdapter nodeDefAdapter = new NodeDefAdapter() {
            @Override
            public ProjectJob.JobTask adapter(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, ProjectJob.JobTask task) {
                return null;
            }
        };
        ComponentService.SF_HIDE_COMPONENTS.remove(ComponentConstants.IO_READ_DATA);
        ComponentService.SF_HIDE_COMPONENTS.remove(ComponentConstants.IO_WRITE_DATA);
        ComponentService.SF_HIDE_COMPONENTS.remove(ComponentConstants.IO_IDENTITY);
        nodeDefAdapter.buildIoWriteData();
        nodeDefAdapter.buildIoReadData();
        nodeDefAdapter.buildIoIdentity();
    }
}