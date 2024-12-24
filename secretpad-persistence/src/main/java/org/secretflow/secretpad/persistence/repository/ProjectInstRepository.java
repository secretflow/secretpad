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

import org.secretflow.secretpad.persistence.entity.ProjectInstDO;
import org.secretflow.secretpad.persistence.projection.ProjectInstProjection;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Project inst repository
 *
 * @author yansi
 * @date 2023/5/31
 */
@Repository
public interface ProjectInstRepository extends BaseRepository<ProjectInstDO, ProjectInstDO.UPK> {

    /**
     * Query project inst results by projectId
     *
     * @param projectId target projectId
     * @return project inst results
     */
    @Query("from ProjectInstDO pi where pi.upk.projectId= :projectId")
    List<ProjectInstDO> findByProjectId(@Param("projectId") String projectId);

    List<ProjectInstDO> findByUpkProjectId(String projectId);

    @Query("select new org.secretflow.secretpad.persistence.projection.ProjectInstProjection(pi, i.name) from ProjectInstDO pi join InstDO i "
            + "on pi.upk.instId=i.instId and pi.upk.projectId= :projectId")
    List<ProjectInstProjection> findProjectionByProjectId(String projectId);

    /**
     * Query project results by instId
     *
     * @param instId
     * @return
     */
    @Query("from ProjectInstDO pi where pi.upk.instId=:instId")
    List<ProjectInstDO> findByInstId(String instId);

    @Modifying
    @Transactional
    void deleteByUpkProjectId(@Param("projectId") String projectId);

}
