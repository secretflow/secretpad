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
    public SecretPadResponse<String> createNode(@Valid @RequestBody CreateNodeRequest request) {
        return SecretPadResponse.success(nodeService.createNode(request));
    }

    @PostMapping(value = "/update", consumes = "application/json")
    public SecretPadResponse<String> update(@Valid @RequestBody UpdateNodeRequest request) {
        nodeService.updateNode(request);
        return SecretPadResponse.success(request.getNodeId());
    }

    @PostMapping(value = "/page", consumes = "application/json")
    public SecretPadResponse<SecretPadPageResponse<NodeVO>> page(@Valid @RequestBody PageNodeRequest request) {
        return SecretPadResponse.success(nodeService.queryPage(request, request.of()));
    }

    @PostMapping(value = "/get", consumes = "application/json")
    public SecretPadResponse<NodeVO> get(@Valid @RequestBody NodeIdRequest request) {
        return SecretPadResponse.success(nodeService.getNode(request.getNodeId()));
    }


    @PostMapping(value = "/delete", consumes = "application/json")
    public SecretPadResponse<Void> deleteNode(@Valid @RequestBody NodeIdRequest request) {
        nodeService.deleteNode(request.getNodeId());
        return SecretPadResponse.success();
    }

    @PostMapping(value = "/token", consumes = "application/json")
    public SecretPadResponse<NodeTokenVO> token(@Valid @RequestBody NodeTokenRequest request) {
        NodeTokenVO nodeToken = nodeService.getNodeToken(request.getNodeId(), false);
        return SecretPadResponse.success(nodeToken);
    }

    @PostMapping(value = "/newToken", consumes = "application/json")
    public SecretPadResponse<NodeTokenVO> newToken(@Valid @RequestBody NodeTokenRequest request) {
        NodeTokenVO nodeToken = nodeService.getNodeToken(request.getNodeId(), true);
        return SecretPadResponse.success(nodeToken);
    }

    @PostMapping(value = "/refresh", consumes = "application/json")
    public SecretPadResponse<NodeVO> refresh(@Valid @RequestBody NodeIdRequest request) {
        return SecretPadResponse.success(nodeService.refreshNode(request.getNodeId()));
    }

    /**
     * List node api
     *
     * @return successful SecretPadResponse with node view object list
     */
    @PostMapping(value = "/list")
    public SecretPadResponse<List<NodeVO>> listNode() {
        return SecretPadResponse.success(nodeService.listNodes());
    }

    /**
     * List node result api
     *
     * @return successful SecretPadResponse with node result list view object
     */
    @PostMapping(value = "/result/list", consumes = "application/json")
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
    public SecretPadResponse<NodeResultDetailVO>
    getNodeResultDetail(@Valid @RequestBody GetNodeResultDetailRequest request) {
        return SecretPadResponse.success(nodeService.getNodeResultDetail(request));
    }

}
