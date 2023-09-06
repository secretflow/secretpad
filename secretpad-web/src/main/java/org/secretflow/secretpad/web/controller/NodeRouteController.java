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

import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.noderoute.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yutu
 * @date 2023/08/09
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1alpha1/nodeRoute")
public class NodeRouteController {
    private final NodeRouterService nodeRouterService;

    @PostMapping(value = "/create", consumes = "application/json")
    public SecretPadResponse<String> create(@Valid @RequestBody CreateNodeRouterRequest request) {
        return SecretPadResponse.success(nodeRouterService.createNodeRouter(request));
    }

    @PostMapping(value = "/page", consumes = "application/json")
    public SecretPadResponse<SecretPadPageResponse<NodeRouterVO>> page(@Valid @RequestBody PageNodeRouteRequest query) {
        return SecretPadResponse.success(nodeRouterService.queryPage(query, query.of()));
    }

    @PostMapping(value = "/get", consumes = "application/json")
    public SecretPadResponse<NodeRouterVO> get(@Valid @RequestBody RouterIdRequest request) {
        return SecretPadResponse.success(nodeRouterService.getNodeRouter(Long.parseLong(request.getRouterId())));
    }

    @PostMapping(value = "/update", consumes = "application/json")
    public SecretPadResponse<String> update(@Valid @RequestBody UpdateNodeRouterRequest request) {
        nodeRouterService.updateNodeRouter(request);
        return SecretPadResponse.success(request.getRouterId());
    }

    @PostMapping(value = "/listNode")
    public SecretPadResponse<List<NodeVO>> listNode() {
        List<NodeVO> nodes = nodeRouterService.listNode();
        return SecretPadResponse.success(nodes);
    }

    @PostMapping(value = "/refresh", consumes = "application/json")
    public SecretPadResponse<NodeRouterVO> refresh(@Valid @RequestBody RouterIdRequest request) {
        return SecretPadResponse.success(nodeRouterService.refreshRouter(Long.parseLong(request.getRouterId())));
    }

    @PostMapping(value = "/delete", consumes = "application/json")
    public SecretPadResponse<Void> delete(@Valid @RequestBody RouterIdRequest request) {
        nodeRouterService.deleteNodeRouter(Long.parseLong(request.getRouterId()));
        return SecretPadResponse.success();
    }
}