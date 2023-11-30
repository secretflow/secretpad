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

import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;
import org.secretflow.secretpad.persistence.model.TeeJobKind;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Tee node datatable management repository
 *
 * @author xujiening
 * @date 2023/9/18
 */
public interface TeeNodeDatatableManagementRepository extends BaseRepository<TeeNodeDatatableManagementDO, TeeNodeDatatableManagementDO.UPK> {

    /**
     * Query last tee node datatable management Optional by nodeId, teeNodeId, datatableId and kind
     *
     * @param nodeId      target nodeId
     * @param teeNodeId   target teeNodeId
     * @param datatableId target datatableId
     * @param kind        target kind
     * @return tee node datatable management Optional
     */
    @Query("from TeeNodeDatatableManagementDO t where t.upk.nodeId=:nodeId and t.upk.teeNodeId=:teeNodeId and t.upk.datatableId=:datatableId and t.kind=:kind order by t.gmtCreate desc limit 1")
    Optional<TeeNodeDatatableManagementDO> findFirstByNodeIdAndTeeNodeIdAndDatatableIdAndKind(@Param("nodeId") String nodeId, @Param("teeNodeId") String teeNodeId,
                                                                                              @Param("datatableId") String datatableId, @Param("kind") TeeJobKind kind);

    /**
     * Query by jobId
     *
     * @param jobId tee job id
     * @return tee node datatable management Optional
     */
    @Query("from TeeNodeDatatableManagementDO t where t.upk.jobId=:jobId")
    Optional<TeeNodeDatatableManagementDO> findByJobId(@Param("jobId") String jobId);

    /**
     * Query last tee node datatable management Optional by nodeId, teeNodeId, datatableIds and kind
     *
     * @param nodeId       target nodeId
     * @param teeNodeId    target teeNodeId
     * @param datatableIds target datatableId list
     * @param kind         target kind
     * @return tee node datatable management list
     */
    @Query("from TeeNodeDatatableManagementDO t where t.upk.nodeId=:nodeId and t.upk.teeNodeId=:teeNodeId and t.upk.datatableId in :datatableIds and t.kind=:kind")
    List<TeeNodeDatatableManagementDO> findAllByNodeIdAndTeeNodeIdAndDatatableIdsAndKind(@Param("nodeId") String nodeId, @Param("teeNodeId") String teeNodeId,
                                                                                         @Param("datatableIds") List<String> datatableIds, @Param("kind") TeeJobKind kind);
}
