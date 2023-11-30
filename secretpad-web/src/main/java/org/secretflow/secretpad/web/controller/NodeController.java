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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.node.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xiaonan
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1alpha1/node")
public class NodeController {

    private final NodeService nodeService;

    /**
     * Create a new node api
     *
     * @param request create node request
     * @return successful SecretPadResponse with nodeId
     */
    @PostMapping(value = "/create", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_CREATE)
    public SecretPadResponse<String> createNode(@Valid @RequestBody CreateNodeRequest request) {
        return SecretPadResponse.success(nodeService.createNode(request));
    }

    /**
     * update node info only address
     *
     * @param request update node info
     * @return nodeId
     */
    @PostMapping(value = "/update", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_UPDATE)
    public SecretPadResponse<String> update(@Valid @RequestBody UpdateNodeRequest request) {
        nodeService.updateNode(request);
        return SecretPadResponse.success(request.getNodeId());
    }

    /**
     * page node info
     *
     * @param request page params
     * @return page of node
     */
    @PostMapping(value = "/page", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_PAGE)
    public SecretPadResponse<SecretPadPageResponse<NodeVO>> page(@Valid @RequestBody PageNodeRequest request) {
        return SecretPadResponse.success(nodeService.queryPage(request, request.of()));
    }

    /**
     * get node info by nodeId
     *
     * @param request nodeId
     * @return node info
     */
    @PostMapping(value = "/get", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_GET)
    public SecretPadResponse<NodeVO> get(@Valid @RequestBody NodeIdRequest request) {
        return SecretPadResponse.success(nodeService.getNode(request.getNodeId()));
    }


    /**
     * delete node by node id
     *
     * @param request node id
     * @return void
     */
    @PostMapping(value = "/delete", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_DELETE)
    public SecretPadResponse<Void> deleteNode(@Valid @RequestBody NodeIdRequest request) {
        nodeService.deleteNode(request.getNodeId());
        return SecretPadResponse.success();
    }

    /**
     * get exist token maybe used or unused
     *
     * @param request node id
     * @return token
     */
    @PostMapping(value = "/token", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_TOKEN)
    public SecretPadResponse<NodeTokenVO> token(@Valid @RequestBody NodeTokenRequest request) {
        NodeTokenVO nodeToken = nodeService.getNodeToken(request.getNodeId(), false);
        return SecretPadResponse.success(nodeToken);
    }

    /**
     * get new token  unused
     *
     * @param request node id
     * @return token
     */
    @PostMapping(value = "/newToken", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_NEW_TOKEN)
    public SecretPadResponse<NodeTokenVO> newToken(@Valid @RequestBody NodeTokenRequest request) {
        NodeTokenVO nodeToken = nodeService.getNodeToken(request.getNodeId(), true);
        return SecretPadResponse.success(nodeToken);
    }

    /**
     * get now node stats
     *
     * @param request node id
     * @return node info
     */
    @PostMapping(value = "/refresh", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_REFRESH)
    public SecretPadResponse<NodeVO> refresh(@Valid @RequestBody NodeIdRequest request) {
        return SecretPadResponse.success(nodeService.refreshNode(request.getNodeId()));
    }

    /**
     * List node api
     *
     * @return successful SecretPadResponse with node view object list
     */
    @PostMapping(value = "/list")
    @ApiResource(code = ApiResourceCodeConstants.NODE_LIST)
    public SecretPadResponse<List<NodeVO>> listNode() {
        return SecretPadResponse.success(nodeService.listNodes());
    }

    /**
     * List node result api
     *
     * @return successful SecretPadResponse with node result list view object
     */
    @PostMapping(value = "/result/list", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_RESULT_LIST)
    public SecretPadResponse<NodeResultsListVO> listResults(@Valid @RequestBody ListNodeResultRequest request) {
        return SecretPadResponse.success(nodeService.listResults(request));
    }

    /**
     * Query node result detail api
     *
     * @param request get node result detail request
     * @return successful SecretPadResponse with node result detail view object
     */
    @PostMapping(value = "/result/detail", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_RESULT_DETAIL)
    public SecretPadResponse<NodeResultDetailVO>
    getNodeResultDetail(@Valid @RequestBody GetNodeResultDetailRequest request) {
        return SecretPadResponse.success(nodeService.getNodeResultDetail(request));
    }

}
