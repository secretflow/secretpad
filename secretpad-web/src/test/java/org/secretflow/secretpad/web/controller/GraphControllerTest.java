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

import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;
import org.secretflow.secretpad.common.constant.ProjectConstants;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.model.graph.*;
import org.secretflow.secretpad.web.utils.FakerUtils;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
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

    @MockBean
    private DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub domainDataSourceServiceBlockingStub;

    @Resource
    private ProjectGraphDomainDatasourceRepository projectGraphDomainDatasourceRepository;

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
            Mockito.when(projectNodeRepository.findByProjectId(Mockito.any())).thenReturn(buildFindByProjectId(createGraphRequest.getProjectId()));
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
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(deleteGraphRequest.getProjectId(), deleteGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
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
            fullUpdateGraphRequest.setDataSourceConfig(List.of(FullUpdateGraphRequest.GraphDataSourceConfig.builder().nodeId("alice")
                    .dataSourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                    .build()));
            projectGraphDomainDatasourceRepository.save(ProjectGraphDomainDatasourceDO.builder()
                    .upk(new ProjectGraphDomainDatasourceDO.UPK(fullUpdateGraphRequest.getProjectId(), fullUpdateGraphRequest.getGraphId(), "alice"))
                    .dataSourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                    .dataSourceName(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                    .editEnable(Boolean.TRUE)
                    .build());

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_UPDATE));
            Mockito.when(projectNodeRepository.findByProjectId(Mockito.any())).thenReturn(buildFindByProjectId(fullUpdateGraphRequest.getProjectId()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(domainDataSourceServiceBlockingStub.listDomainDataSource(Mockito.any())).thenReturn(buildBatchQueryDomainResponse(0));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(fullUpdateGraphRequest.getProjectId(), fullUpdateGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "fullUpdateGraph", FullUpdateGraphRequest.class))
                    .content(JsonUtils.toJSONString(fullUpdateGraphRequest));
        });
    }

    @Test
    void fullUpdateGraphByNotControlNode() throws Exception {
        assertErrorCode(() -> {
            FullUpdateGraphRequest fullUpdateGraphRequest = FakerUtils.fake(FullUpdateGraphRequest.class);
            fullUpdateGraphRequest.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(domainDataSourceServiceBlockingStub.listDomainDataSource(Mockito.any())).thenReturn(buildBatchQueryDomainResponse(0));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(fullUpdateGraphRequest.getProjectId(), fullUpdateGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "fullUpdateGraph", FullUpdateGraphRequest.class))
                    .content(JsonUtils.toJSONString(fullUpdateGraphRequest));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    void fullUpdateGraphByDataSourceIdNotSupport() throws Exception {
        assertErrorCode(() -> {
            FullUpdateGraphRequest fullUpdateGraphRequest = FakerUtils.fake(FullUpdateGraphRequest.class);
            fullUpdateGraphRequest.setProjectId(PROJECT_ID);
            fullUpdateGraphRequest.setDataSourceConfig(List.of(FullUpdateGraphRequest.GraphDataSourceConfig.builder().nodeId("alice1").dataSourceId("not-support").build()));

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(domainDataSourceServiceBlockingStub.listDomainDataSource(Mockito.any())).thenReturn(buildBatchQueryDomainResponse(0));
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(fullUpdateGraphRequest.getProjectId(), fullUpdateGraphRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "fullUpdateGraph", FullUpdateGraphRequest.class))
                    .content(JsonUtils.toJSONString(fullUpdateGraphRequest));
        }, SystemErrorCode.VALIDATION_ERROR);
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
            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(updateGraphNodeRequest.getProjectId(), updateGraphNodeRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
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
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());
            String edges = "[\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-1-output-0__zxzyprqn-node-3-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-1\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-1-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-3\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-3-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-2-output-0__zxzyprqn-node-3-input-1\",\n" +
                    "    \"source\": \"zxzyprqn-node-2\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-2-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-3\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-3-input-1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-3-output-0__zxzyprqn-node-4-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-3\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-3-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-4\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-4-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-3-output-0__zxzyprqn-node-5-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-3\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-3-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-5\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-5-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-5-output-0__zxzyprqn-node-6-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-5\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-5-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-6\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-6-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-5-output-0__zxzyprqn-node-7-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-5\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-5-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-7\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-7-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-6-output-0__zxzyprqn-node-7-input-1\",\n" +
                    "    \"source\": \"zxzyprqn-node-6\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-6-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-7\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-7-input-1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-5-output-1__zxzyprqn-node-8-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-5\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-5-output-1\",\n" +
                    "    \"target\": \"zxzyprqn-node-8\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-8-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-6-output-0__zxzyprqn-node-8-input-1\",\n" +
                    "    \"source\": \"zxzyprqn-node-6\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-6-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-8\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-8-input-1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-7-output-0__zxzyprqn-node-9-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-7\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-7-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-9\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-9-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-7-output-0__zxzyprqn-node-10-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-7\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-7-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-10\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-10-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-7-output-0__zxzyprqn-node-11-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-7\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-7-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-11\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-11-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-7-output-0__zxzyprqn-node-12-input-1\",\n" +
                    "    \"source\": \"zxzyprqn-node-7\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-7-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-12\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-12-input-1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-11-output-0__zxzyprqn-node-12-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-11\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-11-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-12\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-12-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-11-output-0__zxzyprqn-node-13-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-11\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-11-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-13\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-13-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-8-output-0__zxzyprqn-node-13-input-1\",\n" +
                    "    \"source\": \"zxzyprqn-node-8\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-8-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-13\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-13-input-1\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-13-output-0__zxzyprqn-node-15-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-13\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-13-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-15\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-15-input-0\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"edgeId\": \"zxzyprqn-node-13-output-0__zxzyprqn-node-14-input-0\",\n" +
                    "    \"source\": \"zxzyprqn-node-13\",\n" +
                    "    \"sourceAnchor\": \"zxzyprqn-node-13-output-0\",\n" +
                    "    \"target\": \"zxzyprqn-node-14\",\n" +
                    "    \"targetAnchor\": \"zxzyprqn-node-14-input-0\"\n" +
                    "  }\n" +
                    "]";

            projectGraphDO.setEdges(JsonUtils.toJavaList(edges, GraphEdgeDO.class));
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

            ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
            projectGraphDO.setOwnerId(UserContext.getUser().getOwnerId());

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_STOP));
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(stopGraphNodeRequest.getProjectId(), stopGraphNodeRequest.getGraphId())))
                    .thenReturn(Optional.of(projectGraphDO));
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
            Mockito.when(projectNodeRepository.findByProjectId(Mockito.any())).thenReturn(buildFindByProjectId(getGraphRequest.getProjectId()));
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
            ProjectTaskDO projectTaskDO = new ProjectTaskDO();
            ProjectGraphNodeDO graphNode = new ProjectGraphNodeDO();
            graphNode.setCodeName("read_data/datatable");
            projectTaskDO.setGraphNode(graphNode);
            projectTaskDO.setUpk(new ProjectTaskDO.UPK("0dasda", "1312fad", "123131"));
            Mockito.when(taskRepository.findLatestTasks(graphNodeLogsRequest.getProjectId(), graphNodeLogsRequest.getGraphNodeId()))
                    .thenReturn(Optional.of(projectTaskDO));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "getGraphNodeLogs", GraphNodeLogsRequest.class))
                    .content(JsonUtils.toJSONString(graphNodeLogsRequest));
        });
    }

    @Test
    public void graphNodeMaxIndexRefresh() throws Exception {
        assertResponse(() -> {
            GraphNodeMaxIndexRefreshRequest request = new GraphNodeMaxIndexRefreshRequest();
            request.setGraphId("graphId");
            request.setProjectId(PROJECT_ID);
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.GRAPH_NODE_UPDATE));
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(PROJECT_ID, "graphId"))).thenReturn(Optional.of(FakerUtils.fake(ProjectGraphDO.class)));
            return MockMvcRequestBuilders.post(getMappingUrl(GraphController.class, "graphNodeMaxIndexRefresh", GraphNodeMaxIndexRefreshRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    private Domaindatasource.ListDomainDataSourceResponse buildBatchQueryDomainResponse(Integer code) {
        return Domaindatasource.ListDomainDataSourceResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build())
                .setData(Domaindatasource.DomainDataSourceList.newBuilder().addDatasourceList(Domaindatasource.DomainDataSource.newBuilder()
                        .setName(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                        .setDatasourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                        .setType("localfs")
                        .build()))
                .build();
    }

    private List<ProjectNodeDO> buildFindByProjectId(String projectId) {
        return List.of(ProjectNodeDO.builder()
                .upk(new ProjectNodeDO.UPK(projectId, "alice"))
                .build());
    }
}