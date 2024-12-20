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

import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.projection.ProjectNodeProjection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Project node repository
 *
 * @author yansi
 * @date 2023/6/8
 */
@Repository
public interface ProjectNodeRepository extends BaseRepository<ProjectNodeDO, ProjectNodeDO.UPK> {
    /**
     * Query project node results by projectId
     *
     * @param projectId target projectId
     * @return project node results
     */
    @Query("from ProjectNodeDO pn where pn.upk.projectId=:projectId")
    List<ProjectNodeDO> findByProjectId(@Param("projectId") String projectId);

    /**
     * Query ProjectNodeProjection list in project node table by projectId
     *
     * @param projectId target projectId
     * @return ProjectNodeProjection list in project node table
     */
    @Query("select new org.secretflow.secretpad.persistence.projection.ProjectNodeProjection(pn, n.name, n.type) from ProjectNodeDO pn join NodeDO n "
            + "on pn.upk.nodeId=n.nodeId and pn.upk.projectId=:projectId")
    List<ProjectNodeProjection> findProjectionByProjectId(@Param("projectId") String projectId);

    /**
     * Query deleted projectNodeProjection list in project node table by projectId for p2p mode
     *
     * @param projectId target projectId
     * @return ProjectNodeProjection list in project node table
     */
    @Query(value = "select * from project_node where project_id=:projectId and is_deleted = 1", nativeQuery = true)
    List<ProjectNodeDO> findDeletedProjectNodesByProjectId(@Param("projectId") String projectId);

    @Query(value = "select distinct node_id from project_node where project_id=:projectId and is_deleted in (0,1)", nativeQuery = true)
    List<String> findProjectNodesByProjectId(@Param("projectId") String projectId);

    /**
     * Query project node results by nodeId
     *
     * @param nodeId target nodeId
     * @return project node results
     */
    @Query("from ProjectNodeDO pn where pn.upk.nodeId=:nodeId")
    List<ProjectNodeDO> findByNodeId(@Param("nodeId") String nodeId);

    /**
     * Query project node results by nodeId list
     *
     * @param nodeIds target nodeId list
     * @return project node results
     */
    @Query("from ProjectNodeDO pn where pn.upk.nodeId in :nodeIds")
    List<ProjectNodeDO> findByNodeIds(@Param("nodeIds") List<String> nodeIds);

    /**
     * Delete all ProjectNode in project node table by projectId
     *
     * @param projectId target projectId
     */
    @Modifying
    @Transactional
    void deleteByUpkProjectId(@Param("projectId") String projectId);

    @Query(nativeQuery = true, value = "delete from project_node")
    @Modifying
    @Transactional
    void deleteAllAuthentic();
}