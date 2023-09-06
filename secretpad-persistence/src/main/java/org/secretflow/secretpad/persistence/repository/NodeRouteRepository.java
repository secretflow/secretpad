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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
public interface NodeRouteRepository extends JpaRepository<NodeRouteDO, Long>, JpaSpecificationExecutor<NodeRouteDO> {
    /**
     * Query node route results by nodeId of destination
     *
     * @param nodeId the source node of the query, as shown by Alice, is the id of Bob, and bob can access alice
     * @return node route results
     */
    @Query("from NodeRouteDO d where d.dstNodeId=:nodeId")
    List<NodeRouteDO> findByDstNodeId(@Param("nodeId") String nodeId);

    /**
     * Query node route results by nodeId of source
     *
     * @param nodeId the source node of the query, as shown by Alice, is the id of Alice, and bob can access alice
     * @return all node route results
     */
    @Query("from NodeRouteDO d where d.srcNodeId=:nodeId")
    List<NodeRouteDO> findBySrcNodeId(@Param("nodeId") String nodeId);

    @Query("from NodeRouteDO d where d.srcNodeId=:nodeId or d.dstNodeId=:nodeId")
    Set<NodeRouteDO> findBySrcNodeIdAndDstNodeId(@Param("nodeId") String nodeId);

    /**
     * findByRouteId
     *
     * @param routeId id
     * @return NodeRouteDO
     */
    @Query("from NodeRouteDO d where d.id=:routeId")
    NodeRouteDO findByRouteId(@Param("routeId") Long routeId);

    @Query("from NodeRouteDO d where d.srcNodeId=:srcNodeId and d.dstNodeId=:dstNodeId")
    Optional<NodeRouteDO> findBySrcNodeIdAndDstNodeId(@Param("srcNodeId") String srcNodeId,
                                                      @Param("dstNodeId") String dstNodeId);

    void deleteBySrcNodeId(@Param("srcNodeId") String srcNodeId);

    void deleteByDstNodeId(@Param("dstNodeId") String dstNodeId);

    @Query(value = "from NodeRouteDO a join NodeDO b on a.dstNodeId=b.nodeId " +
            "where a.srcNodeId=:nodeId and (a.dstNodeId like %:search% or a.dstNetAddress like %:search% or b.name like %:search%)")
    Page<NodeRouteDO> pageQuery(@Param("nodeId") String nodeId, @Param("search") String search, Pageable pageable);
}