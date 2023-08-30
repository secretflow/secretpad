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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JpaQueryHelper;
import org.secretflow.secretpad.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.model.UpdateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;

/**
 * @author yutu
 * @date 2023/08/04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeRouterServiceImpl implements NodeRouterService {

    private final AbstractNodeRouteManager nodeRouteManager;
    private final NodeRouteRepository nodeRouteRepository;
    private final NodeService nodeService;

    @Override
    public String createNodeRouter(CreateNodeRouterRequest request) {
        return String.valueOf(nodeRouteManager.createNodeRoute(CreateNodeRouteParam.builder()
                .srcNodeId(request.getSrcNodeId()).dstNodeId(request.getDstNodeId()).routeType(request.getRouteType())
                .srcNetAddress(request.getSrcNetAddress()).dstNetAddress(request.getDstNetAddress()).build(), true));
    }

    @Override
    public SecretPadPageResponse<NodeRouterVO> queryPage(PageNodeRouteRequest request, Pageable pageable) {
        Page<NodeRouteDO> page = nodeRouteRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> JpaQueryHelper.getPredicate(root, request, criteriaBuilder),
                pageable);
        SecretPadPageResponse<NodeRouterVO> data = SecretPadPageResponse.toPage(page.map(NodeRouterVO::fromDo));
        data.getList().forEach(d -> {
            d.setSrcNode(nodeService.getNode(d.getSrcNodeId()));
            d.setDstNode(nodeService.getNode(d.getDstNodeId()));
            DomainRoute.RouteStatus routeStatus = nodeRouteManager.getRouteStatus(d.getSrcNodeId(), d.getDstNodeId());
            if (!ObjectUtils.isEmpty(routeStatus)) {
                d.setStatus(routeStatus.getStatus());
            }
        });
        return data;
    }

    @Override
    public void updateNodeRouter(UpdateNodeRouterRequest request) {
        nodeRouteManager
                .updateNodeRoute(UpdateNodeRouteParam.builder().nodeRouteId(Long.parseLong(request.getRouterId()))
                        .srcNetAddress(request.getSrcNetAddress()).dstNetAddress(request.getDstNetAddress()).build());
    }

    @Override
    public NodeRouterVO getNodeRouter(Long routeId) {
        NodeRouteDO byRouteId = nodeRouteRepository.findByRouteId(routeId);
        if (ObjectUtils.isEmpty(byRouteId)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR, "route not exist");
        }
        NodeRouterVO nodeRouterVO = NodeRouterVO.fromDo(byRouteId);
        nodeRouterVO.setSrcNode(nodeService.getNode(nodeRouterVO.getSrcNodeId()));
        nodeRouterVO.setDstNode(nodeService.getNode(nodeRouterVO.getDstNodeId()));
        DomainRoute.RouteStatus routeStatus =
                nodeRouteManager.getRouteStatus(nodeRouterVO.getSrcNodeId(), nodeRouterVO.getDstNodeId());
        if (!ObjectUtils.isEmpty(routeStatus)) {
            nodeRouterVO.setStatus(routeStatus.getStatus());
        }
        return nodeRouterVO;
    }

    @Override
    public List<NodeVO> listNode() {
        return nodeService.listNodes();
    }

    @Override
    public NodeRouterVO refreshRouter(Long routerId) {
        return getNodeRouter(routerId);
    }

    @Override
    public void refreshRouters(Set<Long> routerIds) {
        if (CollectionUtils.isEmpty(routerIds)) {
            nodeRouteRepository.findAll().forEach(nodeRouteDO -> routerIds.add(nodeRouteDO.getId()));
        }
        routerIds.forEach(this::getNodeRouter);
    }

    @Override
    public void deleteNodeRouter(Long routerId) {
        nodeRouteManager.deleteNodeRoute(routerId);
    }
}