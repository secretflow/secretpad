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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.proto.pipeline.Pipeline;

import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.InstErrorCode;
import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.InstDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectResultDO;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.repository.InstRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectDatatableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectGraphRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.ProjectResultRepository;
import org.secretflow.secretpad.service.model.project.AddInstToProjectRequest;
import org.secretflow.secretpad.service.model.project.AddNodeToProjectRequest;
import org.secretflow.secretpad.service.model.project.AddProjectDatatableRequest;
import org.secretflow.secretpad.service.model.project.CreateProjectRequest;
import org.secretflow.secretpad.service.model.project.DeleteProjectDatatableRequest;
import org.secretflow.secretpad.service.model.project.GetProjectDatatableRequest;
import org.secretflow.secretpad.service.model.project.GetProjectJobRequest;
import org.secretflow.secretpad.service.model.project.GetProjectJobTaskLogRequest;
import org.secretflow.secretpad.service.model.project.GetProjectJobTaskOutputRequest;
import org.secretflow.secretpad.service.model.project.GetProjectRequest;
import org.secretflow.secretpad.service.model.project.ListProjectJobRequest;
import org.secretflow.secretpad.service.model.project.StopProjectJobTaskRequest;
import org.secretflow.secretpad.service.model.project.TableColumnConfigParam;
import org.secretflow.secretpad.service.model.project.UpdateProjectRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

/**
 * Project controller test
 *
 * @author yansi
 * @date 2023/7/24
 */
class ProjectControllerTest extends ControllerTest {

    private static final String GRAPH_ID = "graphagdasvacaghyhbvscvyjnba";
    private static final String TASK_ID = "task-dabgvasfasdasdas";
    private static final String JOB_ID = "op-psiv3-dabgvasfasdasdas";

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @MockBean
    private InstRepository instRepository;

    @MockBean
    private ProjectDatatableRepository projectDatatableRepository;

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    @MockBean
    private ProjectGraphRepository graphRepository;

    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private ProjectResultRepository projectResultRepository;

    @MockBean
    private DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub;

    @MockBean
    private JobServiceGrpc.JobServiceBlockingStub jobStub;

    private ProjectDO buildProjectDO() {
        return ProjectDO.builder().projectId(PROJECT_ID).build();
    }

    private InstDO buildInstDO() {
        return InstDO.builder().build();
    }

    private NodeDO buildNodeDO() {
        return NodeDO.builder().nodeId("alice").name("alice").description("alice").auth("alice").type("mpc").build();
    }

