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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.constant.DatabaseConstants;
import org.secretflow.secretpad.common.constant.ScheduledConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.ScheduledStatus;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.ScheduledErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.CronUtils;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JpaQueryHelper;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaAPIConstants;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.*;
import org.secretflow.secretpad.persistence.projection.CountProjection;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.scheduled.model.ScheduledDelRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledIdRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledInfoRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledOfflineRequest;
import org.secretflow.secretpad.scheduled.service.ISecretpadScheduledService;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.ScheduledService;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.graph.GraphEdge;
import org.secretflow.secretpad.service.model.graph.GraphNodeDetail;
import org.secretflow.secretpad.service.model.graph.StartGraphRequest;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.project.MergedProjectResult;
import org.secretflow.secretpad.service.model.project.PageResponse;
import org.secretflow.secretpad.service.model.project.ProjectJobSummaryVO;
import org.secretflow.secretpad.service.model.project.ProjectJobVO;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yutu
 * @date 2024/08/26
 */
@Slf4j
@Service
public class ScheduledServiceImpl implements ScheduledService {
    @Resource
    @Setter
    private ISecretpadScheduledService secretpadScheduledService;
    @Resource
    @Setter
    private ProjectRepository projectRepository;
    @Resource
    @Setter
    private ProjectGraphRepository projectGraphRepository;
    @Resource
    @Setter
    private ProjectScheduleRepository projectScheduleRepository;
    @Resource
    @Setter
    private ProjectScheduleJobRepository projectScheduleJobRepository;
    @Resource
    @Setter
    private ProjectScheduleTaskRepository projectScheduleTaskRepository;
    @Resource
    @Setter
    private ProjectJobRepository projectJobRepository;
    @Resource
    @Setter
    private GraphService graphService;
    @Resource
    @Setter
    private ProjectResultRepository projectResultRepository;
    @Resource
    @Setter
    private NodeRepository nodeRepository;
    @Resource
    @Setter
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Resource
    @Setter
    private AbstractJobManager jobManager;


    /**
     * build scheduler id
     *
     * @param scheduledIdRequest scheduled id request
     * @return scheduler id
     */
    @Override
    public String buildSchedulerId(ScheduledIdRequest scheduledIdRequest) {
        log.info("buildSchedulerId, scheduledIdRequest:{}", scheduledIdRequest);
        checkProjectAndGraph(scheduledIdRequest.getProjectId(), scheduledIdRequest.getGraphId());
        return secretpadScheduledService.buildSchedulerId(scheduledIdRequest);
    }

    /**
     * create scheduler
     *
     * @param scheduledGraphCreateRequest scheduled graph create request
     */
    @Transactional
    @Override
    public void createScheduler(ScheduledGraphCreateRequest scheduledGraphCreateRequest) {
        log.info("createScheduler, scheduledGraphCreateRequest:{}", scheduledGraphCreateRequest);
        ProjectGraphDO projectGraphDO = checkProjectAndGraph(scheduledGraphCreateRequest.getProjectId(), scheduledGraphCreateRequest.getGraphId());
        ProjectJobDO projectJobDO = checkProjectJobExitsAndOnceSuccess(scheduledGraphCreateRequest.getProjectId(), scheduledGraphCreateRequest.getGraphId());
        if (projectScheduleRepository.existsById(scheduledGraphCreateRequest.getScheduleId())) {
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_CREATE_ERROR, "schedule already exists");
        }
        ScheduledGraphCreateRequest.Cron cron = scheduledGraphCreateRequest.getCron();
        List<String> cronGroup = buildCronExpression(cron);
        ProjectScheduleDO projectScheduleDO = new ProjectScheduleDO();
        projectScheduleDO.setScheduleId(scheduledGraphCreateRequest.getScheduleId());
        projectScheduleDO.setScheduleDesc(scheduledGraphCreateRequest.getScheduleDesc());
        projectScheduleDO.setCron(cronGroup);
        projectScheduleDO.setRequest(scheduledGraphCreateRequest);
        projectScheduleDO.setGraphInfo(projectGraphDO);
        projectScheduleDO.setGraphJobId(projectJobDO.getUpk().getJobId());
        projectScheduleDO.setJobInfo(projectJobDO);
        projectScheduleDO.setOwner(UserContext.getUser().getOwnerId());
        projectScheduleDO.setCreator(UserContext.getUserName());
        projectScheduleDO.setProjectId(scheduledGraphCreateRequest.getProjectId());
        projectScheduleDO.setGraphId(scheduledGraphCreateRequest.getGraphId());
        projectScheduleDO.setCreateTime(LocalDateTime.now());
        projectScheduleRepository.save(projectScheduleDO);
        Set<String> matchingDateSet = new HashSet<>();

