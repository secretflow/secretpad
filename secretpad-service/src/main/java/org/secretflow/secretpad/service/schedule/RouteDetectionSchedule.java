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

package org.secretflow.secretpad.service.schedule;

import org.secretflow.secretpad.persistence.datasync.route.RouteDetection;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.common.constant.SystemConstants.SKIP_TEST;

/**
 * @author yutu
 * @date 2023/12/13
 */
@Slf4j
@Service
@Profile(SKIP_TEST)
@RequiredArgsConstructor
public class RouteDetectionSchedule {
    private final DomainRouteServiceGrpc.DomainRouteServiceBlockingStub routeServiceBlockingStub;
    private final NodeRouteRepository nodeRouteRepository;
    private final RouteDetection routeDetection;
    @Value("${secretpad.node-id}")
    private String localNodeId;

    @Scheduled(fixedDelay = 10 * 1000)
    public void routeDetectionSchedule() {
        routeDetection();
    }

    public void routeDetection() {
        List<NodeRouteDO> nodeRouteDOList = nodeRouteRepository.findByDstNodeId(localNodeId);
        DomainRoute.BatchQueryDomainRouteStatusRequest batchQueryDomainRouteStatusRequest = DomainRoute.BatchQueryDomainRouteStatusRequest.newBuilder()
                .addAllRouteKeys(nodeRouteDOList.stream().map(this::domainRouteKeyBuild).collect(Collectors.toList())).build();
        DomainRoute.BatchQueryDomainRouteStatusResponse response =
                routeServiceBlockingStub.batchQueryDomainRouteStatus(batchQueryDomainRouteStatusRequest);
        log.debug("kuscia batchQueryDomainRouteStatus resp {}", response);
        routeDetection(response);
        log.debug("routeDetectionSchedule availableNodes {}", routeDetection.getAvailableNodes());
    }

    private DomainRoute.DomainRouteKey domainRouteKeyBuild(NodeRouteDO nodeRouteDO) {
        return DomainRoute.DomainRouteKey.newBuilder().setSource(nodeRouteDO.getSrcNodeId())
                .setDestination(nodeRouteDO.getDstNodeId()).build();
    }

    private void routeDetection(DomainRoute.BatchQueryDomainRouteStatusResponse response) {
        if (!ObjectUtils.isEmpty(response)) {
            DomainRoute.BatchQueryDomainRouteStatusResponseData data = response.getData();
            if (!ObjectUtils.isEmpty(data)) {
                List<DomainRoute.DomainRouteStatus> routesList = data.getRoutesList();
                if (!CollectionUtils.isEmpty(routesList)) {
                    routesList.forEach(r -> {
                        DomainRoute.RouteStatus status = r.getStatus();
                        String source = r.getSource();
                        if (StringUtils.equalsIgnoreCase("Succeeded", status.getStatus())) {
                            routeDetection.addAvailableNode(source);
                        }
                    });
                }
            }
        }
    }
}