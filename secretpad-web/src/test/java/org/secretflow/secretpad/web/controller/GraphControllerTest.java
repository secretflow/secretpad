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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.ProjectConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.graph.*;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Graph controller test
 *
 * @author yansi
 * @date 2023/7/24
 */
class GraphControllerTest extends ControllerTest {

    @MockBean
    private ProjectGraphRepository graphRepository;

    @MockBean
    private ProjectGraphNodeRepository graphNodeRepository;

    @MockBean
    private ProjectJobTaskRepository taskRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    private static final String NODE_DEF = """
            {
                    "attrPaths": [
                      "datatable_selected"
                    ],
                    "attrs": [
                      {
                        "s": "alice-table"
                      }
                    ],
                    "domain": "read_data",
                    "name": "datatable",
                    "version": "0.0.1"
             }
            """;

    private ProjectNodeDO buildProjectNodeDO() {
        return ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK(PROJECT_ID, "alice")).build();
    }

    //todo get uri path from mapping path
    @Test
    void listComponentI18n() throws Exception {
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "listComponentI18n"));
        });
    }

    @Test
    void listComponents() throws Exception {
        assertResponse(() -> {
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "listComponents"));
        });
    }

    @Test
    void createGraph() throws Exception {
        assertResponse(() -> {
            CreateGraphRequest createGraphRequest = FakerUtils.fake(CreateGraphRequest.class);
            createGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_CREATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "createGraph", CreateGraphRequest.class))
                    .content(JsonUtils.toJSONString(createGraphRequest));
        });
    }

    @Test
    void deleteGraph() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteGraphRequest deleteGraphRequest = FakerUtils.fake(DeleteGraphRequest.class);
            deleteGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_DELETE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "deleteGraph", DeleteGraphRequest.class))
                    .content(JsonUtils.toJSONString(deleteGraphRequest));
        });
    }

    @Test
    void listGraph() throws Exception {
        assertResponse(() -> {
            ListGraphRequest listGraphRequest = FakerUtils.fake(ListGraphRequest.class);
            listGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_LIST));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "listGraph", ListGraphRequest.class))
                    .content(JsonUtils.toJSONString(listGraphRequest));
        });
    }

    @Test
    void updateGraphMeta() throws Exception {
        assertResponseWithEmptyData(() -> {
            UpdateGraphMetaRequest updateGraphMetaRequest = FakerUtils.fake(UpdateGraphMetaRequest.class);
            updateGraphMetaRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_META_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(updateGraphMetaRequest.getProjectId(), updateGraphMetaRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "updateGraphMeta", UpdateGraphMetaRequest.class))
                    .content(JsonUtils.toJSONString(updateGraphMetaRequest));
        });
    }

    @Test
    void fullUpdateGraph() throws Exception {
        assertResponseWithEmptyData(() -> {
            FullUpdateGraphRequest fullUpdateGraphRequest = FakerUtils.fake(FullUpdateGraphRequest.class);
            fullUpdateGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(fullUpdateGraphRequest.getProjectId(), fullUpdateGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "fullUpdateGraph", FullUpdateGraphRequest.class))
                    .content(JsonUtils.toJSONString(fullUpdateGraphRequest));
        });
    }

    @Test
    void updateGraphNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            UpdateGraphNodeRequest updateGraphNodeRequest = FakerUtils.fake(UpdateGraphNodeRequest.class);
            updateGraphNodeRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_NODE_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            ProjectGraphNodeDO graphNodeDO = FakerUtils.fake(ProjectGraphNodeDO.class);
            Mockito.when(graphNodeRepository.findById(new ProjectGraphNodeDO.UPK(updateGraphNodeRequest.getProjectId(), updateGraphNodeRequest.getGraphId(), updateGraphNodeRequest.getNode().getGraphNodeId())))
                    .thenReturn(Optional.of(graphNodeDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "updateGraphNode", UpdateGraphNodeRequest.class))
                    .content(JsonUtils.toJSONString(updateGraphNodeRequest));
        });
    }

    @Test
    void startGraph() throws Exception {
        assertResponse(() -> {
            StartGraphRequest startGraphRequest = FakerUtils.fake(StartGraphRequest.class);
            startGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_START));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setEdges(null);
            Object nodeDef = JsonUtils.toJavaObject(NODE_DEF, Object.class);
            List<ProjectGraphNodeDO> nodes = projectGraphDO.getNodes();
            nodes.get(0).setUpk(new ProjectGraphNodeDO.UPK(startGraphRequest.getProjectId(), startGraphRequest.getGraphId(), startGraphRequest.getNodes().get(0)));
            nodes.get(0).setNodeDef(nodeDef);
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(startGraphRequest.getProjectId(), startGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            Mockito.when(projectRepository.findById(startGraphRequest.getProjectId()))
                    .thenReturn(Optional.of(ProjectDO.builder().computeMode(ProjectConstants.ComputeModeEnum.MPC.name()).build()));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "startGraph", StartGraphRequest.class))
                    .content(JsonUtils.toJSONString(startGraphRequest));
        });
    }

    @Test
    void listGraphNodeStatus() throws Exception {
        assertResponse(() -> {
            ListGraphNodeStatusRequest listGraphNodeStatusRequest = FakerUtils.fake(ListGraphNodeStatusRequest.class);
            listGraphNodeStatusRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_NODE_STATUS));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(listGraphNodeStatusRequest.getProjectId(), listGraphNodeStatusRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "listGraphNodeStatus", ListGraphNodeStatusRequest.class))
                    .content(JsonUtils.toJSONString(listGraphNodeStatusRequest));
        });
    }

    @Test
    void stopGraphNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            StopGraphNodeRequest stopGraphNodeRequest = FakerUtils.fake(StopGraphNodeRequest.class);
            stopGraphNodeRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_STOP));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "stopGraphNode", StopGraphNodeRequest.class))
                    .content(JsonUtils.toJSONString(stopGraphNodeRequest));
        });
    }

    @Test
    void getGraphDetail() throws Exception {
        assertResponse(() -> {
            GetGraphRequest getGraphRequest = FakerUtils.fake(GetGraphRequest.class);
            getGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_DETAIL));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(getGraphRequest.getProjectId(), getGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "getGraphDetail", GetGraphRequest.class))
                    .content(JsonUtils.toJSONString(getGraphRequest));
        });
    }

    @Test
    void getGraphNodeOutput() throws Exception {
        assertResponse(() -> {
            GraphNodeOutputRequest graphNodeOutputRequest = FakerUtils.fake(GraphNodeOutputRequest.class);
            graphNodeOutputRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_NODE_OUTPUT));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Object nodeDef = JsonUtils.toJavaObject(NODE_DEF, Object.class);
            ProjectTaskDO projectTaskDO = FakerUtils.fake(ProjectTaskDO.class);
            projectTaskDO.getGraphNode().setNodeDef(nodeDef);
            Mockito.when(taskRepository.findLatestTasks(graphNodeOutputRequest.getProjectId(), graphNodeOutputRequest.getGraphNodeId()))
                    .thenReturn(Optional.of(projectTaskDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "getGraphNodeOutput", GraphNodeOutputRequest.class))
                    .content(JsonUtils.toJSONString(graphNodeOutputRequest));
        });
    }

    @Test
    void getGraphNodeLogs() throws Exception {
        assertResponse(() -> {
            GraphNodeLogsRequest graphNodeLogsRequest = FakerUtils.fake(GraphNodeLogsRequest.class);
            graphNodeLogsRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_NODE_LOGS));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(taskRepository.findLatestTasks(graphNodeLogsRequest.getProjectId(), graphNodeLogsRequest.getGraphNodeId()))
                    .thenReturn(Optional.of(FakerUtils.fake(ProjectTaskDO.class)));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "getGraphNodeLogs", GraphNodeLogsRequest.class))
                    .content(JsonUtils.toJSONString(graphNodeLogsRequest));
        });
    }
}