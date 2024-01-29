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

import org.secretflow.secretpad.persistence.entity.ProjectReadDataDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project read_data repository
 *
 * @author yutu
 * @date 2023/12/1
 */
@Repository
public interface ProjectReadDtaRepository extends BaseRepository<ProjectReadDataDO, ProjectReadDataDO.UPK> {
    @Query("from ProjectReadDataDO pr where pr.upk.projectId=:projectId and pr.outputId=:outputId")
    List<ProjectReadDataDO> findByProjectIdAndOutputId(@Param("projectId") String projectId, @Param("outputId") String outputId);

    @Query(value = "select id,project_id,output_id,report_id,hash,task,grap_node_id,content,raw,is_deleted,gmt_create,gmt_modified " +
            "from project_read_data where project_id =:projectId and output_id = :outputId order by id desc limit 1", nativeQuery = true)
    ProjectReadDataDO findByProjectIdAndOutputIdLaste(@Param("projectId") String projectId, @Param("outputId") String outputId);

    @Query("from ProjectReadDataDO pr where pr.hash=:hash and pr.grapNodeId =:grapNodeId ")
    List<ProjectReadDataDO> findByHashAndGrapNodeId(@Param("hash") String hash, @Param("grapNodeId") String grapNodeId);
}