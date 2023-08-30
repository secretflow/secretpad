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
     * Create a node
     *
     * @param request create node request
     * @return nodeId
     */
    String createNode(CreateNodeRequest request);

    /**
     * Delete a node
     *
     * @param nodeId target nodeId
     */
    void deleteNode(String nodeId);

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
     * 查询节点
     *
     * @param nodeId 节点id
     * @return 节点视图
     */
    NodeVO getNode(String nodeId);

    void updateNode(UpdateNodeRequest request);

    SecretPadPageResponse<NodeVO> queryPage(PageNodeRequest request, Pageable pageable);

    NodeVO refreshNode(String nodeId);

    NodeTokenVO getNodeToken(String nodeId, boolean refresh);

}
