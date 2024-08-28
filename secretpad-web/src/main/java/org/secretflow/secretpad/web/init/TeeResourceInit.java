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

package org.secretflow.secretpad.web.init;

import org.secretflow.secretpad.common.constant.DeployModeConstants;
import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.model.datatable.PushDatatableToTeeRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.secretflow.secretpad.common.constant.DomainConstants.*;
import static org.secretflow.secretpad.common.constant.SystemConstants.SKIP_P2P;

/**
 * Initialize the TEE resource
 *
 * @author yutu
 * @date 2023/09/20
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Profile(SKIP_P2P)
@Order(Ordered.LOWEST_PRECEDENCE)
public class TeeResourceInit implements CommandLineRunner {
    private final NodeRepository nodeRepository;
    private final NodeRouteRepository nodeRouteRepository;
    private final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    private final DatatableService datatableService;

    @Value("${secretpad.deploy-mode}")
    private String deployMode;
    @Value("${secretpad.platform-type}")
    private String platformType;
    @Value("${secretpad.tee:true}")
    private boolean teeEnabled;
    @Value("${secretpad.node-id}")
    private String nodeId;

    @Override
    public void run(String... args) throws Exception {
        // init tee domain
        if (!teeEnabled) {
            return;
        }
        log.info("init tee node {} {}", deployMode, platformType);
        if ((DeployModeConstants.ALL_IN_ONE.equals(deployMode) || DeployModeConstants.TEE.equals(deployMode))
                && PlatformTypeEnum.CENTER.name().equals(platformType)
        ) {
            initTeeNodeInKuscia(initTeeNodeInDb());
            initTeeNodeRouteInDb();
            initAliceBobDatableToTee();
        }
    }

    @Transactional
    public NodeDO initTeeNodeInDb() {
        NodeDO tee = NodeDO.builder().nodeId("tee").name("tee").controlNodeId("tee").auth("tee").description("tee").instId("")
                .type(DomainConstants.DomainTypeEnum.embedded.name()).netAddress("127.0.0.1:48080").mode(DomainConstants.DomainModeEnum.tee.code).build();
        nodeRepository.save(tee);
        return tee;
    }

    /**
     * Initialize the TEE node in Kuscia
     *
     * @param tee TEE node information
     */
    private void initTeeNodeInKuscia(NodeDO tee) {
        // Call the query domain name operation of Kuscia to query whether a TEE node exists
        UserContext.setBaseUser(UserContextDTO.builder().ownerId(nodeId).build());
        DomainOuterClass.QueryDomainResponse response = kusciaGrpcClientAdapter.queryDomain(DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId(tee.getNodeId()).build());
        // If the TEE node does not exist, create ith the following parameters
        if (ObjectUtils.isNotEmpty(response) && response.getStatus().getCode() != 0) {
            DomainOuterClass.CreateDomainRequest request = DomainOuterClass.CreateDomainRequest.newBuilder().setDomainId(tee.getNodeId())
                    .setAuthCenter(DomainOuterClass.AuthCenter.newBuilder().setAuthenticationType("Token").setTokenGenMethod("UID-RSA-GEN").build())
                    .build();
            try {
                kusciaGrpcClientAdapter.createDomain(request);
            } catch (Exception e) {
                log.warn("tee init error ", e);
            }
        }
    }

    /**
     * Initialize the node routing table
     */
    @Transactional
    public void initTeeNodeRouteInDb() {
        // Create a route from ALICE to TEE
        NodeRouteDO aliceTee = NodeRouteDO.builder().routeId("3").srcNodeId("alice").dstNodeId("tee").srcNetAddress("127.0.0.1:28080").dstNetAddress("127.0.0.1:48080").build();
        nodeRouteRepository.save(aliceTee);
        // Create a route from TEE to ALICE
        NodeRouteDO teeAlice = NodeRouteDO.builder().routeId("4").srcNodeId("tee").dstNodeId("alice").srcNetAddress("127.0.0.1:48080").dstNetAddress("127.0.0.1:28080").build();
        nodeRouteRepository.save(teeAlice);
        // Create a route from BOB to TEE
        NodeRouteDO bobTee = NodeRouteDO.builder().routeId("5").srcNodeId("bob").dstNodeId("tee").srcNetAddress("127.0.0.1:38080").dstNetAddress("127.0.0.1:48080").build();
        nodeRouteRepository.save(bobTee);
        // Create a route from TEE to BOB
        NodeRouteDO teeBob = NodeRouteDO.builder().routeId("6").srcNodeId("tee").dstNodeId("bob").srcNetAddress("127.0.0.1:48080").dstNetAddress("127.0.0.1:38080").build();
        nodeRouteRepository.save(teeBob);
    }

    public void initAliceBobDatableToTee() {
        PushDatatableToTeeRequest aliceTableRequest = PushDatatableToTeeRequest.builder()
                .nodeId(ALICE)
                .datatableId(ALICE_TABLE)
                .build();
        PushDatatableToTeeRequest bobTableRequest = PushDatatableToTeeRequest.builder()
                .nodeId(BOB)
                .datatableId(BOB_TABLE)
                .build();
        log.info("push {} datatable to tee node", ALICE_TABLE);
        datatableService.pushDatatableToTeeNode(aliceTableRequest);
        log.info("push {} datatable to tee node", BOB_TABLE);
        datatableService.pushDatatableToTeeNode(bobTableRequest);
    }
}