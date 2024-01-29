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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.graph.GraphNodeTaskLogsVO;
import org.secretflow.secretpad.service.model.project.*;

import java.util.List;

/**
 * Project service interface
 *
 * @author yansi
 * @date 2023/5/4
 */
public interface ProjectService {

    /**
     * Create project
     *
     * @param request create project request
     * @return projectId
     */
    String createProject(CreateProjectRequest request);

    /**
     * Lists all current projects
     *
     * @return project view object list
     */
    List<ProjectVO> listProject();

    /**
     * Query project by projectId
     *
     * @param projectId target projectId
     * @return project view object
     */
    ProjectVO getProject(String projectId);

    /**
     * Add an institution to the project
     *
     * @param request add institution to project request
     */
    void addInstToProject(AddInstToProjectRequest request);

    /**
     * Add a node to the project
     *
     * @param request add node to project request
     */
    void addNodeToProject(AddNodeToProjectRequest request);

    /**
     * Add a datatable to the project
     *
     * @param request add datatable to project request
     */
    void addDatatableToProject(AddProjectDatatableRequest request);

    /**
     * Query project datatable by get project datatable request
     *
     * @param request get project datatable request
     * @return project datatable view object
     */
    ProjectDatatableVO getProjectDatatable(GetProjectDatatableRequest request);

    /**
     * Remove the datatable from the project
     *
     * @param request delete project datatable request
     */
    void deleteDatatableToProject(DeleteProjectDatatableRequest request);

    /**
     * Paging query project job summary list by list project job request
     *
     * @param projectId list project job request
     * @return page response of project job summary view object
     */
    PageResponse<ProjectJobSummaryVO> listProjectJob(ListProjectJobRequest projectId);

    /**
     * Query project job by projectId and jobId
     *
     * @param projectId target projectId
     * @param jobId     target jobId
     * @return project job view object
     */
    ProjectJobVO getProjectJob(String projectId, String jobId);

    /**
     * Delete the project by projectId
     *
     * @param projectId target projectId
     */
    void deleteProject(String projectId);

    /**
     * Update the project by update project request
     *
     * @param request update project request
     */
    void updateProject(UpdateProjectRequest request);

    /**
     * Query graph node task logs by get project job task log request
     *
     * @param request get project job task log request
     * @return graph node task logs view object
     */
    GraphNodeTaskLogsVO getProjectJobTaskLogs(GetProjectJobTaskLogRequest request);

    /**
     * Stop the project job by stop project job task request
     *
     * @param request stop project job task request
     */
    void stopProjectJob(StopProjectJobTaskRequest request);

    /**
     * check the project contain node.
     *
     * @param projectId project resource ID
     * @param nodeId    node ID
     * @return boolean
     */
    boolean checkNodeInProject(String projectId, String nodeId);

    /**
     * create a project in Autonomy mode
     *
     * @param request
     * @return
     */
    String createP2PProject(CreateProjectRequest request);

    /**
     * Lists all current projects in Autonomy mode
     *
     * @return
     */
    List<ProjectVO> listP2PProject();

    /**
     * Update the project by update project request
     *
     * @param request update project request
     */
    void updateP2PProject(UpdateProjectRequest request);

    /**
     * archive the project by give projectId
     */
    void archiveProject(ArchiveProjectRequest archiveProjectRequest);

    /**
     * project_graph_node  outputs fix derived fields for chexian
     *
     * @param projectId project resource ID
     * @param graphId   project graphId
     * @return ProjectOutputVO
     */
    ProjectOutputVO getProjectAllOutTable(String projectId, String graphId);

    /**
     * update project table config
     *
     * @param request
     */
    void updateProjectTableConfig(AddProjectDatatableRequest request);
}
