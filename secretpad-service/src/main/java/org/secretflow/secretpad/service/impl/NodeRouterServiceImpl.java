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
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.model.CreateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.model.UpdateNodeRouteParam;
import org.secretflow.secretpad.manager.integration.node.NodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.NodeRouterService;
import org.secretflow.secretpad.service.enums.VoteSyncTypeEnum;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.datasync.vote.DbSyncRequest;
import org.secretflow.secretpad.service.model.node.NodeVO;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;
import org.secretflow.secretpad.service.util.DbSyncUtil;

import java.util.*;
import java.util.stream.Collectors;

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

/**
 * @author yutu
 * @date 2023/08/04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NodeRouterServiceImpl implements NodeRouterService {

    private final AbstractNodeRouteManager nodeRouteManager;
    private final NodeManager nodeManager;
    private final NodeRouteRepository nodeRouteRepository;

    private final NodeRepository nodeRepository;
    private final ProjectNodeRepository projectNodeRepository;

    private final ProjectApprovalConfigRepository projectApprovalConfigRepository;
    private final ProjectJobRepository projectJobRepository;

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
                .srcNetAddress(request.getSrcNetAddress())
                .dstNetAddress(request.getDstNetAddress())
                .build();
        NodeDO srcNode = nodeRepository.findByNodeId(param.getSrcNodeId());
        NodeDO dstNode = nodeRepository.findByNodeId(param.getDstNodeId());
        if (StringUtils.isNotEmpty(param.getSrcNetAddress())) {
            srcNode.setNetAddress(param.getSrcNetAddress());
        }
        if (StringUtils.isNotEmpty(param.getDstNetAddress())) {
            dstNode.setNetAddress(param.getDstNetAddress());
        }
        log.info("start create route in kusica");
        nodeRouteManager.createNodeRouteInKuscia(param, srcNode, dstNode, false);
        log.info("start create route in db");
        // if platformType is AUTONOMY, save opposite result this version. later version will delete
        if (envService.getPlatformType().equals(PlatformTypeEnum.AUTONOMY)) {
            createNodeRouteInDB(dstNode, srcNode);
        } else {
            createNodeRouteInDB(srcNode, dstNode);
        }
    }

    public Long createNodeRouteInDB(NodeDO srcNode, NodeDO dstNode) {
        Optional<NodeRouteDO> optionalNodeRouteDO =
                nodeRouteRepository.findBySrcNodeIdAndDstNodeId(srcNode.getNodeId(), dstNode.getNodeId());
        NodeRouteDO nodeRouteDO;
        if (optionalNodeRouteDO.isEmpty()) {
            nodeRouteDO = NodeRouteDO.builder().srcNodeId(srcNode.getNodeId()).dstNodeId(dstNode.getNodeId()).routeId(srcNode.getNodeId() + "__" + dstNode.getNodeId()).build();
        } else {
            nodeRouteDO = optionalNodeRouteDO.get();
            nodeRouteDO.setGmtModified(DateTimes.utcFromRfc3339(DateTimes.nowRfc3339()));
        }
        nodeRouteDO.setSrcNetAddress(srcNode.getNetAddress());
        nodeRouteDO.setDstNetAddress(dstNode.getNetAddress());
        // autonomy will save
        if (!envService.isCenter() && !envService.isAutonomy()) {
            nodeRouteDO.setGmtCreate(null);
            nodeRouteDO.setGmtModified(null);
            DbSyncRequest dbSyncRequest = DbSyncRequest.builder().syncDataType(VoteSyncTypeEnum.NODE_ROUTE.name()).projectNodesInfo(nodeRouteDO).build();
            DbSyncUtil.dbDataSyncToCenter(dbSyncRequest);
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
        // query if running project job exists
        // this version is srcNodeId, later version will be dstNodeId
        List<String> nodeIds = data.getList().stream().map(NodeRouterVO::getSrcNodeId).toList();
        Map<String, List<ProjectNodeDO>> projectNodeMap = new HashMap<>(nodeIds.size());
        if (!CollectionUtils.isEmpty(nodeIds)) {
            List<ProjectNodeDO> projectNodeDOList = projectNodeRepository.findByNodeIds(nodeIds);
            projectNodeMap = projectNodeDOList.stream().collect(Collectors.groupingBy(ProjectNodeDO::getNodeId));
        }
        Map<String, List<ProjectNodeDO>> finalProjectNodeMap = projectNodeMap;
        data.getList().forEach(d -> {
            d.setSrcNode(NodeVO.from(nodeManager.getNode(d.getSrcNodeId()), null, null, null));
            d.setDstNode(NodeVO.from(nodeManager.getNode(d.getDstNodeId()), null, null, null));
            // if platformType is AUTONOMY, save opposite result this version. later version will delete
            String srcNodeId = UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? d.getDstNodeId() : d.getSrcNodeId();
            String dstNodeId = UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? d.getSrcNodeId() : d.getDstNodeId();
            DomainRoute.RouteStatus routeStatus = nodeRouteManager.getRouteStatus(srcNodeId, dstNodeId);
            if (!ObjectUtils.isEmpty(routeStatus)) {
                d.setStatus(routeStatus.getStatus());
            }
            // this version is srcNodeId, later version will be dstNodeId
            d.setIsProjectJobRunning(!CollectionUtils.isEmpty(finalProjectNodeMap) && finalProjectNodeMap.containsKey(d.getSrcNodeId()));
        });
        return data;
    }

    @Override
    public void updateNodeRouter(UpdateNodeRouterRequest request) {
        UpdateNodeRouteParam param = UpdateNodeRouteParam.builder()
                .nodeRouteId(request.getRouterId())
                .srcNetAddress(request.getSrcNetAddress())
                .dstNetAddress(request.getDstNetAddress())
                .build();
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId(param.getNodeRouteId());
        if (org.apache.commons.lang3.ObjectUtils.isEmpty(nodeRouteDO)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist " + param.getNodeRouteId());
        }
        NodeDO srcNode = nodeRepository.findByNodeId(nodeRouteDO.getSrcNodeId());
        NodeDO dstNode = nodeRepository.findByNodeId(nodeRouteDO.getDstNodeId());
        validateNoRunningJobs(srcNode, dstNode);

        checkDataPermissions(dstNode.getNodeId());
        if (StringUtils.isNotEmpty(param.getDstNetAddress())) {
            dstNode.setNetAddress(param.getDstNetAddress());
        }
        // p2p mode this version must do, later version will unify and delete
        if (UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY) && StringUtils.isNotBlank(param.getSrcNetAddress())) {
            srcNode.setNetAddress(param.getSrcNetAddress());
        }
        // p2p mode this version is opposite, later version will unify
        // p2p mode update unidirectional authorization in kuscia
        if (UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY)) {
            nodeRouteManager.updateNodeRoute(nodeRouteDO, dstNode, srcNode);
        } else {
            nodeRouteManager.updateNodeRoute(nodeRouteDO, srcNode, dstNode);
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
        nodeRouterVO.setSrcNode(NodeVO.from(nodeManager.getNode(nodeRouterVO.getSrcNodeId()), null, null, null));
        nodeRouterVO.setDstNode(NodeVO.from(nodeManager.getNode(nodeRouterVO.getDstNodeId()), null, null, null));
        // if platformType is AUTONOMY, save opposite result this version. later version will delete
        String srcNodeId = UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? nodeRouterVO.getDstNodeId() : nodeRouterVO.getSrcNodeId();
        String dstNodeId = UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? nodeRouterVO.getSrcNodeId() : nodeRouterVO.getDstNodeId();
        DomainRoute.RouteStatus routeStatus =
                nodeRouteManager.getRouteStatus(srcNodeId, dstNodeId);
        if (!ObjectUtils.isEmpty(routeStatus)) {
            nodeRouterVO.setStatus(routeStatus.getStatus());
        }
        // query if running project job exists
        // this version is srcNodeId, later version will be dstNodeId
        List<ProjectNodeDO> projectNodeDOList = projectNodeRepository.findByNodeId(nodeRouterVO.getSrcNodeId());
        nodeRouterVO.setIsProjectJobRunning(!CollectionUtils.isEmpty(projectNodeDOList));
        return nodeRouterVO;
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

    private void checkDataPermissions(String nodeId) {
        UserContextDTO user = UserContext.getUser();
        if (user.getPlatformType().equals(PlatformTypeEnum.EDGE) && !user.getOwnerId().equals(nodeId)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "no Permissions");
        }
    }

    private void validateNoRunningJobs(NodeDO srcNode, NodeDO dstNode) {
        List<ProjectApprovalConfigDO> projectApprovalConfigDOList = projectApprovalConfigRepository
                .findByInitiator(srcNode.getNodeId(), dstNode.getNodeId());
        projectApprovalConfigDOList = projectApprovalConfigDOList.stream()
                .filter(pac -> pac.getParties().contains(srcNode.getNodeId()) && pac.getParties().contains(dstNode.getNodeId()))
                .toList();
        if (projectApprovalConfigDOList.isEmpty()) {
            return;
        }

        List<String> commonProjectIds = projectApprovalConfigDOList.stream()
                .map(ProjectApprovalConfigDO::getProjectId)
                .collect(Collectors.toList());
        if (commonProjectIds.isEmpty()) {
            return;
        }

        List<ProjectJobDO> projectJobDOList = projectJobRepository.findByProjectIds(commonProjectIds);
        if (projectJobDOList.isEmpty()) {
            return;
        }

        List<String> jobIds = projectJobDOList.stream()
                .map(job -> job.getUpk().getJobId())
                .toList();
        if (jobIds.isEmpty()) {
            return;
        }

        List<GraphJobStatus> statusList = projectJobRepository.findStatusByJobIds(jobIds);
        // If the status set contains RUNNING, throw an exception and do not update the node route
        if (statusList.contains(GraphJobStatus.RUNNING)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_UPDATE_ERROR, "cannot update node route while a job is running.");
        }
    }

}