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

import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Project graph repository
 *
 * @author yansi
 * @date 2023/5/31
 */
@Repository
public interface ProjectGraphRepository extends JpaRepository<ProjectGraphDO, ProjectGraphDO.UPK> {
    /**
     * Query project graph results by projectId
     *
     * @param projectId target projectId
     * @return project graph results
     */
    @Query("from ProjectGraphDO pd where pd.upk.projectId=:projectId")
    List<ProjectGraphDO> findByProjectId(@Param("projectId") String projectId);

    /**
     * Query project graph result by graphId and projectId
     *
     * @param graphId   target graphId
     * @param projectId target projectId
     * @return project graph result
     */
    @Query("from ProjectGraphDO pd where pd.upk.graphId=:graphId and pd.upk.projectId=:projectId")
    Optional<ProjectGraphDO> findByGraphId(@Param("graphId") String graphId, @Param("projectId") String projectId);

    /**
     * Query the count of project graph results satisfied by projectId
     *
     * @param projectId target projectId
     * @return the count of project graph results satisfied by projectId
     */
    @Query("select count(*) from ProjectGraphDO pd where pd.upk.projectId=:projectId")
    Integer countByProjectId(@Param("projectId") String projectId);
}