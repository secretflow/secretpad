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

import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project datatable repository
 *
 * @author xiaonan
 * @date 2023/5/30
 */
@Repository
public interface ProjectDatatableRepository extends BaseRepository<ProjectDatatableDO, ProjectDatatableDO.UPK> {
    /**
     * Query project datatable results by projectId
     *
     * @param projectId target projectId
     * @return project datatable results
     */
    @Query("from ProjectDatatableDO pd where pd.upk.projectId=:projectId")
    List<ProjectDatatableDO> findByProjectId(@Param("projectId") String projectId);

    /**
     * Query project datatable results by projectId and datatableId
     *
     * @param projectId   target projectId
     * @param datatableId target datatableId
     * @return project datatable results
     */
    @Query("from ProjectDatatableDO pd where pd.upk.projectId=:projectId and pd.upk.datatableId=:datatableId")
    List<ProjectDatatableDO> findByDatableId(@Param("projectId") String projectId, @Param("datatableId") String datatableId);

    /**
     * Query project datatable unique primary key results by projectId and source
     *
     * @param projectId target projectId
     * @param source    target project datatable source
     * @return project datatable unique primary key results
     */
    @Query("select pd.upk from ProjectDatatableDO pd where pd.upk.projectId=:projectId and pd.source=:source")
    List<ProjectDatatableDO.UPK> findUpkByProjectId(@Param("projectId") String projectId, @Param("source") ProjectDatatableDO.ProjectDatatableSource source);

    /**
     * Batch query project datatable results by projectId and datatableIds
     *
     * @param nodeId       target projectId
     * @param datatableIds target datatableId list
     * @return project datatable results
     */
    @Query("from ProjectDatatableDO pd where pd.upk.nodeId=:nodeId and pd.upk.datatableId in :datatableIds")
    List<ProjectDatatableDO> authProjectDatatablesByDatatableIds(@Param("nodeId") String nodeId, @Param("datatableIds") List<String> datatableIds);
}