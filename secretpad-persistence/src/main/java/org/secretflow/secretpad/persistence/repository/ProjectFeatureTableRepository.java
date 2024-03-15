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

import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/26
 */
@Repository
public interface ProjectFeatureTableRepository extends BaseRepository<ProjectFeatureTableDO, ProjectFeatureTableDO.UPK> {

    @Query("from ProjectFeatureTableDO pd where pd.upk.nodeId=:nodeId and pd.upk.featureTableId in :featureTableIds")
    List<ProjectFeatureTableDO> findByNodeIdAndFeatureTableIds(@Param("nodeId") String nodeId, @Param("featureTableIds") List<String> featureTableIds);

    @Query("from ProjectFeatureTableDO pd where pd.upk.nodeId=:nodeId and pd.upk.projectId=:projectId")
    List<ProjectFeatureTableDO> findByNodeIdAndProjectId(@Param("nodeId") String nodeId, @Param("projectId") String projectId);
}
