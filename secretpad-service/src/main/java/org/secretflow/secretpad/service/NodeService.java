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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.node.p2p.P2pCreateNodeRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Node service interface
 *
 * @author xiaonan
 * @date 2023/5/4
 */
public interface NodeService {

    /**
     * Lists all current nodes
     *
     * @return node view object list
     */
    List<NodeVO> listNodes();

    /**
     * List all Node base info without oneself node.
     *
     * @param oneselfNodeId oneselfNodeId
     * @return List<NodeBaseInfoVO>
     */
    List<NodeBaseInfoVO> listOtherNodeBaseInfo(String oneselfNodeId);

    /**
     * Create a node
     *
     * @param request create node request
     * @return nodeId
     */
    String createNode(CreateNodeRequest request);

    /**
     * Create a node for p2p mode
     *
     * @param request create node request
     * @return nodeId
     */
    String createP2pNode(P2pCreateNodeRequest request);


    /**
     * Delete a node
     *
     * @param nodeId target nodeId
     */
    void deleteNode(String nodeId);

    /**
     * Delete a node for p2p mode
     *
     * @param routerId target routerId
     */
    void deleteP2pNode(String routerId);

    void deleteP2pNodeRoute(String routerId);

    /**
     * List all node result products
     *
     * @param request list node result request
     * @return All node results list view object
     */
    AllNodeResultsListVO listAllNodeResults(ListNodeResultRequest request);

    /**
     * List the node result products
     *
     * @param request list node result request
     * @return node result product list view object
     */
    NodeResultsListVO listResults(ListNodeResultRequest request);

    /**
     * Query node result detail
     *
     * @param request get node result detail request
     * @return node result detail view object
     */
    NodeResultDetailVO getNodeResultDetail(GetNodeResultDetailRequest request);

    /**
     * get node info
     *
     * @param nodeId node id
     * @return node info
     */
    NodeVO getNode(String nodeId);

    /**
     * update node info
     *
     * @param request node info
     */
    void updateNode(UpdateNodeRequest request);

    /**
     * page of node
     *
     * @param request  page request param
     * @param pageable jpa page param
     * @return page node info
     */
    SecretPadPageResponse<NodeVO> queryPage(PageNodeRequest request, Pageable pageable);

    /**
     * get now node stats
     *
     * @param nodeId node id
     * @return node info
     */
    NodeVO refreshNode(String nodeId);

    /**
     * get node deploy token used or unused
     *
     * @param nodeId  node id
     * @param refresh true: unused false used
     * @return node info
     */
    NodeTokenVO getNodeToken(String nodeId, boolean refresh);

    /**
     * List tee nodes
     *
     * @return NodeDTO list
     */
    List<NodeVO> listTeeNode();

    /**
     * List Cooperating Node
     *
     * @param nodeId current node Id
     * @return node view object list
     */
    List<NodeVO> listCooperatingNode(String nodeId);

    /**
     * Initial node for p2p mode
     */
    void initialNode(String instName);

    void updateNodeMasterNodeId(String nodeId);

    List<String> findInstIdsForNodes(List<String> nodeIds);
}
