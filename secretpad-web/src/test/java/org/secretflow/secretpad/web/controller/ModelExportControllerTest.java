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
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.ModelExportErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.ModelExportDTO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeKusciaParamsDO;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectGraphNodeKusciaParamsRepository;
import org.secretflow.secretpad.persistence.repository.ProjectGraphNodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskRepository;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.model.export.ModelExportPackageRequest;
import org.secretflow.secretpad.service.model.model.export.ModelExportStatusRequest;
import org.secretflow.secretpad.service.model.model.export.ModelPartyConfig;
import org.secretflow.secretpad.service.model.model.export.ModelPartyPathRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

    @Test
    public void pack() throws Exception {
        assertResponse(() -> {
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());
            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
            return MockMvcRequestBuilders.post(getMappingUrl(ModelExportController.class, "pack", ModelExportPackageRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void packNotParty() throws Exception {
        assertResponse(() -> {
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice1").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectGraphNodeDOs());
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
            ModelExportPackageRequest request = FakerUtils.fake(ModelExportPackageRequest.class);
            request.setModelPartyConfig(List.of(ModelPartyConfig.builder().modelParty("alice").modelDataName("1").modelDataSource("/home").build()));
            request.getModelComponent().forEach(component -> component.setDomain("ml.train"));
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
            Mockito.doNothing().when(jobManager).createJob(Mockito.any());
            Mockito.when(graphService.getGraphDetail(Mockito.any())).thenReturn(GraphDetailVO.builder().build());

            Mockito.when(projectGraphNodeKusciaParamsRepository.findByUpk(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectGraphNodeKusciaParamsDO()));
            Mockito.when(projectGraphNodeRepository.findReadTableByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
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
            Mockito.when(taskRepository.findLatestTasks(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(buildProjectTaskDO()));
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

    private ProjectTaskDO buildProjectTaskDONoParties() {
        return ProjectTaskDO.builder()
                .upk(ProjectTaskDO.UPK.builder().projectId(UUIDUtils.random(4)).taskId(UUIDUtils.random(4)).jobId(UUIDUtils.random(4)).build())
                .build();
    }

    private ProjectGraphNodeKusciaParamsDO buildProjectGraphNodeKusciaParamsDO() {
        return ProjectGraphNodeKusciaParamsDO.builder()
                .upk(ProjectGraphNodeKusciaParamsDO.UPK.builder().projectId(UUIDUtils.random(4)).graphNodeId(UUIDUtils.random(4)).graphNodeId(UUIDUtils.random(4)).build())
                .inputs(JsonUtils.toJSONString(List.of("1")))
                .outputs(JsonUtils.toJSONString(List.of("1")))
                .nodeEvalParam("xxx")
                .build();
    }

    private List<ProjectGraphNodeDO> buildProjectGraphNodeDOs() {
        Object nodeDef = JsonUtils.toJavaObject(NODE_DEF, Object.class);
        return List.of(ProjectGraphNodeDO.builder().nodeDef(nodeDef).build());
    }

    private ModelExportDTO buildSuccessModelExportDTO() {
        return ModelExportDTO.builder()
                .projectId(UUIDUtils.random(4))
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
}