    private ProjectNodeDO buildProjectNodeDO() {
        return ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK(PROJECT_ID, "alice")).build();
    }

    private ProjectDatatableDO buildProjectDatatableDO() {
        ProjectDatatableDO.UPK upk = new ProjectDatatableDO.UPK();
        upk.setDatatableId("alice-ref1");
        upk.setNodeId("alice");
        upk.setProjectId(PROJECT_ID);
        List<ProjectDatatableDO.TableColumnConfig> tableConfig = new ArrayList<>();
        ProjectDatatableDO.TableColumnConfig config = new ProjectDatatableDO.TableColumnConfig();
        config.setColType("id1");
        config.setColType("string");
        config.setAssociateKey(true);
        config.setLabelKey(false);
        config.setGroupKey(true);
        tableConfig.add(config);
        return ProjectDatatableDO.builder().upk(upk).tableConfig(tableConfig).build();
    }

    private ProjectJobDO buildProjectJobDO(boolean isTaskEmpty) {
        ProjectJobDO.UPK upk = new ProjectJobDO.UPK();
        upk.setProjectId(PROJECT_ID);
        upk.setJobId(JOB_ID);
        ProjectJobDO projectJobDO = ProjectJobDO.builder().upk(upk).graphId(GRAPH_ID).edges(Collections.emptyList()).build();
        Map<String, ProjectTaskDO> projectTaskDOMap = new HashMap<>();
        ProjectTaskDO.UPK taskUpk = new ProjectTaskDO.UPK();
        taskUpk.setJobId(JOB_ID);
        taskUpk.setProjectId(PROJECT_ID);
        if (!isTaskEmpty) {
            taskUpk.setTaskId(TASK_ID);
            projectTaskDOMap.put(TASK_ID, ProjectTaskDO.builder().upk(taskUpk).graphNode(buildProjectGraphNodeDO()).build());
        } else {
            taskUpk.setTaskId("task-dabgvasfasdasdasssss");
            projectTaskDOMap.put("task-dabgvasfasdasdasssss", ProjectTaskDO.builder().upk(taskUpk).graphNode(buildProjectGraphNodeDO()).build());
        }
        projectJobDO.setTasks(projectTaskDOMap);
        projectJobDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectJobDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        return projectJobDO;
    }

    private List<ProjectDatatableDO.UPK> buildProjectDatatableDOUPK() {
        List<ProjectDatatableDO.UPK> upkList = new ArrayList<>();
        ProjectDatatableDO.UPK upk = new ProjectDatatableDO.UPK();
        upk.setDatatableId("alice-ref1");
        upk.setNodeId("alice");
        upk.setProjectId(PROJECT_ID);
        upkList.add(upk);
        return upkList;
    }

    private ProjectGraphNodeDO buildProjectGraphNodeDO() {
        ProjectGraphNodeDO.UPK upk = new ProjectGraphNodeDO.UPK();
        upk.setGraphNodeId("alice");
        return ProjectGraphNodeDO.builder().upk(upk).nodeDef(Pipeline.NodeDef.getDefaultInstance()).build();
    }

    private List<ProjectResultDO> buildProjectResultDOList() {
        List<ProjectResultDO> projectResultDOList = new ArrayList<>();
        ProjectResultDO.UPK upk = new ProjectResultDO.UPK();
        upk.setKind(ResultKind.FedTable);
        upk.setNodeId("alice");
        upk.setRefId("alice-ref1");
        upk.setProjectId(PROJECT_ID);
        ProjectResultDO projectResultDO = ProjectResultDO.builder().upk(upk).taskId(TASK_ID).jobId("op-psiv3-dabgvasfasdasdas").build();
        projectResultDO.setGmtCreate(DateTimes.utcFromRfc3339("2023-08-02T08:30:15.235+08:00"));
        projectResultDO.setGmtModified(DateTimes.utcFromRfc3339("2023-08-02T16:30:15.235+08:00"));
        projectResultDOList.add(projectResultDO);
        return projectResultDOList;
    }

    private Domaindata.QueryDomainDataResponse buildQueryDomainDataResponse(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainData.newBuilder().setDomainId("alice").setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").
                        setDatasourceId("alice-datasource-ref1").addColumns(Common.DataColumn.newBuilder().setName("id1").setType("string")).build()
        ).build();
    }

    private Domaindata.QueryDomainDataResponse buildEmptyQueryDomainDataResponse(Integer code) {
        return Domaindata.QueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).build();
    }

    private Domaindata.BatchQueryDomainDataResponse buildBatchQueryDomainDataResponse(Integer code) {
        return Domaindata.BatchQueryDomainDataResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(code).build()).setData(
                Domaindata.DomainDataList.newBuilder().addDomaindataList(Domaindata.DomainData.newBuilder().setDomainId("alice").
                        setDomaindataId("alice-ref1").setType("2").setRelativeUri("dmds://psi_125676513").build()).build()
        ).build();
    }

    @Test
    void createProject() throws Exception {
        assertResponse(() -> {
            CreateProjectRequest request = new CreateProjectRequest();
            request.setName("test");
            request.setDescription("test project");
            request.setComputeMode("mpc");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_CREATE));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(instRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildInstDO()));
            Mockito.when(nodeRepository.findById(Mockito.any())).thenReturn(Optional.of(FakerUtils.fake(NodeDO.class)));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "createProject", CreateProjectRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void createProjectByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            CreateProjectRequest request = new CreateProjectRequest();
            request.setName("test");
            request.setDescription("test project");
            request.setComputeMode("mpc");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_CREATE));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "createProject", CreateProjectRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void createProjectByInstNotExistsException() throws Exception {
        assertErrorCode(() -> {
            CreateProjectRequest request = new CreateProjectRequest();
            request.setName("test");
            request.setDescription("test project");
            request.setComputeMode("mpc");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_CREATE));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(instRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "createProject", CreateProjectRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, InstErrorCode.INST_NOT_EXISTS);
    }

    @Test
    void listProject() throws Exception {
        assertResponse(() -> {
            Mockito.when(projectRepository.findAll()).thenReturn(Collections.emptyList());
            Mockito.when(projectNodeRepository.findByNodeId(Mockito.anyString())).thenReturn(Collections.emptyList());
            Mockito.when(projectRepository.findAllById(Mockito.anySet())).thenReturn(Collections.emptyList());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "listProject"));
        });
        UserContext.remove();
    }

    @Test
    void getProject() throws Exception {
        assertResponse(() -> {
            GetProjectRequest request = FakerUtils.fake(GetProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_GET));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectDatatableRepository.findUpkByProjectId(Mockito.anyString(), Mockito.any())).thenReturn(buildProjectDatatableDOUPK());
            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(0);
            Mockito.when(dataStub.batchQueryDomainData(Mockito.any())).thenReturn(batchQueryDomainDataResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProject", GetProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getProjectByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectRequest request = FakerUtils.fake(GetProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProject", GetProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getProjectByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            GetProjectRequest request = FakerUtils.fake(GetProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_GET));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectDatatableRepository.findUpkByProjectId(Mockito.anyString(), Mockito.any())).thenReturn(buildProjectDatatableDOUPK());
            Domaindata.BatchQueryDomainDataResponse batchQueryDomainDataResponse = buildBatchQueryDomainDataResponse(1);
            Mockito.when(dataStub.batchQueryDomainData(Mockito.any())).thenReturn(batchQueryDomainDataResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProject", GetProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }

    @Test
    void updateProject() throws Exception {
        assertResponseWithEmptyData(() -> {
            UpdateProjectRequest request = FakerUtils.fake(UpdateProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "updateProject", UpdateProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void updateProjectByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            UpdateProjectRequest request = FakerUtils.fake(UpdateProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "updateProject", UpdateProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void deleteProject() throws Exception {
        assertResponseWithEmptyData(() -> {
            GetProjectRequest request = FakerUtils.fake(GetProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DELETE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "deleteProject", GetProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteProjectByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectRequest request = FakerUtils.fake(GetProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DELETE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "deleteProject", GetProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void deleteProjectByProjectGraphNotEmptyException() throws Exception {
        assertErrorCode(() -> {
            GetProjectRequest request = FakerUtils.fake(GetProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DELETE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(graphRepository.countByProjectId(Mockito.anyString())).thenReturn(1);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "deleteProject", GetProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_GRAPH_NOT_EMPTY);
    }

    @Test
    void addProjectInst() throws Exception {
        assertResponseWithEmptyData(() -> {
            AddInstToProjectRequest request = FakerUtils.fake(AddInstToProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_INST));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(instRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildInstDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectInst", AddInstToProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void addProjectInstByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            AddInstToProjectRequest request = FakerUtils.fake(AddInstToProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_INST));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectInst", AddInstToProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void addProjectInstByInstNotExistsException() throws Exception {
        assertErrorCode(() -> {
            AddInstToProjectRequest request = FakerUtils.fake(AddInstToProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_INST));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectInst", AddInstToProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, InstErrorCode.INST_NOT_EXISTS);
    }

    @Test
    void addProjectNode() throws Exception {
        assertResponseWithEmptyData(() -> {
            AddNodeToProjectRequest request = FakerUtils.fake(AddNodeToProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_NODE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(nodeRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectNode", AddNodeToProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void addProjectNodeByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            AddNodeToProjectRequest request = FakerUtils.fake(AddNodeToProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_NODE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectNode", AddNodeToProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void addProjectNodeByNodeNotExistsException() throws Exception {
        assertErrorCode(() -> {
            AddNodeToProjectRequest request = FakerUtils.fake(AddNodeToProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_NODE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectNode", AddNodeToProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, NodeErrorCode.NODE_NOT_EXIST_ERROR);
    }

    @Test
    void addProjectDatatable() throws Exception {
        assertResponseWithEmptyData(() -> {
            AddProjectDatatableRequest request = FakerUtils.fake(AddProjectDatatableRequest.class);
            List<TableColumnConfigParam> configs = new ArrayList<>(1);
            TableColumnConfigParam tableColumnConfigParam = new TableColumnConfigParam();
            tableColumnConfigParam.setColName("id1");
            tableColumnConfigParam.setGroupKey(true);
            tableColumnConfigParam.setAssociateKey(true);
            tableColumnConfigParam.setLabelKey(true);
            configs.add(tableColumnConfigParam);
            request.setConfigs(configs);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_TABLE));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectDatatable", AddProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void addProjectDatatableByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            AddProjectDatatableRequest request = FakerUtils.fake(AddProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_TABLE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectDatatable", AddProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void addProjectDatatableByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            AddProjectDatatableRequest request = FakerUtils.fake(AddProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ADD_TABLE));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(1);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "addProjectDatatable", AddProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }

    @Test
    void deleteProjectDatatable() throws Exception {
        assertResponseWithEmptyData(() -> {
            DeleteProjectDatatableRequest request = FakerUtils.fake(DeleteProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DATATABLE_DELETE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectDatatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "deleteProjectDatatable", DeleteProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void deleteProjectDatatableByProjectDatatableNotExistsException() throws Exception {
        assertErrorCode(() -> {
            DeleteProjectDatatableRequest request = FakerUtils.fake(DeleteProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DATATABLE_DELETE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "deleteProjectDatatable", DeleteProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_DATATABLE_NOT_EXISTS);
    }

    @Test
    void getProjectDatatable() throws Exception {
        assertResponse(() -> {
            GetProjectDatatableRequest request = FakerUtils.fake(GetProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DATATABLE_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectDatatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectDatatable", GetProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getProjectDatatableByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectDatatableRequest request = FakerUtils.fake(GetProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DATATABLE_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectDatatable", GetProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getProjectDatatableByProjectDatatableNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectDatatableRequest request = FakerUtils.fake(GetProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DATATABLE_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectDatatable", GetProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_DATATABLE_NOT_EXISTS);
    }

    @Test
    void getProjectDatatableByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            GetProjectDatatableRequest request = FakerUtils.fake(GetProjectDatatableRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_DATATABLE_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectDatatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(1);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getProjectDatatable", GetProjectDatatableRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }


    @Test
    void listJob() throws Exception {
        assertResponse(() -> {
            ListProjectJobRequest request = FakerUtils.fake(ListProjectJobRequest.class);
            request.setPageNum(1);
            request.setPageSize(20);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_LIST));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            //Mockito.when(projectJobRepository.pageByProjectIdAndGraphId(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(page);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "listJob", ListProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    @Disabled
    void getJob() throws Exception {
        assertResponse(() -> {
            GetProjectJobRequest request = FakerUtils.fake(GetProjectJobRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectResultRepository.findByProjectJobId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectResultDOList());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJob", GetProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getJobByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobRequest request = FakerUtils.fake(GetProjectJobRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJob", GetProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getJobByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobRequest request = FakerUtils.fake(GetProjectJobRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_GET));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJob", GetProjectJobRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void stopJob() throws Exception {
        assertResponseWithEmptyData(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_STOP));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectResultRepository.findByProjectJobId(Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectResultDOList());

            Mockito.when(jobStub.stopJob(Mockito.any())).thenReturn(org.secretflow.v1alpha1.kusciaapi.Job.StopJobResponse.newBuilder().build());
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "stopJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void stopJobByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_STOP));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "stopJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void stopJobByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            StopProjectJobTaskRequest request = FakerUtils.fake(StopProjectJobTaskRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_JOB_STOP));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "stopJob", StopProjectJobTaskRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getJobLog() throws Exception {
        assertResponse(() -> {
            GetProjectJobTaskLogRequest request = FakerUtils.fake(GetProjectJobTaskLogRequest.class);
            request.setTaskId("task-dabgvasfasdasdas");
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_LOGS));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobLog", GetProjectJobTaskLogRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getJobLogByProjectNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskLogRequest request = FakerUtils.fake(GetProjectJobTaskLogRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_LOGS));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobLog", GetProjectJobTaskLogRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getJobLogByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskLogRequest request = FakerUtils.fake(GetProjectJobTaskLogRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_LOGS));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobLog", GetProjectJobTaskLogRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getJobTaskOutput() throws Exception {
        assertResponse(() -> {
            GetProjectJobTaskOutputRequest request = FakerUtils.fake(GetProjectJobTaskOutputRequest.class);
            request.setTaskId(TASK_ID);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_OUTPUT));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(buildNodeDO());
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectResultRepository.findByOutputId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectResultDOList());

            Mockito.when(projectDatatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(0);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobTaskOutput", GetProjectJobTaskOutputRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void getJobTaskOutputByProjectJobNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskOutputRequest request = FakerUtils.fake(GetProjectJobTaskOutputRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_OUTPUT));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobTaskOutput", GetProjectJobTaskOutputRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_NOT_EXISTS);
    }

    @Test
    void getJobTaskOutputByProjectJobTaskNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskOutputRequest request = FakerUtils.fake(GetProjectJobTaskOutputRequest.class);
            request.setTaskId(TASK_ID + "sbvasdx");
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_OUTPUT));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(true)));

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobTaskOutput", GetProjectJobTaskOutputRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
    }

    @Test
    void getJobTaskOutputByDatatableNotExistsException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskOutputRequest request = FakerUtils.fake(GetProjectJobTaskOutputRequest.class);
            request.setTaskId(TASK_ID);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_OUTPUT));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectResultRepository.findByOutputId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectResultDOList());

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobTaskOutput", GetProjectJobTaskOutputRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.DATATABLE_NOT_EXISTS);
    }

    @Test
    void getJobTaskOutputByProjectFailedException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskOutputRequest request = FakerUtils.fake(GetProjectJobTaskOutputRequest.class);
            request.setTaskId(TASK_ID);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_OUTPUT));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(buildNodeDO());
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectResultRepository.findByOutputId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectResultDOList());

            Mockito.when(projectDatatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(1);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobTaskOutput", GetProjectJobTaskOutputRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Test
    void getJobTaskOutputByQueryDatatableFailedException() throws Exception {
        assertErrorCode(() -> {
            GetProjectJobTaskOutputRequest request = FakerUtils.fake(GetProjectJobTaskOutputRequest.class);
            request.setTaskId(TASK_ID);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_TASK_OUTPUT));
            Mockito.when(nodeRepository.findByNodeId(Mockito.anyString())).thenReturn(buildNodeDO());
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectJobRepository.findByJobId(Mockito.anyString())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectJobRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectJobDO(false)));
            Mockito.when(projectResultRepository.findByOutputId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(buildProjectResultDOList());

            Mockito.when(projectDatatableRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectDatatableDO()));

            Domaindata.QueryDomainDataResponse queryDomainDataResponse = buildQueryDomainDataResponse(1);
            Mockito.when(dataStub.queryDomainData(Mockito.any())).thenReturn(queryDomainDataResponse);

            return MockMvcRequestBuilders.post(getMappingUrl(ProjectController.class, "getJobTaskOutput", GetProjectJobTaskOutputRequest.class)).
                    content(JsonUtils.toJSONString(request));
        }, DatatableErrorCode.QUERY_DATATABLE_FAILED);
    }
}