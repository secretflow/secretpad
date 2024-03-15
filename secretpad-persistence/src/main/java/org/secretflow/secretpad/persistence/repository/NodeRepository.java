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

import org.secretflow.secretpad.persistence.entity.NodeDO;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Node repository
 *
 * @author xiaonan
 * @date 2023/5/30
 */
@Repository
public interface NodeRepository extends BaseRepository<NodeDO, String> {

    /**
     * Query node results by nodeId
     *
     * @param nodeId target nodeId
     * @return node results
     */
    @Query("from NodeDO nd where nd.nodeId=:nodeId")
    NodeDO findByNodeId(String nodeId);

    /**
     * Query deleted node result by nodeId
     *
     * @param nodeId target nodeId
     * @return node result optional
     */
    @Query(value = "select id,node_id,name,auth,description,control_node_id,net_address,token,type,mode,master_node_id,is_deleted,gmt_create,gmt_modified from node " +
            "where node_id=:nodeId and is_deleted = 1", nativeQuery = true)
    Optional<NodeDO> findDeletedRecordByNodeId(String nodeId);

    /**
     * Delete node
     *
     * @param nodeId target nodeId
     */
    @Query("delete from NodeDO nd where nd.nodeId=:nodeId")
    void deleteByNodeId(String nodeId);

    List<NodeDO> findByType(String type);

    List<NodeDO> findByModeIn(List<Integer> modes);

    List<NodeDO> findByNodeIdIn(Collection<String> nodeIDS);

    @Query(nativeQuery = true, value = "delete from node where node_id =:nodeId")
    @Modifying
    @Transactional
    void deleteAuthentic(String nodeId);

}