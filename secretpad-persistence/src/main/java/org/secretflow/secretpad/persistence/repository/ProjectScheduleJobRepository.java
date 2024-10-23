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

package org.secretflow.secretpad.persistence.repository;

import org.secretflow.secretpad.persistence.entity.ProjectScheduleJobDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.projection.CountProjection;
import org.secretflow.secretpad.persistence.projection.ProjectJobStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * project schedule job repository
 *
 * @author yutu
 * @date 2024/8/27
 */
@Repository
public interface ProjectScheduleJobRepository extends BaseRepository<ProjectScheduleJobDO, ProjectScheduleJobDO.UPK> {

    /**
     * find ProjectScheduleJobDO list by projectId
     *
     * @param projectIds target projectId list
     * @return project job list
     */
    @Query("from ProjectScheduleJobDO pj where pj.upk.projectId in :projectIds")
    List<ProjectScheduleJobDO> findByProjectIds(@Param("projectIds") List<String> projectIds);

    /**
     * find GraphJobStatus list by project jobId
     *
     * @param jobIds target jobId list
     * @return project job status list
     */
    @Query("select pj.status from ProjectScheduleJobDO pj where pj.upk.jobId in :jobIds")
    List<GraphJobStatus> findStatusByJobIds(@Param("jobIds") List<String> jobIds);

    /**
     * Query project job result by jobId
     *
     * @param jobId target jobId
     * @return project job result
     */
    @EntityGraph(value = "project_schedule_job.all_task")
    @Query("from ProjectScheduleJobDO pj where pj.upk.jobId=:jobId")
    Optional<ProjectScheduleJobDO> findByJobId(@Param("jobId") String jobId);

    /**
     * Paging query project job results by projectId, graphId and pageable
     *
     * @param projectId target projectId
     * @param graphId   target graphId
     * @param pageable  paging configuration
     * @return project job results
     */
    @Query("from ProjectScheduleJobDO pj where pj.upk.projectId=:projectId and pj.graphId=:graphId and pj.scheduleTaskId=:scheduleTaskId")
    Page<ProjectScheduleJobDO> pageByProjectIdAndGraphId(@Param("projectId") String projectId, @Param("graphId") String graphId, @Param("scheduleTaskId") String scheduleTaskId, Pageable pageable);

    /**
     * Paging query project job results by projectId and pageable
     *
     * @param projectId target projectId
     * @param pageable  paging configuration
     * @return project job results
     */
    @Query("from ProjectScheduleJobDO pj where pj.upk.projectId=:projectId and pj.scheduleTaskId=:scheduleTaskId")
    Page<ProjectScheduleJobDO> pageByProjectId(@Param("projectId") String projectId, @Param("scheduleTaskId") String scheduleTaskId, Pageable pageable);

    /**
     * Query the count of project job results satisfied by projectId
     *
     * @param projectId target projectId
     * @return the count of project job results satisfied by projectId
     */
    @Query("select count(*) from ProjectScheduleJobDO pj where pj.upk.projectId=:projectId")
    Integer countByProjectId(@Param("projectId") String projectId);

    /**
     * Query CountProjection list in project task table by projectId and jobIds
     *
     * @param projectId target projectId
     * @param jobIds    target jobId list
     * @return CountProjection list in project task table
     */
    @Query("select new org.secretflow.secretpad.persistence.projection.CountProjection(d.upk.jobId, count(*)) from ProjectTaskDO d where d.upk.projectId=:projectId and d.upk.jobId in :jobIds group by d.upk.jobId")
    List<CountProjection> countTasksByJobIds(@Param("projectId") String projectId, @Param("jobIds") List<String> jobIds);

    /**
     * Query CountProjection list in project task table by projectId, jobIds and taskStatus
     *
     * @param projectId  target projectId
     * @param jobIds     target jobId list
     * @param taskStatus target task status
     * @return CountProjection list in project task table
     */
    @Query("select new org.secretflow.secretpad.persistence.projection.CountProjection(d.upk.jobId, count(*)) from ProjectTaskDO d where d.upk.projectId=:projectId and d.status=:taskStatus and d.upk.jobId in :jobIds group by d.upk.jobId")
    List<CountProjection> countTasksByJobIds(@Param("projectId") String projectId, @Param("jobIds") List<String> jobIds, @Param("taskStatus") GraphNodeTaskStatus taskStatus);

    /**
     * Query project job results by projectId, graphId and status
     *
     * @param projectId target projectId
     * @param graphId   target graphId
     * @param status    target status
     * @return project job results
     */
    @Query("from ProjectScheduleJobDO pj where pj.upk.projectId=:projectId and pj.graphId=:graphId and pj.status=:status")
    List<ProjectScheduleJobDO> findByStatus(@Param("projectId") String projectId, @Param("graphId") String graphId, @Param("status") GraphJobStatus status);

    /**
     * Query ProjectJobStatus list in project job table by projectId and jobIds
     *
     * @param projectId target projectId
     * @param jobIds    target jobId list
     * @return ProjectJobStatus list
     */
    @Query("select new org.secretflow.secretpad.persistence.projection.ProjectJobStatus(d.upk.projectId, d.upk.jobId, d.status) from ProjectScheduleJobDO d where d.upk.projectId=:projectId and d.upk.jobId in :jobIds")
    List<ProjectJobStatus> findStatusByJobIds(@Param("projectId") String projectId, @Param("jobIds") List<String> jobIds);

    @Query(nativeQuery = true, value = "delete from project_schedule_job")
    @Modifying
    @Transactional
    void deleteAllAuthentic();
}