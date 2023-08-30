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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.DatabaseConstants;
import org.secretflow.secretpad.common.errorcode.*;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.projection.CountProjection;
import org.secretflow.secretpad.persistence.projection.ProjectNodeProjection;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.constant.DemoConstants;
import org.secretflow.secretpad.service.model.datatable.TableColumnConfigVO;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.graph.GraphEdge;
import org.secretflow.secretpad.service.model.graph.GraphNodeDetail;
import org.secretflow.secretpad.service.model.graph.GraphNodeTaskLogsVO;
import org.secretflow.secretpad.service.model.project.*;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import jakarta.transaction.Transactional;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Project service implementation class
 *
 * @author yansi
 * @date 2023/5/4
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobManager.class);

    @Autowired
    private InstRepository instRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectInstRepository projectInstRepository;

    @Autowired
    private ProjectNodeRepository projectNodeRepository;

    @Autowired
    private ProjectDatatableRepository projectDatatableRepository;

    @Autowired
    private ProjectJobTaskLogRepository jobTaskLogRepository;

    @Autowired
    private AbstractDatatableManager datatableManager;

    @Autowired
    private ProjectJobRepository projectJobRepository;

    @Autowired
    private ProjectResultRepository projectResultRepository;

    @Autowired
    private ProjectGraphRepository projectGraphDORepository;

    @Autowired
    private ProjectGraphRepository graphRepository;

    @Autowired
    private JobServiceGrpc.JobServiceBlockingStub jobStub;

    @Override
    @Transactional
    public String createProject(CreateProjectRequest request) {
        ProjectDO projectDO =
                ProjectDO.Factory.newProject(request.getName(), request.getDescription(), request.getComputeMode());
        projectRepository.save(projectDO);
        addInstToProject(new AddInstToProjectRequest(projectDO.getProjectId(), DemoConstants.DEMO_ALICE_INST_ID));
        addInstToProject(new AddInstToProjectRequest(projectDO.getProjectId(), DemoConstants.DEMO_BOB_INST_ID));
        return projectDO.getProjectId();
    }

    @Override
    public List<ProjectVO> listProject() {
        List<ProjectDO> projects = projectRepository.findAll();
        // TODO: merge find.
        return projects.stream().map(projectDO -> {
            List<ProjectNodeProjection> pnps =
                    projectNodeRepository.findProjectionByProjectId(projectDO.getProjectId());
            Integer graphCount = projectGraphDORepository.countByProjectId(projectDO.getProjectId());
            Integer jobCount = projectJobRepository.countByProjectId(projectDO.getProjectId());
            return ProjectVO.builder().projectId(projectDO.getProjectId()).projectName(projectDO.getName())
                    .description(projectDO.getDescription()).computeMode(projectDO.getComputeMode())
                    .nodes(pnps.stream().map(it -> ProjectNodeVO.from(it, null)).collect(Collectors.toList()))
                    .graphCount(graphCount).jobCount(jobCount).gmtCreate(DateTimes.toRfc3339(projectDO.getGmtCreate()))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public ProjectVO getProject(String projectId) {
        ProjectDO project = openProject(projectId);
        List<ProjectNodeProjection> pnps = projectNodeRepository.findProjectionByProjectId(project.getProjectId());
        Map<DatatableDTO.NodeDatatableId, DatatableDTO> dtoMap = getProjectDatatableDtos(projectId);
        Map<String, List<DatatableDTO>> nodeDtos =
                dtoMap.values().stream().collect(Collectors.groupingBy(DatatableDTO::getNodeId));
        return ProjectVO.builder().projectId(project.getProjectId()).projectName(project.getName())
                .description(project.getDescription()).computeMode(project.getComputeMode())
                .nodes(pnps.stream()
                        .map(it -> ProjectNodeVO.from(it, nodeDtos.get(it.getProjectNodeDO().getUpk().getNodeId())))
                        .collect(Collectors.toList()))
                .build();
    }


    @Override
    @Transactional
    public void updateProject(UpdateProjectRequest request) {
        ProjectDO project = openProject(request.getProjectId());
        if (!Strings.isNullOrEmpty(request.getName())) {
            project.setName(request.getName());
        }
        if (!Strings.isNullOrEmpty(request.getDescription())) {
            project.setDescription(request.getDescription());
        }
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId) {
        openProject(projectId);
        if (graphRepository.countByProjectId(projectId) != 0) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_GRAPH_NOT_EMPTY);
        }
        projectRepository.deleteById(projectId);
    }

    /**
     * Query project datatable data transfer object list by projectId then collect to a map
     *
     * @param projectId target projectId
     * @return Map of node datatableId and datatable data transfer object
     */
    private Map<DatatableDTO.NodeDatatableId, DatatableDTO> getProjectDatatableDtos(String projectId) {
        List<ProjectDatatableDO.UPK> pdUpks = projectDatatableRepository.findUpkByProjectId(projectId,
                ProjectDatatableDO.ProjectDatatableSource.IMPORTED);
        return datatableManager.findByIds(
                pdUpks.stream().map(upk -> DatatableDTO.NodeDatatableId.from(upk.getNodeId(), upk.getDatatableId()))
                        .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void addInstToProject(AddInstToProjectRequest request) {
        openProject(request.getProjectId());
        openInst(request.getInstId());
        // Note: we don't check whether inst is in project, so this operation is idempotent.
        projectInstRepository.save(ProjectInstDO.Factory.newProjectInst(request.getProjectId(), request.getInstId()));
    }

    @Override
    @Transactional
    public void addNodeToProject(AddNodeToProjectRequest request) {
        openProject(request.getProjectId());
        // openProjectInst(request.getProjectId(), xxx)  // TODO: check inst id
        openNode(request.getNodeId());
        // Note: we don't check whether node is in project, so this operation is idempotent.
        projectNodeRepository.save(ProjectNodeDO.Factory.newProjectNode(request.getProjectId(), request.getNodeId()));
    }

    @Override
    @Transactional
    public void addDatatableToProject(AddProjectDatatableRequest request) {
        openProject(request.getProjectId());
        openProjectNode(request.getProjectId(), request.getNodeId());
        // Note: we don't check whether datatable is in project, so this operation is idempotent.
        DatatableDTO datatable = openDatatable(request.getNodeId(), request.getDatatableId());
        Map<String, TableColumnConfigParam> configs = Maps.newHashMap();
        if (!CollectionUtils.isEmpty(request.getConfigs())) {
            request.getConfigs().forEach(
                    it -> configs.put(it.getColName(), it)
            );
        }
        // for column that not exist in table, just ignore
        LOGGER.info("add datatable, configs={}, datatable={}", JsonUtils.toJSONString(configs), JsonUtils.toJSONString(datatable));
        ProjectDatatableDO projectDatatable = ProjectDatatableDO.Factory.newProjectDatatable(
                request.getProjectId(), request.getNodeId(), request.getDatatableId(),
                datatable.getSchema().stream().map(c ->
                                ProjectDatatableDO.TableColumnConfig.from(
                                        new ProjectDatatableDO.TableColumn(c.getColName(), c.getColType(), c.getColComment()),
                                        configs.containsKey(c.getColName()) && configs.get(c.getColName()).isAssociateKey(),
                                        configs.containsKey(c.getColName()) && configs.get(c.getColName()).isGroupKey(),
                                        configs.containsKey(c.getColName()) && configs.get(c.getColName()).isLabelKey()
                                ))
                        .collect(Collectors.toList()));
        projectDatatableRepository.save(projectDatatable);
    }

    @Override
    public ProjectDatatableVO getProjectDatatable(GetProjectDatatableRequest request) {
        openProject(request.getProjectId());
        ProjectDatatableDO projectDatatable = openProjectDatatable(request.getProjectId(), request.getNodeId(), request.getDatatableId());
        DatatableDTO datatableDto = openDatatable(request.getNodeId(), request.getDatatableId());
        return new ProjectDatatableVO(datatableDto.getDatatableId(), datatableDto.getDatatableName(),
                projectDatatable.getTableConfig().stream().map(TableColumnConfigVO::from).collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public void deleteDatatableToProject(DeleteProjectDatatableRequest request) {
        openProjectDatatable(request.getProjectId(), request.getNodeId(), request.getDatatableId());
        projectDatatableRepository.deleteById(new ProjectDatatableDO.UPK(request.getProjectId(), request.getNodeId(), request.getDatatableId()));
    }

    @Override
    public PageResponse<ProjectJobSummaryVO> listProjectJob(ListProjectJobRequest request) {
        Page<ProjectJobDO> page = Strings.isNullOrEmpty(request.getGraphId()) ?
                projectJobRepository.pageByProjectId(request.getProjectId(),
                        PageRequest.of(request.getPageNum() - 1, request.getPageSize(), Sort.Direction.DESC, DatabaseConstants.GMT_CREATE))
                : projectJobRepository.pageByProjectIdAndGraphId(request.getProjectId(), request.getGraphId(),
                PageRequest.of(request.getPageNum() - 1, request.getPageSize(), Sort.Direction.DESC, DatabaseConstants.GMT_CREATE));
        if (Objects.isNull(page) || page.getSize() == 0) {
            return PageResponse.of(1, request.getPageSize(), Collections.emptyList());
        }
        List<String> jobIds = page.get().map(it -> it.getUpk().getJobId()).collect(Collectors.toList());
        Map<String, Long> jobFedTableCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.FedTable));
        Map<String, Long> jobModelCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.Model));
        Map<String, Long> jobRuleCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.Rule));
        Map<String, Long> reportRuleCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.Report));
        Map<String, Long> finishedTaskCounts = CountProjection.toMap(projectJobRepository.countTasksByJobIds(request.getProjectId(), jobIds, GraphNodeTaskStatus.SUCCEED));
        Map<String, Long> taskCounts = CountProjection.toMap(projectJobRepository.countTasksByJobIds(request.getProjectId(), jobIds));
        List<ProjectJobSummaryVO> data = page.get().map(it ->
                ProjectJobSummaryVO.of(it,
                        jobFedTableCounts.getOrDefault(it.getUpk().getJobId(), 0L),
                        jobModelCounts.getOrDefault(it.getUpk().getJobId(), 0L),
                        jobRuleCounts.getOrDefault(it.getUpk().getJobId(), 0L),
                        reportRuleCounts.getOrDefault(it.getUpk().getJobId(), 0L),
                        finishedTaskCounts.getOrDefault(it.getUpk().getJobId(), 0L),
                        taskCounts.getOrDefault(it.getUpk().getJobId(), 0L)
                )
        ).collect(Collectors.toList());
        return PageResponse.of(page.getTotalPages(), request.getPageSize(), data);
    }

    @Override
    public ProjectJobVO getProjectJob(String projectId, String jobId) {
        openProject(projectId);
        ProjectJobDO job = openProjectJob(projectId, jobId);
        List<MergedProjectResult> results = MergedProjectResult.of(projectResultRepository.findByProjectJobId(projectId, jobId));
        Map<String, List<MergedProjectResult>> taskResults = results.stream().collect(Collectors.groupingBy(MergedProjectResult::getTaskId));
        GraphDetailVO detailVO = GraphDetailVO.builder()
                .projectId(job.getUpk().getProjectId())
                .graphId(job.getGraphId())
                .name(job.getName())
                .edges(CollectionUtils.isEmpty(job.getEdges()) ? Collections.emptyList() : job.getEdges().stream().map(GraphEdge::fromDO).collect(Collectors.toList()))
                .nodes(CollectionUtils.isEmpty(job.getTasks()) ? Collections.emptyList() : job.getTasks().values().stream().map(it -> GraphNodeDetail.fromDO(
                                        it.getGraphNode(), it.getStatus(), taskResults.get(it.getUpk().getTaskId()))
                                .withJobTask(it.getUpk().getJobId(), it.getUpk().getTaskId()))
                        .collect(Collectors.toList()))
                .build();
        return ProjectJobVO.from(job, detailVO);
    }

    @Override
    @Transactional
    public void stopProjectJob(StopProjectJobTaskRequest request) {
        openProject(request.getProjectId());
        ProjectJobDO job = openProjectJob(request.getProjectId(), request.getJobId());
        job.stop();
        // TODO: we don't check the status, because of we can't know error reason. For job not found, should be treat as success now.
        jobStub.stopJob(Job.StopJobRequest.newBuilder().setJobId(job.getUpk().getJobId()).build());
        projectJobRepository.save(job);
    }

    @Override
    public GraphNodeTaskLogsVO getProjectJobTaskLogs(GetProjectJobTaskLogRequest request) {
        openProject(request.getProjectId());
        ProjectJobDO job = openProjectJob(request.getProjectId(), request.getJobId());
        return new GraphNodeTaskLogsVO(job.getTasks().get(request.getTaskId()).getStatus(),
                jobTaskLogRepository.findAllByJobTaskId(request.getJobId(), request.getTaskId())
                        .stream().map(ProjectJobTaskLogDO::getLog).collect(Collectors.toList()));
    }


    /**
     * Open the project information by projectId
     *
     * @param projectId target projectId
     * @return project data object
     */
    private ProjectDO openProject(String projectId) {
        Optional<ProjectDO> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        return projectOpt.get();
    }

    /**
     * Open the institution information by institutionId
     *
     * @param instId target institutionId
     * @return institution data object
     */
    private InstDO openInst(String instId) {
        Optional<InstDO> instOpt = instRepository.findById(instId);
        if (instOpt.isEmpty()) {
            throw SecretpadException.of(InstErrorCode.INST_NOT_EXISTS);
        }
        return instOpt.get();
    }

    /**
     * Open the node information by nodeId
     *
     * @param nodeId target nodeId
     * @return node data object
     */
    private NodeDO openNode(String nodeId) {
        Optional<NodeDO> nodeOpt = nodeRepository.findById(nodeId);
        if (nodeOpt.isEmpty()) {
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        return nodeOpt.get();
    }

    /**
     * Open the project node information by nodeId
     *
     * @param projectId target projectId
     * @param nodeId    target nodeId
     * @return project node data object
     */
    private ProjectNodeDO openProjectNode(String projectId, String nodeId) {
        Optional<ProjectNodeDO> projectNodeDOopt = projectNodeRepository.findById(new ProjectNodeDO.UPK(projectId, nodeId));
        if (projectNodeDOopt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NODE_NOT_EXISTS);
        }
        return projectNodeDOopt.get();
    }

    /**
     * Open the project datatable information by projectId, nodeId and datatableId
     *
     * @param projectId   target projectId
     * @param nodeId      target nodeId
     * @param datatableId target datatableId
     * @return project datatable data object
     */
    private ProjectDatatableDO openProjectDatatable(String projectId, String nodeId, String datatableId) {
        Optional<ProjectDatatableDO> projectDatatableOpt = projectDatatableRepository.findById(
                new ProjectDatatableDO.UPK(projectId, nodeId, datatableId));
        if (projectDatatableOpt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_DATATABLE_NOT_EXISTS);
        }
        return projectDatatableOpt.get();
    }

    /**
     * Open the project job information by projectId and jobId
     *
     * @param projectId target projectId
     * @param jobId     target jobId
     * @return project job data object
     */
    private ProjectJobDO openProjectJob(String projectId, String jobId) {
        Optional<ProjectJobDO> jobOpt = projectJobRepository.findById(new ProjectJobDO.UPK(projectId, jobId));
        if (jobOpt.isEmpty()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
        }
        return jobOpt.get();
    }

    /**
     * Open the datatable information by nodeId and datatableId
     *
     * @param nodeId      target nodeId
     * @param datatableId target datatableId
     * @return datatable data transfer object
     */
    private DatatableDTO openDatatable(String nodeId, String datatableId) {
        Optional<DatatableDTO> datatableOpt = datatableManager.findById(DatatableDTO.NodeDatatableId.from(nodeId, datatableId));
        if (datatableOpt.isEmpty()) {
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
        }
        return datatableOpt.get();
    }

}
