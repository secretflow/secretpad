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

import org.secretflow.secretpad.persistence.entity.ProjectFedTableDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project federal table repository
 *
 * @author xiaonan
 * @date 2023/5/30
 */
@Repository
public interface ProjectFedTableRepository extends BaseRepository<ProjectFedTableDO, ProjectFedTableDO.UPK> {
    /**
     * Query project federal table results by projectId
     *
     * @param projectId target projectId
     * @return project federal table results
     */
    @Query("from ProjectFedTableDO pd where pd.upk.projectId=:projectId")
    List<ProjectFedTableDO> findByProjectId(@Param("projectId") String projectId);
}