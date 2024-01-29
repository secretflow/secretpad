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

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.graph.GraphNodeOutputVO;
import org.secretflow.secretpad.service.model.graph.GraphNodeTaskLogsVO;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.project.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Project controller
 *
 * @author xiaonan
 * @date 2023/6/15
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private GraphService graphService;

    @Autowired
    private NodeService nodeService;

    /**
     * Create a new project api
     *
     * @param request create project request
     * @return successful SecretPadResponse with create project view object
     */
    @ResponseBody
    @ApiResource(code = ApiResourceCodeConstants.PRJ_CREATE)
    @PostMapping(value = "/create", consumes = "application/json")
    @Operation(summary = "create a new project", description = "create a new project")
    public SecretPadResponse<CreateProjectVO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return SecretPadResponse.success(new CreateProjectVO(projectService.createProject(request)));
    }

    /**
     * List project api
     *
     * @return successful SecretPadResponse with project view object list
     */
    @ResponseBody
    @PostMapping(value = "/list", consumes = "application/json")
    @Operation(summary = "list project", description = "list project")
    @ApiResource(code = ApiResourceCodeConstants.PRJ_LIST)
    public SecretPadResponse<List<ProjectVO>> listProject() {
        return SecretPadResponse.success(projectService.listProject());
    }

    /**
     * Query project detail api
     *
     * @param request get project request
     * @return successful SecretPadResponse with project view object
     */
    @ResponseBody
    @PostMapping(value = "/get", consumes = "application/json")
    @Operation(summary = "query project detail", description = "query project detail")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_GET)
    public SecretPadResponse<ProjectVO> getProject(@Valid @RequestBody GetProjectRequest request) {
        return SecretPadResponse.success(projectService.getProject(request.getProjectId()));
    }

    /**
     * Update project api
     *
     * @param request update project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/update", consumes = "application/json")
    @Operation(summary = "update project", description = "update project")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_UPDATE)
    public SecretPadResponse<Void> updateProject(@Valid @RequestBody UpdateProjectRequest request) {
        projectService.updateProject(request);
        return SecretPadResponse.success();
    }

    /**
     * Delete project api
     *
     * @param request delete project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/delete", consumes = "application/json")
    @Operation(summary = "delete project", description = "delete project")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_DELETE)
    public SecretPadResponse<Void> deleteProject(@Valid @RequestBody GetProjectRequest request) {
        projectService.deleteProject(request.getProjectId());
        return SecretPadResponse.success();
    }

    /**
     * Add institution to the project api
     *
     * @param request add institution to project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/inst/add")
    @Operation(summary = "add institution to the project", description = "add institution to the project")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_ADD_INST)
    public SecretPadResponse<Object> addProjectInst(@Valid @RequestBody AddInstToProjectRequest request) {
        projectService.addInstToProject(request);
        return SecretPadResponse.success();
    }


    /**
     * Add node to the project api
     *
     * @param request add node to project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/node/add")
    @Operation(summary = "add node to the project", description = "add node to the project")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_ADD_NODE)
    public SecretPadResponse<Object> addProjectNode(@Valid @RequestBody AddNodeToProjectRequest request) {
        projectService.addNodeToProject(request);
        return SecretPadResponse.success();
    }


    /**
     * Add datatable to the project api
     *
     * @param request add datatable to project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/datatable/add")
    @Operation(summary = "add datatable to the project", description = "add datatable to the project")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_ADD_TABLE)
    public SecretPadResponse<Object> addProjectDatatable(@Valid @RequestBody AddProjectDatatableRequest request) {
        projectService.addDatatableToProject(request);
        return SecretPadResponse.success();
    }

    /**
     * Delete datatable and cancel datatable authorization in the project api
     *
     * @param request delete project datatable request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/datatable/delete")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_DATATABLE_DELETE)
    @Operation(summary = "delete datatable and cancel datatable authorization in the project", description = "delete datatable and cancel datatable authorization in the project")
    public SecretPadResponse<Object> deleteProjectDatatable(@Valid @RequestBody DeleteProjectDatatableRequest request) {
        projectService.deleteDatatableToProject(request);
        return SecretPadResponse.success();
    }

    /**
     * Query project datatable detail api
     *
     * @param request get project request
     * @return successful SecretPadResponse with project datatable view object
     */
    @ResponseBody
    @PostMapping(value = "/datatable/get")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_DATATABLE_GET)
    @Operation(summary = "query project datatable detail", description = "query project datatable detail")
    public SecretPadResponse<Object> getProjectDatatable(@Valid @RequestBody GetProjectDatatableRequest request) {
        return SecretPadResponse.success(projectService.getProjectDatatable(request));
    }

    /**
     * Paging list project job list api
     *
     * @param request list project job request
     * @return successful SecretPadResponse with paging project job summary view object
     */
    @ResponseBody
    @PostMapping(value = "/job/list")
    @Operation(summary = "project job list", description = "project job list")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_JOB_LIST)
    public SecretPadResponse<PageResponse<ProjectJobSummaryVO>> listJob(@Valid @RequestBody ListProjectJobRequest request) {
        return SecretPadResponse.success(projectService.listProjectJob(request));
    }

    /**
     * Query project job detail api
     *
     * @param request get project job request
     * @return successful SecretPadResponse with project job view object
     */
    @ResponseBody
    @PostMapping(value = "/job/get")
    @Operation(summary = "project job detail", description = "project job detail")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_JOB_GET)
    public SecretPadResponse<ProjectJobVO> getJob(@Valid @RequestBody GetProjectJobRequest request) {
        return SecretPadResponse.success(projectService.getProjectJob(request.getProjectId(), request.getJobId()));
    }

    /**
     * Stop project job api
     *
     * @param request stop project job task request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/job/stop")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_JOB_STOP)
    @Operation(summary = "stop project job", description = "stop project job")
    public SecretPadResponse<Void> stopJob(@Valid @RequestBody StopProjectJobTaskRequest request) {
        projectService.stopProjectJob(request);
        return SecretPadResponse.success();
    }

    /**
     * Query project job task logs api
     *
     * @param request get project job task log request
     * @return successful SecretPadResponse with graph node task logs view object
     */
    @ResponseBody
    @PostMapping(value = "/job/task/logs")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_TASK_LOGS)
    @Operation(summary = "project job task logs", description = "project job task logs")
    public SecretPadResponse<GraphNodeTaskLogsVO> getJobLog(@Valid @RequestBody GetProjectJobTaskLogRequest request) {
        return SecretPadResponse.success(projectService.getProjectJobTaskLogs(request));
    }

    /**
     * Query project job task output api
     *
     * @param request get project job task output request
     * @return successful SecretPadResponse with graph node output view object
     */
    @ResponseBody
    @PostMapping(value = "/job/task/output")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_TASK_OUTPUT)
    @Operation(summary = "project job task output", description = "project job task output")
    public SecretPadResponse<GraphNodeOutputVO> getJobTaskOutput(@Valid @RequestBody GetProjectJobTaskOutputRequest request) {
        return SecretPadResponse.success(graphService.getGraphNodeTaskOutputVO(request));
    }

    /**
     * create project chose tee domain id ,this is for the chose list
     *
     * @return successful SecretPadResponse with tee node list view object
     */
    @PostMapping(value = "/tee/list")
    @Operation(summary = "tee list", description = "tee list")
    public SecretPadResponse<List<NodeVO>> getTeeNodeList() {
        return SecretPadResponse.success(nodeService.listTeeNode());
    }

    /**
     * project_graph_node  outputs fix derived fields for chexian
     *
     * @param request projectId graphId
     * @return ProjectOutputVO
     */
    @PostMapping(value = "/getOutTable", consumes = "application/json")
    public SecretPadResponse<ProjectOutputVO> getProjectAllOutTable(@Valid @RequestBody GetProjectGraphRequest request) {
        return SecretPadResponse.success(projectService.getProjectAllOutTable(request.getProjectId(), request.getGraphId()));
    }

    /**
     * Update project schema api
     *
     * @param request update project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/update/tableConfig", consumes = "application/json")
    @Operation(summary = "update project table config", description = "update project table config")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_UPDATE)
    public SecretPadResponse<Void> updateProjectTableConfig(@Valid @RequestBody AddProjectDatatableRequest request) {
        projectService.updateProjectTableConfig(request);
        return SecretPadResponse.success();
    }

}
