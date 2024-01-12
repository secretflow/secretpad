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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.manager.integration.model.*;

import java.util.List;

/**
 * @author xiaonan
 * @date 2023/05/23
 */
public abstract class AbstractNodeManager {

    /**
     * List all nodes
     *
     * @return NodeDTO list
     */
    public abstract List<NodeDTO> listNode();

    /**
     * List Cooperating Node
     *
     * @param nodeId current node Id
     * @return NodeDTO list
     */
    public abstract List<NodeDTO> listCooperatingNode(String nodeId);

    /**
     * Create node
     *
     * @param param create parma
     * @return nodeId
     */
    public abstract String createNode(CreateNodeParam param);

    /**
     * Create node for p2p mode
     *
     * @param param create parma
     * @return nodeId
     */
    public abstract String createP2pNode(CreateNodeParam param);

    /**
     * Delete node
     *
     * @param nodeId nodeId
     */
    public abstract void deleteNode(String nodeId);

    /**
     * Lists all node routes that target this node
     *
     * @param nodeId nodeId of destination
     * @return node route list
     */
    public abstract List<NodeRouteDTO> findBySrcNodeId(String nodeId);

    /**
     * Lists all node results
     *
     * @param param list result param
     * @return node result list
     */
    public abstract NodeResultListDTO listResult(ListResultParam param);

    /**
     * Find node result
     *
     * @param nodeId       nodeId
     * @param domainDataId domainDataId
     * @return node result DTO
     */
    public abstract NodeResultDTO getNodeResult(String nodeId, String domainDataId);

    /**
     * Refresh node
     *
     * @param nodeId nodeId
     * @return NodeDTO
     */
    public abstract NodeDTO refreshNode(String nodeId);

    /**
     * Get node token
     *
     * @param nodeId  nodeId
     * @param refresh true refresh，false only find unused
     * @return NodeTokenDTO
     */
    public abstract NodeTokenDTO getNodeToken(String nodeId, boolean refresh);

    /**
     * Get node information
     *
     * @param nodeId nodeId
     * @return NodeDTO
     */
    public abstract NodeDTO getNode(String nodeId);

    /**
     * Get node cert
     *
     * @param nodeId target nodeId
     * @return node cert
     */
    public abstract String getCert(String nodeId);

    /**
     * Check if node exists
     *
     * @param nodeId nodeId
     * @return boolean true exist false no exist
     */
    public abstract boolean checkNodeExists(String nodeId);

    /**
     * Check nodeReady grpc code and node status
     *
     * @param nodeId nodeId
     * @return boolean true ready false no ready
     */
    public abstract boolean checkNodeReady(String nodeId);

    /**
     * List tee nodes
     *
     * @return NodeDTO list
     */
    public abstract List<NodeDTO> listTeeNode();

    /**
     * Check srcAddress and dstAddress
     *
     * @param srcNodeAddress net address of platform nodeId
     * @param dstNodeAddress net address of collaborative node
     */
    public abstract void checkSrcAddressAndDstAddressEquals(String srcNodeAddress, String dstNodeAddress);

    /**
     * Check node cert
     *
     * @param nodeId  target nodeId
     * @param request create node param request
     */
    public abstract void checkNodeCert(String nodeId, CreateNodeParam request);

    /**
     * Initial node for p2p mode
     *
     * @param nodeId target nodeId
     */
    public abstract void initialNode(String nodeId);
}
