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

package org.secretflow.secretpad.persistence.repository;


import org.secretflow.secretpad.persistence.entity.ProjectResultDO;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.projection.CountProjection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Project result repository
 *
 * @author yansi
 * @date 2023/6/8
 */
@Repository
public interface ProjectResultRepository extends BaseRepository<ProjectResultDO, ProjectResultDO.UPK> {
    /**
     * Query project result results by projectId
     *
     * @param projectId target projectId
     * @return project result results
     */
    @Query("from ProjectResultDO d where d.upk.projectId=:projectId")
    List<ProjectResultDO> findByProjectId(@Param("projectId") String projectId);

    /**
     * Query project result results by projectId and jobId
     *
     * @param projectId target projectId
     * @param jobId     target jobId
     * @return project result results
     */
    @Query("from ProjectResultDO d where d.upk.projectId=:projectId and d.jobId=:jobId")
    List<ProjectResultDO> findByProjectJobId(@Param("projectId") String projectId, @Param("jobId") String jobId);

    /**
     * Query project result results by projectId, taskId and outputId
     *
     * @param projectId target projectId
     * @param taskId    target taskId
     * @param outputId  target outputId
     * @return project result results
     */
    @Query("from ProjectResultDO d where d.upk.projectId=:projectId and d.taskId=:taskId and d.upk.refId=:outputId")
    List<ProjectResultDO> findByOutputId(@Param("projectId") String projectId, @Param("taskId") String taskId, @Param("outputId") String outputId);

    /**
     * Query CountProjection list in project result table by projectId, jobIds and kind
     *
     * @param projectId target projectId
     * @param jobIds    target jobId list
     * @param kind      target result kind
     * @return CountProjection list
     */
    @Query("select new org.secretflow.secretpad.persistence.projection.CountProjection(d.jobId, count(distinct d.upk.refId)) from ProjectResultDO d where d.upk.projectId=:projectId and d.jobId in :jobIds and d.upk.kind=:kind group by d.jobId")
    List<CountProjection> countByJobIds(@Param("projectId") String projectId, @Param("jobIds") List<String> jobIds, @Param("kind") ResultKind kind);

    /**
     * Query project result Optional by refId
     *
     * @param refId target refId
     * @return project result Optional
     */
    @Query("from ProjectResultDO d where d.upk.refId=:refId")
    Optional<ProjectResultDO> findByRefId(@Param("refId") String refId);

    /**
     * Query project result results by nodeId
     *
     * @param nodeId target nodeId
     * @return project result results
     */
    @Query("from ProjectResultDO d where d.upk.nodeId=:nodeId")
    List<ProjectResultDO> findByNodeId(@Param("nodeId") String nodeId);

    /**
     * Query project result Optional by nodeId and refId
     *
     * @param refId target nodeId
     * @param refId target refId
     * @return project result Optional
     */
    @Query("from ProjectResultDO d where d.upk.nodeId=:nodeId and d.upk.refId=:refId")
    Optional<ProjectResultDO> findByNodeIdAndRefId(@Param("nodeId") String nodeId, @Param("refId") String refId);

    /**
     * Query the count of project result results satisfied by nodeId
     *
     * @param nodeId target nodeId
     * @return the count of project result results satisfied by nodeId
     */
    @Query("select count(*) from " +
            "ProjectResultDO pr join ProjectDO p on pr.upk.projectId=p.projectId " +
            "where pr.upk.nodeId=:nodeId")
    Long countByNodeId(@Param("nodeId") String nodeId);

    /**
     * Query project result results by projectId and refId
     *
     * @param projectId target projectId
     * @param refId     target refId
     * @return project result results
     */
    @Query("from ProjectResultDO d where d.upk.projectId=:projectId and d.upk.refId=:refId")
    List<ProjectResultDO> findByProjectIdAndRefId(@Param("projectId") String projectId, @Param("refId") String refId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "delete from project_result where job_id=:jobId and project_id=:projectId")
    void deleteByJobId(@Param("projectId") String projectId, @Param("jobId") String jobId);
}