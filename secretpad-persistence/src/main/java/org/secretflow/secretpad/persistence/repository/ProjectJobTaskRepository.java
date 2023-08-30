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

import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Project job task repository
 *
 * @author yansi
 * @date 2023/6/8
 */
@Repository
public interface ProjectJobTaskRepository extends JpaRepository<ProjectTaskDO, ProjectTaskDO.UPK> {
    /**
     * Query the latest project job task result by projectId and graphNodeId
     *
     * @param projectId   target projectId
     * @param graphNodeId target graphNodeId
     * @return the latest project job task result
     */
    @Query(value = "select * from project_job_task where project_id=:projectId and graph_node_id=:graphNodeId order by id desc limit 1", nativeQuery = true)
    Optional<ProjectTaskDO> findLatestTasks(@Param("projectId") String projectId, @Param("graphNodeId") String graphNodeId);

    /**
     * Query project job task results by projectId, graphNodeId and status
     *
     * @param projectId   target projectId
     * @param graphNodeId target graphNodeId
     * @param status      target status
     * @return project job task results
     */
    @Query(value = "from ProjectTaskDO where upk.projectId=:projectId and graphNodeId=:graphNodeId and status=:status")
    List<ProjectTaskDO> findByStatus(@Param("projectId") String projectId, @Param("graphNodeId") String graphNodeId, @Param("status") GraphNodeTaskStatus status);

    /**
     * Query project job task results by status
     *
     * @param status target status
     * @return project job task results
     */
    List<ProjectTaskDO> findByStatus(GraphNodeTaskStatus status);
}
