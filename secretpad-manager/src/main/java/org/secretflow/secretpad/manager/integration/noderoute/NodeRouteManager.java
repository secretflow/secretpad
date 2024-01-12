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

package org.secretflow.secretpad.manager.integration.noderoute;

import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.model.NodeRouteDTO;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author yutu
 * @date 2023/08/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeRouteManager extends AbstractNodeRouteManager {

    private final NodeRouteRepository nodeRouteRepository;
    private final NodeRepository nodeRepository;

    private final AbstractNodeManager nodeManager;
    private final DomainRouteServiceGrpc.DomainRouteServiceBlockingStub routeServiceBlockingStub;

    @Value("${secretpad.platform-type}")
    private String platformType;

    @Override
    public void createNodeRouteInKuscia(CreateNodeRouteParam param, NodeDO srcNode, NodeDO dstNode, boolean check) {
        if (DomainRouterConstants.DomainRouterTypeEnum.FullDuplex.name().equals(param.getRouteType())
                && !routeExist(param.getDstNodeId(), param.getSrcNodeId())
        ) {
            createNodeRoute(param, dstNode, srcNode);
        }
        if (check) {
            checkRouteExist(param.getSrcNodeId(), param.getDstNodeId());
        }
        createNodeRoute(param, srcNode, dstNode);
    }


    private void createNodeRouteNotInDb(NodeDO srcNode, NodeDO dstNode) {
        if (checkDomainRouterExists(srcNode.getNodeId(), dstNode.getNodeId())) {
            deleteDomainRouter(srcNode.getNodeId(), dstNode.getNodeId());
        }
        DomainRoute.TokenConfig tokenConfig = buildTokenConfig();
        DomainRoute.RouteEndpoint routeEndpoint = buildRouteEndpoint(dstNode);
        DomainRoute.CreateDomainRouteRequest createDomainRouteRequest =
                DomainRoute.CreateDomainRouteRequest.newBuilder().setAuthenticationType("Token").setTokenConfig(tokenConfig)
                        .setDestination(dstNode.getNodeId()).setEndpoint(routeEndpoint).setSource(srcNode.getNodeId()).build();
        log.info("start create domain route!");
        DomainRoute.CreateDomainRouteResponse createDomainRouteResponse =
                routeServiceBlockingStub.createDomainRoute(createDomainRouteRequest);
        log.info("end create domain route!");
        if (createDomainRouteResponse.getStatus().getCode() != 0) {
            log.error("Create node router failed, code = {}, msg = {}", createDomainRouteResponse.getStatus().getCode(),
                    createDomainRouteResponse.getStatus().getMessage());
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_CREATE_ERROR, "create node router failed.");
        }
        log.info("success end create domain route!");

    }

    @Override
    public void createNodeRoute(CreateNodeRouteParam param, NodeDO srcNode, NodeDO dstNode) {
        createNodeRouteNotInDb(srcNode, dstNode);
    }

    @Override
    public NodeRouteDTO queryNodeRoute(String srcNodeId) {
        return null;
    }

    @Override
    public void deleteNodeRoute(String srcNodeId, String dstNodeId) {
        Optional<NodeRouteDO> nodeRouteDO = nodeRouteRepository.findBySrcNodeIdAndDstNodeId(srcNodeId, dstNodeId);
        nodeRouteDO.ifPresent(this::deleteNodeRoute);
    }

    @Override
    public void deleteNodeRoute(String nodeRouteId) {
        List<String> ids = List.of("1", "2", "3", "4", "5", "6");
        if (ids.contains(nodeRouteId)) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "default route can not delete");
        }
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId(nodeRouteId);
        deleteNodeRoute(nodeRouteDO);
    }

    @Transactional
    public void deleteNodeRoute(NodeRouteDO nodeRouteDO) {
        deleteDomainRouter(nodeRouteDO);
        if (ObjectUtils.isEmpty(nodeRouteDO)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR, "node router do not exit");
        }
        nodeRouteRepository.deleteById(nodeRouteDO.getRouteId());
    }

    @Override
    public void updateNodeRoute(NodeRouteDO nodeRouteDO, NodeDO srcNode, NodeDO dstNode) {

        checkRouteNotExist(nodeRouteDO.getSrcNodeId(), nodeRouteDO.getDstNodeId());
        createNodeRouteInKuscia(
                CreateNodeRouteParam.builder().srcNodeId(nodeRouteDO.getSrcNodeId()).dstNodeId(nodeRouteDO.getDstNodeId())
                        .srcNetAddress(nodeRouteDO.getSrcNetAddress()).dstNetAddress(nodeRouteDO.getDstNetAddress()).
                        routeType(DomainRouterConstants.DomainRouterTypeEnum.HalfDuplex.name()).build(), srcNode, dstNode,
                false);
    }

    @Override
    public DomainRoute.RouteStatus getRouteStatus(String srcNodeId, String dstNodeId) {
        DomainRoute.RouteStatus status = null;
        DomainRoute.QueryDomainRouteResponse response = queryDomainRouter(srcNodeId, dstNodeId);
        log.info("DomainRoute.RouteStatus response {}", response);
        if (response.getStatus().getCode() == 0) {
            status = response.getData().getStatus();
        }
        return status;
    }

    @Override
    public boolean checkNodeRouteExists(String srcNodeId, String dstNodeId) {
        return checkDomainRouterExists(srcNodeId, dstNodeId);
    }

    @Override
    public boolean checkNodeRouteReady(String srcNodeId, String dstNodeId) {
        DomainRoute.QueryDomainRouteResponse response = queryDomainRouter(srcNodeId, dstNodeId);
        if (response.getStatus().getCode() != 0) {
            return false;
        }
        return DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name().equals(response.getData().getStatus().getStatus());
    }

    private DomainRoute.TokenConfig buildTokenConfig() {
        return DomainRoute.TokenConfig.newBuilder().setTokenGenMethod("RSA-GEN").build();
    }

    private DomainRoute.RouteEndpoint buildRouteEndpoint(NodeDO dstNode) {
        String netAddress = dstNode.getNetAddress();
        String[] split = netAddress.split(":");
        String host = split[0];
        int port = Integer.parseInt(split[1]);
        DomainRoute.EndpointPort.Builder builder = DomainRoute.EndpointPort.newBuilder();
        // p2p mode must be https
        if (getPlatformType().equals(PlatformTypeEnum.AUTONOMY)) {
            builder.setPort(port).setName("https").setProtocol("HTTPS").setIsTLS(true).build();
        } else {
            builder.setPort(port).setName("http").setProtocol("HTTP").setIsTLS(true).build();
        }
        // isTLS: true name: http port: 1080 protocol: HTTP
        DomainRoute.EndpointPort endpointPort = builder.build();
        return DomainRoute.RouteEndpoint.newBuilder().setHost(host).addPorts(endpointPort).build();
    }

    private DomainRoute.QueryDomainRouteResponse queryDomainRouter(String srcNodeId, String dstNodeId) {
        DomainRoute.QueryDomainRouteRequest queryDomainRouteRequest =
                DomainRoute.QueryDomainRouteRequest.newBuilder().setSource(srcNodeId).setDestination(dstNodeId).build();
        return routeServiceBlockingStub.queryDomainRoute(queryDomainRouteRequest);
    }

    private boolean checkDomainRouterExists(String srcNodeId, String dstNodeId) {
        DomainRoute.QueryDomainRouteResponse response = queryDomainRouter(srcNodeId, dstNodeId);
        return response.getStatus().getCode() == 0;
    }

    private void deleteDomainRouter(String srcNodeId, String dstNodeId) {
        DomainRoute.DeleteDomainRouteRequest request =
                DomainRoute.DeleteDomainRouteRequest.newBuilder().setSource(srcNodeId).setDestination(dstNodeId).build();
        routeServiceBlockingStub.deleteDomainRoute(request);
    }

    private void deleteDomainRouter(NodeRouteDO nodeRouteDO) {
        // if platformType is AUTONOMY, delete route node id is opposite. later version will delete
        String srcNodeId = getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? nodeRouteDO.getDstNodeId() : nodeRouteDO.getSrcNodeId();
        String dstNodeId = getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? nodeRouteDO.getSrcNodeId() : nodeRouteDO.getDstNodeId();
        DomainRoute.DeleteDomainRouteRequest request =
                DomainRoute.DeleteDomainRouteRequest.newBuilder().setSource(srcNodeId).setDestination(dstNodeId).build();
        DomainRoute.DeleteDomainRouteResponse response = routeServiceBlockingStub.deleteDomainRoute(request);
        if (response.getStatus().getCode() == 11404) {
            nodeRouteRepository.deleteById(nodeRouteDO.getRouteId());
            nodeRouteRepository.flush();
            return;
        }
        if (response.getStatus().getCode() != 0) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_DELETE_ERROR);
        }
    }

    private boolean routeExist(String srcNodeId, String dstNodeId) {
        Optional<NodeRouteDO> optionalNodeRouteDO =
                nodeRouteRepository.findBySrcNodeIdAndDstNodeId(srcNodeId, dstNodeId);
        return optionalNodeRouteDO.isPresent();
    }

    private void checkRouteExist(String srcNodeId, String dstNodeId) {
        if (routeExist(srcNodeId, dstNodeId)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_ALREADY_EXISTS,
                    "route exist " + srcNodeId + "->" + dstNodeId);
        }
    }

    @Override
    public void checkRouteNotExist(String srcNodeId, String dstNodeId) {
        if (!routeExist(srcNodeId, dstNodeId)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist " + srcNodeId + "->" + dstNodeId);
        }
    }

    private PlatformTypeEnum getPlatformType() {
        return PlatformTypeEnum.valueOf(platformType);
    }
}