        if (CollectionUtils.isEmpty(cronGroup)) {
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_CREATE_ERROR, "no scheduling dates available");
        }
        List<String> matchingAllDates = new ArrayList<>();
        cronGroup.forEach(cronExpression -> {
            matchingAllDates.addAll(CronUtils.getMatchingDates(cronExpression, cron.getStartTime(), cron.getEndTime()));
        });
        if (CollectionUtils.isEmpty(matchingAllDates)) {
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_CREATE_ERROR, "no scheduling dates available");
        }
        cronGroup.forEach(cronExpression -> {
            List<String> matchingDates = CronUtils.getMatchingDates(cronExpression, cron.getStartTime(), cron.getEndTime());
            log.info("createScheduler, cronExpression:{}, matchingDates:{}", cronExpression, matchingDates);
            matchingDates.forEach(date -> {
                if (matchingDateSet.contains(date)) {
                    log.warn("createScheduler, cronExpression:{}, matchingDates:{}, duplicate date:{} skip", cronExpression, matchingDates, date);
                    return;
                }
                ProjectScheduleTaskDO projectScheduleTaskDO = new ProjectScheduleTaskDO();
                projectScheduleTaskDO.setProjectId(scheduledGraphCreateRequest.getProjectId());
                projectScheduleTaskDO.setGraphId(scheduledGraphCreateRequest.getGraphId());
                projectScheduleTaskDO.setScheduleId(scheduledGraphCreateRequest.getScheduleId());
                ScheduledIdRequest scheduledIdRequest = new ScheduledIdRequest();
                scheduledIdRequest.setProjectId(scheduledGraphCreateRequest.getProjectId());
                scheduledIdRequest.setGraphId(scheduledGraphCreateRequest.getGraphId());
                projectScheduleTaskDO.setScheduleTaskId(secretpadScheduledService.buildSchedulerId(scheduledIdRequest));
                projectScheduleTaskDO.setCron(cronExpression);
                projectScheduleTaskDO.setOwner(UserContext.getUser().getOwnerId());
                projectScheduleTaskDO.setCreator(UserContext.getUserName());
                projectScheduleTaskDO.setScheduleTaskExpectStartTime(DateTimes.toLocalDateTime(date));

                StartGraphRequest startGraphRequest = new StartGraphRequest();
                startGraphRequest.setProjectId(scheduledGraphCreateRequest.getProjectId());
                startGraphRequest.setGraphId(scheduledGraphCreateRequest.getGraphId());
                startGraphRequest.setNodes(scheduledGraphCreateRequest.getNodes());

                GraphContext.setIsScheduled(true);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                GraphContext.setScheduleExpectStartDate(projectScheduleTaskDO.getScheduleTaskExpectStartTime().format(formatter));
                graphService.startGraph(startGraphRequest);

                ProjectScheduleJobDO projectScheduleJobDO = convertToProjectScheduleJobDO(GraphContext.getProjectJobDO());
                projectScheduleJobDO.setScheduleTaskId(projectScheduleTaskDO.getScheduleTaskId());
                projectScheduleJobDO.setOwner(UserContext.getUser().getOwnerId());
                projectScheduleJobRepository.save(projectScheduleJobDO);
                Job.CreateJobRequest request = GraphContext.getRequest();
                if (ObjectUtils.isEmpty(request)) {
                    log.error("convertToProjectScheduleJobDO, request is null");
                    throw SecretpadException.of(ScheduledErrorCode.REQUEST_IS_NULL);
                }
                projectScheduleTaskDO.setJobRequest(Base64.getEncoder().encodeToString(request.toByteArray()));
                projectScheduleTaskDO.setScheduleJobId(projectScheduleJobDO.getUpk().getJobId());
                projectScheduleTaskRepository.save(projectScheduleTaskDO);
                secretpadScheduledService.addScheduler(
                        projectScheduleJobDO.getUpk().getJobId(),
                        projectScheduleDO.getScheduleId(),
                        projectScheduleTaskDO,
                        cronExpression
                );
                GraphContext.remove();
                matchingDateSet.add(date);
            });
        });

    }

    /**
     * convert to project schedule job do
     *
     * @param projectJobDO project job do
     * @return project schedule job do
     */
    @Override
    public ProjectScheduleJobDO convertToProjectScheduleJobDO(ProjectJobDO projectJobDO) {
        log.info("convertToProjectScheduleJobDO, projectJobDO:{}", projectJobDO);
        if (projectJobDO == null) {
            log.error("convertToProjectScheduleJobDO, project job is null");
            throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_NOT_EXIST);
        }
        ProjectScheduleJobDO projectScheduleJobDO = new ProjectScheduleJobDO();
        BeanUtils.copyProperties(projectJobDO, projectScheduleJobDO);
        projectScheduleJobDO.setUpk(new ProjectScheduleJobDO.UPK(projectJobDO.getUpk().getProjectId(), projectJobDO.getUpk().getJobId()));
        return projectScheduleJobDO;
    }

    /**
     * query page
     */
    @Override
    public SecretPadPageResponse<PageScheduledVO> queryPage(PageScheduledRequest request, Pageable pageable) {
        Page<ProjectScheduleDO> page = projectScheduleRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> JpaQueryHelper.getPredicate(root, request, criteriaBuilder),
                pageable);
        if (page.isEmpty()) {
            return SecretPadPageResponse.toPage(null, 0);
        }
        List<PageScheduledVO> data = page.stream().map(PageScheduledVO::from).collect(Collectors.toList());
        data.forEach(d -> {
            d.setTaskRunning(haveJobRunning(d.getScheduleId()));
            d.setOwnerName(UserContext.getUser().getOwnerName());
        });
        return SecretPadPageResponse.toPage(data, page.getTotalElements());
    }

    @Override
    @Transactional
    public void offline(ScheduledOfflineRequest request) {
        ProjectScheduleDO projectScheduleDO = checkProjectScheduleDO(request.getScheduleId());
        if (haveJobRunning(request.getScheduleId())) {
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_RUNNING_NOT_OFFLINE);
        }
        checkOwner(projectScheduleDO.getOwner());
        secretpadScheduledService.pauseScheduler(request.getScheduleId());
        projectScheduleRepository.updateStatusByScheduleId(request.getScheduleId(), ScheduledStatus.DOWN.name());
    }

    @Override
    @Transactional
    public void del(ScheduledDelRequest request) {
        ProjectScheduleDO projectScheduleDO = checkProjectScheduleDO(request.getScheduleId());
        checkOwner(projectScheduleDO.getOwner());
        ScheduledStatus status = projectScheduleDO.getStatus();
        if (status.equals(ScheduledStatus.UP)) {
            log.error("del, schedule is used, scheduleId:{}", request.getScheduleId());
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_UP_NOT_DEL);
        }
        projectScheduleRepository.deleteById(projectScheduleDO.getScheduleId());
    }

    @Override
    public ProjectJobVO info(ScheduledInfoRequest request) {
        ProjectScheduleDO projectScheduleDO = checkProjectScheduleDO(request.getScheduleId());
        ProjectJobDO job = projectScheduleDO.getJobInfo();
        List<ProjectResultDO> projectResultDOS = projectResultRepository.findByProjectJobId(projectScheduleDO.getProjectId(), job.getUpk().getJobId());
        log.info("getProjectJob projectResultDOS ={}", projectResultDOS);
        List<MergedProjectResult> results = MergedProjectResult.of(projectResultDOS);
        Map<String, List<MergedProjectResult>> taskResults = results.stream().collect(Collectors.groupingBy(MergedProjectResult::getTaskId));
        GraphDetailVO detailVO = GraphDetailVO.builder()
                .projectId(job.getUpk().getProjectId())
                .graphId(job.getGraphId())
                .name(job.getName())
                .edges(CollectionUtils.isEmpty(job.getEdges()) ? Collections.emptyList() : job.getEdges().stream().map(GraphEdge::fromDO).collect(Collectors.toList()))
                .nodes(CollectionUtils.isEmpty(job.getTasks()) ? Collections.emptyList() : job.getTasks().values().stream().map(it -> GraphNodeDetail.fromDO(
                                        it.getGraphNode(), it.getStatus(), taskResults.get(it.getUpk().getTaskId()))
                                .withJobTask(it.getUpk().getJobId(), it.getUpk().getTaskId())
                                .withJobParties(getParties(it.getParties(), nodeRepository))
                                .withTaskProgress(it.getExtraInfo().getProgress()))
                        .collect(Collectors.toList()))
                .build();
        return ProjectJobVO.from(job, detailVO);
    }

    @Override
    public SecretPadPageResponse<TaskPageScheduledVO> taskPage(TaskPageScheduledRequest request, Pageable pageable) {
        log.info("taskPage, request:{}, of:{}", request, pageable);
        Page<ProjectScheduleTaskDO> page = projectScheduleTaskRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> JpaQueryHelper.getPredicate(root, request, criteriaBuilder),
                pageable);
        if (page.isEmpty()) {
            return SecretPadPageResponse.toPage(null, 0);
        }
        List<TaskPageScheduledVO> data = page.stream().map(TaskPageScheduledVO::from).collect(Collectors.toList());
        return SecretPadPageResponse.toPage(data, page.getTotalElements());
    }

    @Override
    @Transactional
    public void taskStop(TaskStopScheduledRequest request) {
        log.info("taskStop, request:{}", request);
        ProjectScheduleTaskDO projectScheduleTaskDO = checkProjectScheduleTaskDO(request.getScheduleTaskId());
        checkOwner(projectScheduleTaskDO.getOwner());
        secretpadScheduledService.pauseScheduler(projectScheduleTaskDO.getScheduleId());
        if (projectScheduleTaskDO.getStatus().equals(ScheduledStatus.RUNNING)
                || projectScheduleTaskDO.getStatus().equals(ScheduledStatus.TO_BE_RUN)
        ) {
            Optional<ProjectScheduleJobDO> byJobId = projectScheduleJobRepository.findByJobId(projectScheduleTaskDO.getScheduleJobId());
            ProjectScheduleJobDO job = byJobId.orElseThrow(() -> SecretpadException.of(ScheduledErrorCode.SCHEDULE_TASK_NOT_EXIST));
            job.stop();
            projectScheduleJobRepository.save(job);
            kusciaGrpcClientAdapter.stopJob(Job.StopJobRequest.newBuilder().setJobId(job.getUpk().getJobId()).build());
            projectScheduleTaskRepository.updateStatus(request.getScheduleTaskId(), ScheduledStatus.STOPPING.name());
        } else {
            log.error("taskStop, schedule task status is not running, scheduleTaskId:{}", projectScheduleTaskDO.getScheduleTaskId());
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_TASK_STATUS_NOT_RUNNING, projectScheduleTaskDO.getStatus().name());
        }
    }

    private void checkScheduleTaskStatus(String scheduleId) {
        ProjectScheduleDO projectScheduleDO = checkProjectScheduleDO(scheduleId);
        ScheduledStatus status = projectScheduleDO.getStatus();
        if (status.equals(ScheduledStatus.DOWN)) {
            throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, " schedule is down");
        }
    }

    private ProjectScheduleTaskDO checkProjectScheduleTaskDO(@NotBlank String scheduleTaskId) {
        Optional<ProjectScheduleTaskDO> projectScheduleTaskDO = projectScheduleTaskRepository.findById(scheduleTaskId);
        if (projectScheduleTaskDO.isEmpty()) {
            log.error("checkProjectScheduleTaskDO, schedule task not exist, scheduleTaskId:{}", scheduleTaskId);
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_TASK_NOT_EXIST);
        }
        return projectScheduleTaskDO.get();
    }

    @Override
    @Transactional
    public void taskRerun(TaskReRunScheduledRequest request) {
        String scheduleId = request.getScheduleId();
        checkScheduleTaskStatus(scheduleId);
        String scheduleTaskId = request.getScheduleTaskId();
        ProjectScheduleTaskDO projectScheduleTaskDO = checkProjectScheduleTaskDO(scheduleTaskId);
        checkOwner(projectScheduleTaskDO.getOwner());
        ScheduledStatus status = projectScheduleTaskDO.getStatus();
        if (ScheduledStatus.TO_BE_RUN.equals(status) || ScheduledStatus.RUNNING.equals(status) || ScheduledStatus.STOPPING.equals(status)) {
            log.error("taskRerun, schedule task status is not finish, scheduleTaskId:{}", projectScheduleTaskDO.getScheduleTaskId());
            throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, " schedule task status is not finish, now is" + projectScheduleTaskDO.getStatus().name());
        }
        String type = request.getType();
        Job.RestartJobResponse restartJobResponse;
        switch (type) {
            case ScheduledConstants.SCHEDULED_RESTART_TYPE_ALL:
                Boolean allReRun = projectScheduleTaskDO.getAllReRun();
                if (allReRun != null && allReRun) {
                    throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, " schedule task already performed a rerun of all");
                }
                Job.DeleteJobResponse deleteJobResponse = kusciaGrpcClientAdapter.deleteJob(Job.DeleteJobRequest.newBuilder().setJobId(projectScheduleTaskDO.getScheduleJobId()).build());
                if (!(deleteJobResponse.getStatus().getCode() == KusciaAPIConstants.OK)) {
                    throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, deleteJobResponse.getStatus().getMessage());
                }
                Job.CreateJobRequest createJobRequest;
                try {
                    createJobRequest = Job.CreateJobRequest.parseFrom(Base64.getDecoder().decode(projectScheduleTaskDO.getJobRequest()));
                } catch (InvalidProtocolBufferException e) {
                    log.error("SecretpadJob execute data:{} ", projectScheduleTaskDO, e);
                    throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, e, e.getMessage());
                }
                jobManager.createJob(createJobRequest);
                projectResultRepository.deleteByJobId(projectScheduleTaskDO.getProjectId(), projectScheduleTaskDO.getScheduleJobId());
                projectScheduleTaskDO.setAllReRun(Boolean.TRUE);
                break;
            case ScheduledConstants.SCHEDULED_RESTART_TYPE_FAILED:
                LocalDateTime scheduleTaskStartTime = projectScheduleTaskDO.getScheduleTaskStartTime();
                if (scheduleTaskStartTime != null && LocalDateTime.now().isAfter(scheduleTaskStartTime.plusDays(30))) {
                    throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, "scheduleTaskStartTime is more than 30 days");
                }
                restartJobResponse = kusciaGrpcClientAdapter.restartJob(Job.RestartJobRequest.newBuilder().setJobId(projectScheduleTaskDO.getScheduleJobId()).build());
                if (!(restartJobResponse.getStatus().getCode() == KusciaAPIConstants.OK)) {
                    throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, restartJobResponse.getStatus().getMessage());
                }
                break;
            default:
                throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR, "type is not support");
        }
        projectScheduleTaskDO.setStatus(ScheduledStatus.RUNNING);
        projectScheduleTaskDO.setScheduleTaskStartTime(LocalDateTime.now());
        projectScheduleTaskDO.setScheduleTaskEndTime(null);
        projectScheduleTaskRepository.save(projectScheduleTaskDO);
        Optional<ProjectScheduleJobDO> projectScheduleJobDOOptional = projectScheduleJobRepository.findById(new ProjectScheduleJobDO.UPK(projectScheduleTaskDO.getProjectId(), projectScheduleTaskDO.getScheduleJobId()));
        if (projectScheduleJobDOOptional.isPresent()) {
            ProjectScheduleJobDO projectScheduleJobDO = projectScheduleJobDOOptional.get();
            projectScheduleJobDO.setStatus(GraphJobStatus.RUNNING);
            projectScheduleJobRepository.save(projectScheduleJobDO);
        }
    }

    @Override
    public ProjectJobVO taskInfo(TaskInfoScheduledRequest request) {
        ProjectScheduleDO projectScheduleDO = checkProjectScheduleDO(request.getScheduleId());
        ProjectScheduleTaskDO projectScheduleTaskDO = checkProjectScheduleTaskDO(request.getScheduleTaskId());
        String scheduleJobId = projectScheduleTaskDO.getScheduleJobId();
        Optional<ProjectScheduleJobDO> projectScheduleJobDOOptional = projectScheduleJobRepository.findByJobId(scheduleJobId);
        if (projectScheduleJobDOOptional.isEmpty()) {
            throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_NOT_EXIST);
        }
        ProjectJobDO job = ProjectScheduleJobDO.convertToProjectJobDO(projectScheduleJobDOOptional.get());
        List<ProjectResultDO> projectResultDOS = projectResultRepository.findByProjectJobId(projectScheduleDO.getProjectId(), job.getUpk().getJobId());
        log.info("getProjectJob projectResultDOS ={}", projectResultDOS);
        List<MergedProjectResult> results = MergedProjectResult.of(projectResultDOS);
        Map<String, List<MergedProjectResult>> taskResults = results.stream().collect(Collectors.groupingBy(MergedProjectResult::getTaskId));
        GraphDetailVO detailVO = GraphDetailVO.builder()
                .projectId(job.getUpk().getProjectId())
                .graphId(job.getGraphId())
                .name(job.getName())
                .edges(CollectionUtils.isEmpty(job.getEdges()) ? Collections.emptyList() : job.getEdges().stream().map(GraphEdge::fromDO).collect(Collectors.toList()))
                .nodes(CollectionUtils.isEmpty(job.getTasks()) ? Collections.emptyList() : job.getTasks().values().stream().map(it -> GraphNodeDetail.fromDO(
                                        it.getGraphNode(), it.getStatus(), taskResults.get(it.getUpk().getTaskId()))
                                .withJobTask(it.getUpk().getJobId(), it.getUpk().getTaskId())
                                .withJobParties(getParties(it.getParties(), nodeRepository))
                                .withTaskProgress(it.getExtraInfo().getProgress()))
                        .collect(Collectors.toList()))
                .build();
        return ProjectJobVO.from(job, detailVO);
    }

    @Override
    public boolean onceSuccess(ScheduledGraphOnceSuccessRequest scheduledGraphCreateRequest) {
        try {
            checkProjectAndGraph(scheduledGraphCreateRequest.getProjectId(), scheduledGraphCreateRequest.getGraphId());
            checkProjectJobExitsAndOnceSuccess(scheduledGraphCreateRequest.getProjectId(), scheduledGraphCreateRequest.getGraphId());
        } catch (SecretpadException e) {
            if (e.getErrorCode().equals(ScheduledErrorCode.PROJECT_JOB_NEED_SUCCESS_ONCE) || e.getErrorCode().equals(ScheduledErrorCode.PROJECT_JOB_NOT_EXIST)) {
                return false;
            }
            throw e;
        }
        return true;
    }

    /**
     * list project job
     *
     * @param request
     */
    @Override
    public PageResponse<ProjectJobSummaryVO> listProjectJob(ScheduleListProjectJobRequest request) {
        checkProjectAndGraph(request.getProjectId(), request.getGraphId());
        Page<ProjectScheduleJobDO> page = Strings.isNullOrEmpty(request.getGraphId()) ?
                projectScheduleJobRepository.pageByProjectId(request.getProjectId(), request.getScheduleTaskId(),
                        PageRequest.of(request.getPageNum() - 1, request.getPageSize(), Sort.Direction.DESC, DatabaseConstants.GMT_CREATE))
                : projectScheduleJobRepository.pageByProjectIdAndGraphId(request.getProjectId(), request.getGraphId(), request.getScheduleTaskId(),
                PageRequest.of(request.getPageNum() - 1, request.getPageSize(), Sort.Direction.DESC, DatabaseConstants.GMT_CREATE));
        if (Objects.isNull(page) || page.getSize() == 0) {
            return PageResponse.of(1, request.getPageSize(), Collections.emptyList());
        }
        List<String> jobIds = page.get().map(it -> it.getUpk().getJobId()).collect(Collectors.toList());
        Map<String, Long> jobFedTableCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.FedTable));
        Map<String, Long> jobModelCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.Model));
        Map<String, Long> jobRuleCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.Rule));
        Map<String, Long> reportRuleCounts = CountProjection.toMap(projectResultRepository.countByJobIds(request.getProjectId(), jobIds, ResultKind.Report));
        Map<String, Long> finishedTaskCounts = CountProjection.toMap(projectScheduleJobRepository.countTasksByJobIds(request.getProjectId(), jobIds, GraphNodeTaskStatus.SUCCEED));
        Map<String, Long> taskCounts = CountProjection.toMap(projectScheduleJobRepository.countTasksByJobIds(request.getProjectId(), jobIds));
        List<ProjectJobSummaryVO> data = page.get().map(it ->
                ProjectJobSummaryVO.of(ProjectScheduleJobDO.convertToProjectJobDO(it),
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

    private boolean haveJobRunning(String scheduleId) {
        List<ProjectScheduleTaskDO> byScheduleIdAndStatus = projectScheduleTaskRepository.findByScheduleIdAndStatus(scheduleId, ScheduledStatus.RUNNING);
        return !byScheduleIdAndStatus.isEmpty();
    }

    private ProjectScheduleDO checkProjectScheduleDO(String scheduleId) {
        Optional<ProjectScheduleDO> projectScheduleDO = projectScheduleRepository.findById(scheduleId);
        if (projectScheduleDO.isEmpty()) {
            log.error("checkProjectScheduleDO, schedule not exist, scheduleId:{}", scheduleId);
            throw SecretpadException.of(ScheduledErrorCode.SCHEDULE_NOT_EXIST);
        }
        return projectScheduleDO.get();
    }


    private ProjectGraphDO checkProjectAndGraph(String projectId, String graphId) {
        Optional<ProjectDO> projectDO = projectRepository.findById(projectId);
        if (projectDO.isEmpty()) {
            log.error("checkProjectAndGraph, project not exist, projectId:{}", projectId);
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        Optional<ProjectGraphDO> projectGraphDO = projectGraphRepository.findByGraphId(graphId, projectId);
        if (projectGraphDO.isEmpty()) {
            log.error("checkProjectAndGraph, graph not exist, graphId:{}, projectId:{}", graphId, projectId);
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_NOT_EXISTS, graphId);
        }
        return projectGraphDO.get();
    }

    /**
     * check project job exits and once success
     *
     * @param projectId project id
     * @param graphId   graph id
     * @return project job at the current moment
     * @see ProjectJobDO
     */
    private ProjectJobDO checkProjectJobExitsAndOnceSuccess(String projectId, String graphId) {
        Optional<ProjectJobDO> projectJobDO = projectJobRepository.findByProjectIdAndGraphIdOrderByIdDesc(projectId, graphId);
        if (projectJobDO.isEmpty()) {
            log.error("checkProjectJobExits, project job not exist, projectId:{}, graphId:{}", projectId, graphId);
            throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_NOT_EXIST);
        }
        List<ProjectJobDO> byStatus = projectJobRepository.findByStatus(projectId, graphId, GraphJobStatus.SUCCEED);
        if (CollectionUtils.isEmpty(byStatus)) {
            log.error("createScheduler, project job requires at least one successful , projectId:{}, graphId:{}", projectId, graphId);
            throw SecretpadException.of(ScheduledErrorCode.PROJECT_JOB_NEED_SUCCESS_ONCE);
        }
        return projectJobDO.get();
    }

    private List<String> buildCronExpression(ScheduledGraphCreateRequest.Cron cron) {
        log.info("buildCronExpression, cron:{}", cron);
        return CronUtils.buildCronExpression(
                cron.getScheduleCycle(),
                cron.getScheduleDate(),
                cron.getScheduleTime(),
                cron.getStartTime(),
                cron.getEndTime()
        );
    }

    private List<NodeSimpleInfo> getParties(List<String> parties, NodeRepository nodeRepository) {
        return nodeRepository.findByNodeIdIn(parties).stream().map(e -> NodeSimpleInfo.builder().nodeName(e.getName()).nodeId(e.getNodeId()).build()).collect(Collectors.toList());
    }

    private void checkOwner(String owner) {
        UserContextDTO user = UserContext.getUser();
        if (!StringUtils.equals(user.getOwnerId(), owner)) {
            log.error("checkOwner, user not owner, user:{}, owner:{}", user.getOwnerId(), owner);
            throw SecretpadException.of(ScheduledErrorCode.USER_NOT_OWNER);
        }
    }
}