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

import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeKusciaParamsDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * project graph node kuscia params repository
 *
 * @author yutu
 * @date 2023/10/26
 */
@Repository
public interface ProjectGraphNodeKusciaParamsRepository extends BaseRepository<ProjectGraphNodeKusciaParamsDO, ProjectGraphNodeKusciaParamsDO.UPK> {

    @Query("from ProjectGraphNodeKusciaParamsDO pd where pd.upk.projectId=:projectId and pd.upk.graphId=:graphId and pd.upk.graphNodeId=:graphNodeId")
    Optional<ProjectGraphNodeKusciaParamsDO> findByUpk(String projectId, String graphId, String graphNodeId);
}