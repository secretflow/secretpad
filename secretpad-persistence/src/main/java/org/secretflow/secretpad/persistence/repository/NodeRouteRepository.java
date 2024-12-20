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

import org.secretflow.secretpad.persistence.entity.NodeRouteDO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Node route repository
 *
 * @author xiaonan
 * @date 2023/5/30
 */
@Repository
public interface NodeRouteRepository extends BaseRepository<NodeRouteDO, String> {
    /**
     * Query node route results by nodeId of destination
     *
     * @param nodeId the source node of the query, as shown by Alice, is the id of Bob, and bob can access alice
     * @return node route results
     */
    @Query("from NodeRouteDO d where d.dstNodeId=:nodeId")
    List<NodeRouteDO> findByDstNodeId(@Param("nodeId") String nodeId);

    List<NodeRouteDO> findByDstNodeIdIn(Collection<String> nodeIds);

    /**
     * Query node route results by nodeId of source
     *
     * @param nodeId the source node of the query, as shown by Alice, is the id of Alice, and bob can access alice
     * @return all node route results
     */
    @Query("from NodeRouteDO d where d.srcNodeId=:nodeId")
    List<NodeRouteDO> findBySrcNodeId(@Param("nodeId") String nodeId);

    @Query("from NodeRouteDO d where d.srcNodeId=:nodeId or d.dstNodeId=:nodeId")
    Set<NodeRouteDO> findBySrcNodeIdOrDstNodeId(@Param("nodeId") String nodeId);

    /**
     * findByRouteId
     *
     * @param routeId id
     * @return NodeRouteDO
     */
    @Query("from NodeRouteDO d where d.routeId=:routeId")
    NodeRouteDO findByRouteId(@Param("routeId") String routeId);

    @Query("from NodeRouteDO d where d.srcNodeId=:srcNodeId and d.dstNodeId=:dstNodeId")
    Optional<NodeRouteDO> findBySrcNodeIdAndDstNodeId(@Param("srcNodeId") String srcNodeId,
                                                      @Param("dstNodeId") String dstNodeId);

    /**
     * Delete node route by srcNodeId
     *
     * @param srcNodeId source node id
     */
    @Modifying
    @Transactional
    void deleteBySrcNodeId(@Param("srcNodeId") String srcNodeId);

    /**
     * Delete node route by dstNodeId
     *
     * @param dstNodeId destination node id
     */
    @Modifying
    @Transactional
    void deleteByDstNodeId(@Param("dstNodeId") String dstNodeId);

    @Query(value = "from NodeRouteDO a join NodeDO b on a.srcNodeId=b.nodeId " +
            "where a.dstNodeId IN (:nodeIds)  and (a.srcNodeId like :search or a.dstNetAddress like :search or b.name like :search)")
    Page<NodeRouteDO> pageQuery(@Param("nodeIds") Collection<String> nodeIds, @Param("search") String search, Pageable pageable);
}