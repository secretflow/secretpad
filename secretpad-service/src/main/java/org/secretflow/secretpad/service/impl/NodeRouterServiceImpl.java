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

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.model.UpdateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;
import org.secretflow.secretpad.service.util.PushToCenterUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
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

    private final NodeRepository nodeRepository;

    private final EnvService envService;

    @Value("${secretpad.gateway}")
    private String kusciaLiteGateway;
    @Value("${secretpad.center-platform-service}")
    private String routeHeader;
    @Value("${secretpad.node-id}")
    private String nodeId;

    @Override
    public void createNodeRouter(CreateNodeRouterRequest request) {
        CreateNodeRouteParam param = CreateNodeRouteParam.builder()
                .srcNodeId(request.getSrcNodeId())
                .dstNodeId(request.getDstNodeId())
                .routeType(request.getRouteType())
                .srcNetAddress(replaceNetAddressProtocol(request.getSrcNetAddress()))
                .dstNetAddress(replaceNetAddressProtocol(request.getDstNetAddress()))
                .build();
        NodeDO srcNode = nodeRepository.findByNodeId(param.getSrcNodeId());
        NodeDO dstNode = nodeRepository.findByNodeId(param.getDstNodeId());
        //checkNode(srcNode);
        //checkNode(dstNode);
        if (StringUtils.isNotEmpty(param.getSrcNetAddress())) {
            srcNode.setNetAddress(param.getSrcNetAddress());
        }
        if (StringUtils.isNotEmpty(param.getDstNetAddress())) {
            dstNode.setNetAddress(param.getDstNetAddress());
        }
        log.info("start create route in kusica");
        nodeRouteManager.createNodeRouteInKuscia(param, srcNode, dstNode, false);
        log.info("start create route in db");
        createNodeRouteInDB(srcNode, dstNode);
    }

    public Long createNodeRouteInDB(NodeDO srcNode, NodeDO dstNode) {
        Optional<NodeRouteDO> optionalNodeRouteDO =
                nodeRouteRepository.findBySrcNodeIdAndDstNodeId(srcNode.getNodeId(), dstNode.getNodeId());
        NodeRouteDO nodeRouteDO;
        if (optionalNodeRouteDO.isEmpty()) {
            nodeRouteDO = NodeRouteDO.builder().srcNodeId(srcNode.getNodeId()).dstNodeId(dstNode.getNodeId()).routeId(srcNode.getNodeId() + "__" + dstNode.getNodeId()).build();
        } else {
            nodeRouteDO = optionalNodeRouteDO.get();
        }
        nodeRouteDO.setSrcNetAddress(srcNode.getNetAddress());
        nodeRouteDO.setDstNetAddress(dstNode.getNetAddress());
        if (!envService.isCenter()) {
            nodeRouteDO.setGmtCreate(null);
            nodeRouteDO.setGmtModified(null);
            VoteSyncRequest voteSyncRequest = VoteSyncRequest.builder().syncDataType(VoteSyncTypeEnum.NODE_ROUTE.name()).projectNodesInfo(nodeRouteDO).build();
            PushToCenterUtil.dataPushToCenter(voteSyncRequest);
        } else {
            log.info("this is center ,save node route ! {}", nodeRouteDO);
            nodeRouteDO = nodeRouteRepository.save(nodeRouteDO);
            log.info("center success save node route ! {}", nodeRouteDO);
        }
        return nodeRouteDO.getId();
    }

    @Override
    public SecretPadPageResponse<NodeRouterVO> queryPage(PageNodeRouteRequest request, Pageable pageable) {
        Page<NodeRouteDO> page = nodeRouteRepository.pageQuery(request.getNodeId(), "%".concat(request.getSearch()).concat("%"), pageable);
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
        UpdateNodeRouteParam param = UpdateNodeRouteParam.builder()
                .nodeRouteId(request.getRouterId())
                .srcNetAddress(replaceNetAddressProtocol(request.getSrcNetAddress()))
                .dstNetAddress(replaceNetAddressProtocol(request.getDstNetAddress()))
                .build();
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId(param.getNodeRouteId());
        if (org.apache.commons.lang3.ObjectUtils.isEmpty(nodeRouteDO)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist " + param.getNodeRouteId());
        }
        NodeDO srcNode = nodeRepository.findByNodeId(nodeRouteDO.getSrcNodeId());
        NodeDO dstNode = nodeRepository.findByNodeId(nodeRouteDO.getDstNodeId());
        checkDataPermissions(dstNode.getNodeId());
        nodeRouteManager.updateNodeRoute(nodeRouteDO, srcNode, dstNode);
        if (StringUtils.isNotEmpty(param.getDstNetAddress())) {
            dstNode.setNetAddress(param.getDstNetAddress());
        }
        log.info("updateNodeRouter {} {}", srcNode, dstNode);
        createNodeRouteInDB(srcNode, dstNode);
    }

    @Override
    public NodeRouterVO getNodeRouter(String routeId) {
        NodeRouteDO byRouteId = nodeRouteRepository.findByRouteId(routeId);
        if (ObjectUtils.isEmpty(byRouteId)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR, "route not exist");
        }
        checkDataPermissions(byRouteId.getDstNodeId());
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
    public NodeRouterVO refreshRouter(String routerId) {
        return getNodeRouter(routerId);
    }

    @Override
    public void refreshRouters(Set<String> routerIds) {
        if (CollectionUtils.isEmpty(routerIds)) {
            nodeRouteRepository.findAll().forEach(nodeRouteDO -> routerIds.add(nodeRouteDO.getRouteId()));
        }
        routerIds.forEach(this::getNodeRouter);
    }

    @Override
    public void deleteNodeRouter(String routerId) {
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId(routerId);
        if (org.apache.commons.lang3.ObjectUtils.isEmpty(nodeRouteDO)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist " + routerId);
        }
        checkDataPermissions(nodeRouteDO.getDstNodeId());
        nodeRouteManager.deleteNodeRoute(routerId);
    }

    private String replaceNetAddressProtocol(String netAddress) {
        try {
            URL url = new URL(netAddress);
            return String.format("%s:%d", url.getHost(), url.getPort());
        } catch (MalformedURLException e) {
            log.warn("replaceNetAddressProtocol str cast URL error");
            return netAddress;
        }
    }

    private void checkDataPermissions(String nodeId) {
        UserContextDTO user = UserContext.getUser();
        if (user.getPlatformType().equals(PlatformTypeEnum.EDGE)) {
            if (!user.getOwnerId().equals(nodeId)) {
                throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "no Permissions");
            }
        }
    }
}