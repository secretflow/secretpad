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


import org.secretflow.secretpad.persistence.entity.ProjectGraphDomainDatasourceDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project graph domain datasource repository
 *
 * @author yutu
 * @date 2024/05/24
 */
@Repository
public interface ProjectGraphDomainDatasourceRepository extends BaseRepository<ProjectGraphDomainDatasourceDO, ProjectGraphDomainDatasourceDO.UPK> {

    /*
     * Query project graph domain datasource by project id and graph id
     */
    @Query("from ProjectGraphDomainDatasourceDO pd where pd.upk.projectId=:projectId and pd.upk.graphId=:graphId")
    List<ProjectGraphDomainDatasourceDO> findByProjectIdAndGraphId(String projectId, String graphId);

}