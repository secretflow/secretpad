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
package org.secretflow.secretpad.web.controller.p2p;

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.node.p2p.P2pCreateNodeRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Node controller for p2p
 *
 * @author xujiening
 * @date 2023/11/30
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1alpha1/p2p/node")
public class P2pNodeController {

    private final NodeService nodeService;

    /**
     * Create a new node api
     *
     * @param request create node request
     * @return successful SecretPadResponse with nodeId
     */
    @PostMapping(value = "/create", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_CREATE)
    public SecretPadResponse<String> createP2pNode(@Valid @RequestBody P2pCreateNodeRequest request) {
        return SecretPadResponse.success(nodeService.createP2pNode(request));
    }

    /**
     * Delete collaborative node via router id
     *
     * @param request delete collaborative node request
     * @return void
     */
    @PostMapping(value = "/delete", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_DELETE)
    public SecretPadResponse<Void> deleteP2pNode(@Valid @RequestBody RouterIdRequest request) {
        nodeService.deleteP2pNode(request.getRouterId());
        return SecretPadResponse.success();
    }
}
