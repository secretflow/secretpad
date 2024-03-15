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

import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project graph node repository
 *
 * @author yansi
 * @date 2023/5/31
 */
@Repository
public interface ProjectGraphNodeRepository extends BaseRepository<ProjectGraphNodeDO, ProjectGraphNodeDO.UPK> {

    @Query(value = "select id,project_id,graph_id,graph_node_id,code_name,label,x,y,inputs,outputs,node_def,is_deleted,gmt_create,gmt_modified" +
            " from project_graph_node where project_id = ?1 and graph_id=?2", nativeQuery = true)
    List<ProjectGraphNodeDO> findByProjectIdAndGraphId(String projectId, String graphId);

    @Query("from ProjectGraphNodeDO pgn where pgn.upk.projectId = :#{#projectId} and pgn.upk.graphId = :#{#graphId} and pgn.codeName = 'read_data/datatable'")
    List<ProjectGraphNodeDO> findReadTableByProjectIdAndGraphId(String projectId, String graphId);
}
