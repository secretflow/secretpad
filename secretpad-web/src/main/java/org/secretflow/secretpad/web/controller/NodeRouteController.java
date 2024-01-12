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

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.RouterIdRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

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

    private final NodeService nodeService;

    /**
     * page request for query domain route
     *
     * @param query dst domain name id address
     * @return page of domainRoute
     */
    @PostMapping(value = "/page", consumes = "application/json")
    @DataResource(field = "nodeId", resourceType = DataResourceTypeEnum.NODE_ID)
    @ApiResource(code = ApiResourceCodeConstants.NODE_ROUTE_PAGE)
    public SecretPadResponse<SecretPadPageResponse<NodeRouterVO>> page(@Valid @RequestBody PageNodeRouteRequest query) {
        return SecretPadResponse.success(nodeRouterService.queryPage(query, query.of()));
    }

    /**
     * get domainRoute info by routeId
     *
     * @param request domainRouteId
     * @return domainRoute info
     */
    @PostMapping(value = "/get", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_ROUTE_GET)
    public SecretPadResponse<NodeRouterVO> get(@Valid @RequestBody RouterIdRequest request) {
        return SecretPadResponse.success(nodeRouterService.getNodeRouter(request.getRouterId()));
    }

    /**
     * update domainRoute info by routeId
     *
     * @param request dst domainRoute address and domainRouteId
     * @return domainRouteId
     */
    @PostMapping(value = "/update", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_ROUTE_UPDATE)
    public SecretPadResponse<String> update(@Valid @RequestBody UpdateNodeRouterRequest request) {
        nodeRouterService.updateNodeRouter(request);
        return SecretPadResponse.success(request.getRouterId());
    }

    /**
     * select list domain for dst create dst domainRoute
     *
     * @return list of domain info
     */
    @PostMapping(value = "/listNode")
    @ApiResource(code = ApiResourceCodeConstants.NODE_ROUTE_LIST_NODE)
    public SecretPadResponse<List<NodeVO>> listNode() {
        List<NodeVO> nodes = nodeService.listNodes();
        return SecretPadResponse.success(nodes);
    }

    /**
     * get domainRoute now stats
     *
     * @param request routeId
     * @return RouteInfo
     */
    @PostMapping(value = "/refresh", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_ROUTE_REFRESH)
    public SecretPadResponse<NodeRouterVO> refresh(@Valid @RequestBody RouterIdRequest request) {
        return SecretPadResponse.success(nodeRouterService.refreshRouter(request.getRouterId()));
    }

    /**
     * delete domainRoute only for this routeId
     *
     * @param request routeId
     * @return void
     */
    @PostMapping(value = "/delete", consumes = "application/json")
    @ApiResource(code = ApiResourceCodeConstants.NODE_ROUTE_DELETE)
    public SecretPadResponse<Void> delete(@Valid @RequestBody RouterIdRequest request) {
        nodeRouterService.deleteNodeRouter(request.getRouterId());
        return SecretPadResponse.success();
    }
}