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
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.persistence.entity.ProjectReadDataDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.repository.ProjectReadDtaRepository;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.graph.adapter.impl.ModelParamModificationsAdapter;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import com.secretflow.spec.v1.ComponentDef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.proto.pipeline.Pipeline;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ModelParamModificationsAdapterTest
 *
 * @author lufeng
 * @date 2023/4/24
 */
@ExtendWith(MockitoExtension.class)
public class ModelParamModificationsAdapterTest {

    private static final String NODE_DEF_EMPTY = """
            {
                    "attrPaths": [
                      "models"
                    ],
                    "attrs": [
                    ],
                    "domain": "feature",
                    "name": "model_param_modifications",
                    "version": "0.0.1"
             }
            """;
    private static final String NODE_DEF = """
            {
                    "attrPaths": [
                      "models"
                    ],
                    "attrs": [
                      {
                        "custom_value": {
				"featureWeights": [{
					"featureName": "age",
					"party": "lf",
					"featureWeight": 0.028514989
				}],
				"bias": -0.004350494,
				"modelHash": "29f5074b-4328-49fa-b1d4-c9566f031457"

                             },
                        "custom_protobuf_cls": "",
                        "s": "{
				'featureWeights': [{
					'featureName': 'age',
					'party': 'lf',
					'featureWeight': 0.028514989
				}],
				'bias': -0.004350494,
				'modelHash': '29f5074b-4328-49fa-b1d4-c9566f031457'

                             }"
                      }
                    ],
                    "domain": "feature",
                    "name": "model_param_modifications",
                    "version": "0.0.1"
             }
            """;
    @InjectMocks
    private ModelParamModificationsAdapter modelParamModificationsAdapter;
    @Mock
    private ProjectReadDtaRepository readDtaRepository;

    @Test
    void adapterWriteTest() {
        Object nodeDef = JsonUtils.toJavaObject(NODE_DEF, Object.class);
        Pipeline.NodeDef pipelineNodeDef;
        ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_READ_DATA, ComponentDef.newBuilder()
                .setDomain("io")
                .setName("read_data")
                .setVersion("0.0.1").build());
        ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_WRITE_DATA, ComponentDef.newBuilder()
                .setDomain("io")
                .setName("write_data")
                .setVersion("0.0.1").build());
        when(readDtaRepository.findByHashAndGrapNodeId(anyString(), anyString()))
                .thenReturn(buildProjectNodeDO());

        if (nodeDef instanceof Pipeline.NodeDef) {
            pipelineNodeDef = (Pipeline.NodeDef) nodeDef;
        } else {
            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            pipelineNodeDef = (Pipeline.NodeDef) ProtoUtils.fromObject(nodeDef, nodeDefBuilder);
        }
        GraphNodeInfo g = GraphNodeInfo.builder()
                .codeName("5057B9993D")
                .graphNodeId("B67E3F99E1")
                .label("102D48C3A5")
                .x(11744015)
                .y(17203591)
                .inputs(List.of("wgoi-zxzyprqn-node-1-input-0"))
                .outputs(List.of("wgoi-zxzyprqn-node-1-input-0", "wgoi-zxzyprqn-node-1-input-1"))
                .nodeDef(nodeDef).build();
        ProjectJob.JobTask jobTask = ProjectJob.JobTask.builder()
                .taskId("wgoi-B67E3F99E1")
                .status(GraphNodeTaskStatus.INITIALIZED)
                .dependencies(List.of("wgoi-B67E3F99E1"))
                .node(g).build();
        modelParamModificationsAdapter.adapter(pipelineNodeDef, g, jobTask);
    }

    @Test
    void adapterIdentityTest() {
        Object nodeDef = JsonUtils.toJavaObject(NODE_DEF_EMPTY, Object.class);
        Pipeline.NodeDef pipelineNodeDef;
        ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_READ_DATA, ComponentDef.newBuilder()
                .setDomain("io")
                .setName("read_data")
                .setVersion("0.0.1").build());
        ComponentService.SF_HIDE_COMPONENTS.put(ComponentConstants.IO_IDENTITY, ComponentDef.newBuilder()
                .setDomain("io")
                .setName("identity")
                .setVersion("0.0.1").build());
        if (nodeDef instanceof Pipeline.NodeDef) {
            pipelineNodeDef = (Pipeline.NodeDef) nodeDef;
        } else {
            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            pipelineNodeDef = (Pipeline.NodeDef) ProtoUtils.fromObject(nodeDef, nodeDefBuilder);
        }
        GraphNodeInfo g = GraphNodeInfo.builder()
                .codeName("5057B9993D")
                .graphNodeId("B67E3F99E1")
                .label("102D48C3A5")
                .x(11744015)
                .y(17203591)
                .inputs(List.of("wgoi-zxzyprqn-node-1-input-0"))
                .outputs(List.of("wgoi-zxzyprqn-node-1-input-0", "wgoi-zxzyprqn-node-1-input-1"))
                .nodeDef(nodeDef).build();
        ProjectJob.JobTask jobTask = ProjectJob.JobTask.builder()
                .taskId("wgoi-B67E3F99E1")
                .status(GraphNodeTaskStatus.INITIALIZED)
                .dependencies(List.of("wgoi-B67E3F99E1"))
                .node(g).build();
        modelParamModificationsAdapter.adapter(pipelineNodeDef, g, jobTask);
    }

    private List<ProjectReadDataDO> buildProjectNodeDO() {
        ProjectReadDataDO readDataDO = ProjectReadDataDO.builder()
                .grapNodeId("B67E3F99E1")
                .upk(new ProjectReadDataDO.UPK("wgoi-zxzyprqn-node-1-input-0", "wgoi-zxzyprqn-node-1-input-0"))
                .content("123").build();
        return Arrays.asList(readDataDO);
    }

}