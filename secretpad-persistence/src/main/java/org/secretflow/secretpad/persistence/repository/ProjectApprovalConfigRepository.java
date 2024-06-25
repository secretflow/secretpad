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


import org.secretflow.secretpad.persistence.entity.ProjectApprovalConfigDO;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @author cml
 * @date 2023/11/27
 */
@Repository
public interface ProjectApprovalConfigRepository extends BaseRepository<ProjectApprovalConfigDO, String> {

    @Query("from ProjectApprovalConfigDO pd join VoteRequestDO vd on pd.voteID=vd.voteID and pd.type=:type")
    List<ProjectApprovalConfigDO> listProjectApprovalConfigByType(@Param("type") String type);

    List<ProjectApprovalConfigDO> findByType(@Param("type") String type);

    Optional<ProjectApprovalConfigDO> findByProjectIdAndType(@Param("projectId") String projectId, @Param("type") String type);

    @Query("from ProjectApprovalConfigDO where projectId=:projectId and type=:type")
    List<ProjectApprovalConfigDO> findArchiveRecordByProjectId(@Param("projectId") String projectId, @Param("type") String type);

    /**
     * Find project list by projectIds and type
     *
     * @param projectIds target projectIds
     * @param type       target type
     * @return project list
     */
    @Query("from ProjectApprovalConfigDO where projectId in :projectIds and type=:type")
    List<ProjectApprovalConfigDO> findByProjectIdsAndType(@Param("projectIds") List<String> projectIds, @Param("type") String type);

    /**
     * find ProjectApprovalConfigDO list by initiator
     *
     * @param srcNodeId target source node id
     * @param dstNodeId target destination node id
     * @return project list
     */
    @Query("select pac from ProjectApprovalConfigDO pac where pac.initiator in (:srcNodeId, :dstNodeId)")
    List<ProjectApprovalConfigDO> findByInitiator(@Param("srcNodeId") String srcNodeId, @Param("dstNodeId") String dstNodeId);
}
