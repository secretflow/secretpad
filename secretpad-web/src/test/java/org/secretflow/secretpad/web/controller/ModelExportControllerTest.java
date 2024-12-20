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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.CacheConstants;
import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.ModelExportErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.ModelExportDTO;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.model.PartyDataSource;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.model.export.ModelExportPackageRequest;
import org.secretflow.secretpad.service.model.model.export.ModelExportStatusRequest;
import org.secretflow.secretpad.service.model.model.export.ModelPartyConfig;
import org.secretflow.secretpad.service.model.model.export.ModelPartyPathRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

/**
 * @author yutu
 * @date 2024/02/19
 */
public class ModelExportControllerTest extends ControllerTest {
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
    @MockBean
    private ProjectJobTaskRepository taskRepository;
    @MockBean
    private AbstractJobManager jobManager;
    @MockBean
    private ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;
    @MockBean
    private ProjectGraphNodeRepository projectGraphNodeRepository;
    @Resource
    private CacheManager cacheManager;
    @MockBean
    private NodeRepository nodeRepository;
    @MockBean
    private GraphService graphService;
    @MockBean
    private ProjectGraphRepository graphRepository;
    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @Test
    public void pack() throws Exception {
        assertResponse(() -> {
            ProjectGraphDO projectGraphDO = buildProjectGraphDO();
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setTrainId("zxzyprqn-node-6");
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());
            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
            projectGraphDO.setNodes(buildProjectGraphNodeDOs());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()))).thenReturn(Optional.of(projectGraphDO));

            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void packNotParty() throws Exception {
        assertResponse(() -> {
            ProjectGraphDO projectGraphDO = buildProjectGraphDO();
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setTrainId("zxzyprqn-node-6");
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice1").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
            projectGraphDO.setNodes(buildProjectGraphNodeDOs());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()))).thenReturn(Optional.of(projectGraphDO));

            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void packComponentUnsupported() throws Exception {
        assertErrorCode(() -> {
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, SystemErrorCode.VALIDATION_ERROR);
    }

    @Test
    public void packGraphNotSuccess() throws Exception {
        assertErrorCode(() -> {
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ModelExportErrorCode.MODEL_EXPORT_FAILED);
    }

    @Test
    public void packGraphHasNoTask() throws Exception {
        assertErrorCode(() -> {
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
    }

    @Test
    public void packGraphReadTableNotFound() throws Exception {
        assertErrorCode(() -> {
            ProjectGraphDO projectGraphDO = buildProjectGraphDO();
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setTrainId("zxzyprqn-node-6");
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
            projectGraphDO.setNodes(buildProjectGraphNodeDOs());
            Mockito.when(graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()))).thenReturn(Optional.of(projectGraphDO));
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ModelExportErrorCode.MODEL_EXPORT_FAILED);
    }

    @Test
    public void statusSuccess() throws Exception {
        assertResponse(() -> {
            ModelExportStatusRequest request = FakerUtils.fake(ModelExportStatusRequest.class);
            Cache cache1 = cacheManager.getCache(CacheConstants.MODEL_EXPORT_CACHE);
            cache1.put(request.getJobId(), JsonUtils.toJSONString(buildSuccessModelExportDTO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "status", ModelExportStatusRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void statusFailed() throws Exception {
        assertResponse(() -> {
            ModelExportStatusRequest request = FakerUtils.fake(ModelExportStatusRequest.class);
            Cache cache1 = cacheManager.getCache(CacheConstants.MODEL_EXPORT_CACHE);
            cache1.put(request.getJobId(), JsonUtils.toJSONString(buildFailedModelExportDTO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "status", ModelExportStatusRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void statusJobNotFound() throws Exception {
        assertErrorCode(() -> {
            ModelExportStatusRequest request = FakerUtils.fake(ModelExportStatusRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "status", ModelExportStatusRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ModelExportErrorCode.MODEL_EXPORT_FAILED);
    }

    @Test
    public void modelPartyPath() throws Exception {
        assertResponse(() -> {
            ModelPartyPathRequest request = FakerUtils.fake(ModelPartyPathRequest.class);
            Mockito.when(kusciaGrpcClientAdapter.listDomainDataSource(Mockito.any())).thenReturn(buildBatchQueryDomainResponse(0));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO()));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(buildNodeDO());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "modelPartyPath", ModelPartyPathRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void modelPartyPathNoTask() throws Exception {
        assertErrorCode(() -> {
            ModelPartyPathRequest request = FakerUtils.fake(ModelPartyPathRequest.class);
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(buildNodeDO());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "modelPartyPath", ModelPartyPathRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
    }

    @Test
    public void modelPartyPathTaskNoParties() throws Exception {
        assertErrorCode(() -> {
            ModelPartyPathRequest request = FakerUtils.fake(ModelPartyPathRequest.class);
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDONoParties()));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(buildNodeDO());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "modelPartyPath", ModelPartyPathRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ModelExportErrorCode.MODEL_EXPORT_FAILED);
    }

    @Test
    public void modelPartyPathNodeNotFound() throws Exception {
        assertErrorCode(() -> {
            ModelPartyPathRequest request = FakerUtils.fake(ModelPartyPathRequest.class);
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDONoParties()));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "modelPartyPath", ModelPartyPathRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ModelExportErrorCode.MODEL_EXPORT_FAILED);
    }


    private ProjectTaskDO buildProjectTaskDO() {
        return ProjectTaskDO.builder()
                .upk(ProjectTaskDO.UPK.builder().projectId(UUIDUtils.random(4)).taskId(UUIDUtils.random(4)).jobId(UUIDUtils.random(4)).build())
                .parties(List.of("alice", "bob"))
                .build();
    }

    private ProjectJobDO buildProjectJobDO() {
        return ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK(UUIDUtils.random(4), UUIDUtils.random(4)))
                .status(GraphJobStatus.SUCCEED)
                .build();
    }

    private ProjectTaskDO buildProjectTaskDONoParties() {
        return ProjectTaskDO.builder()
                .upk(ProjectTaskDO.UPK.builder().projectId(UUIDUtils.random(4)).taskId(UUIDUtils.random(4)).jobId(UUIDUtils.random(4)).build())
                .build();
    }

    private ProjectGraphNodeKusciaParamsDO buildProjectGraphNodeKusciaParamsDO() {
        String param = """
                {
                  "domain": "io",
                  "name": "write_data",
                  "version": "0.0.1",
                  "attr_paths": ["write_data", "write_data_type"],
                  "attrs": [{
                    "s": "{\\"modelHash\\":\\"f4cfa281-dbb8-4b60-bd78-97d3fa5674fb\\",\\"featureWeights\\":[{\\"featureName\\":\\"age\\",\\"party\\":\\"alice\\",\\"featureWeight\\":0},{\\"featureName\\":\\"balance\\",\\"party\\":\\"alice\\",\\"featureWeight\\":0.088554493}],\\"bias\\":-0.043597333}"
                  }, {
                    "s": "sf.model.ss_glm"
                  }]
                }
                """;
        return ProjectGraphNodeKusciaParamsDO.builder()
                .upk(ProjectGraphNodeKusciaParamsDO.UPK.builder().projectId(UUIDUtils.random(4)).graphNodeId(UUIDUtils.random(4)).graphNodeId(UUIDUtils.random(4)).build())
                .inputs(JsonUtils.toJSONString(List.of("1")))
                .outputs(JsonUtils.toJSONString(List.of("1")))
                .nodeEvalParam(param)
                .build();
    }

    private List<ProjectGraphNodeDO> buildProjectGraphNodeDOs() {
        Object nodeDef = JsonUtils.toJavaObject(NODE_DEF, Object.class);
        return Lists.newArrayList(ProjectGraphNodeDO.builder().upk(new ProjectGraphNodeDO.UPK("", "", "zxzyprqn-node-2")).nodeDef(nodeDef).build(), ProjectGraphNodeDO.builder().upk(new ProjectGraphNodeDO.UPK("", "", "zxzyprqn-node-1")).nodeDef(nodeDef).build());
    }

    private ModelExportDTO buildSuccessModelExportDTO() {
        return ModelExportDTO.builder()
                .projectId(UUIDUtils.random(4))
                .initiator("alice")
                .graphId(UUIDUtils.random(4))
                .modelId(UUIDUtils.random(4))
                .modelName("test")
                .modelDesc("test")
                .sampleTables("{'alice':'alice', 'bob':'bob'}")
                .modelList(List.of("1"))
                .jobId(UUIDUtils.random(4))
                .taskId(UUIDUtils.random(4))
                .trainId(UUIDUtils.random(4))
                .status(GraphNodeTaskStatus.SUCCEED)
                .graphDetail(JsonUtils.toJSONString(GraphDetailVO.builder().graphId("test").build()))
                .modelReportId("111")
                .partyDataSources(Lists.newArrayList(PartyDataSource.builder().partyId("alice").datasource("alice").build()))
                .build();
    }

    private ModelExportDTO buildFailedModelExportDTO() {
        return ModelExportDTO.builder()
                .modelId(UUIDUtils.random(4))
                .status(GraphNodeTaskStatus.FAILED)
                .build();
    }

    private NodeDO buildNodeDO() {
        return NodeDO.builder()
                .nodeId(UUIDUtils.random(4))
                .name("test")
                .build();
    }

    private ProjectGraphDO buildProjectGraphDO() throws Exception {
        ProjectGraphDO projectGraphDO = FakerUtils.fake(ProjectGraphDO.class);
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
        return projectGraphDO;
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
}