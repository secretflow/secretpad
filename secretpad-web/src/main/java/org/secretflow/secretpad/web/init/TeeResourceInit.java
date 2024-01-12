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
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainRpc;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.secretflow.secretpad.common.constant.SystemConstants.SKIP_TEST_P2P;

/**
 * @author yutu
 * @date 2023/09/20
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Profile(SKIP_TEST_P2P)
public class TeeResourceInit implements CommandLineRunner {
    private final NodeRepository nodeRepository;
    private final NodeRouteRepository nodeRouteRepository;
    private final KusciaDomainRpc kusciaDomainRpc;

    @Value("${secretpad.deploy-mode}")
    private String deployMode;
    @Value("${secretpad.platform-type}")
    private String platformType;

    @Override
    public void run(String... args) throws Exception {
        // init tee domain
        if ((DeployModeConstants.ALL_IN_ONE.equals(deployMode) || DeployModeConstants.TEE.equals(deployMode))
                && PlatformTypeEnum.CENTER.name().equals(platformType)
        ) {
            initTeeNodeInKuscia(initTeeNodeInDb());
            initTeeNodeRouteInDb();
        }
    }

    @Transactional
    public NodeDO initTeeNodeInDb() {
        NodeDO tee = NodeDO.builder().nodeId("tee").name("tee").controlNodeId("tee").auth("tee").description("tee")
                .type(DomainConstants.DomainTypeEnum.embedded.name()).netAddress("127.0.0.1:48080").mode(DomainConstants.DomainModeEnum.tee.code).build();
        nodeRepository.save(tee);
        return tee;
    }

    private void initTeeNodeInKuscia(NodeDO tee) {
        DomainOuterClass.QueryDomainResponse response = kusciaDomainRpc.queryDomainNoCheck(DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId(tee.getNodeId()).build());
        if (response.getStatus().getCode() != 0) {
            DomainOuterClass.CreateDomainRequest request = DomainOuterClass.CreateDomainRequest.newBuilder().setDomainId(tee.getNodeId())
                    .setAuthCenter(DomainOuterClass.AuthCenter.newBuilder().setAuthenticationType("Token").setTokenGenMethod("UID-RSA-GEN").build())
                    .build();
            try {
                kusciaDomainRpc.createDomain(request);
            } catch (Exception e) {
                log.warn("tee init error ", e);
            }
        }
    }

    @Transactional
    public void initTeeNodeRouteInDb() {
        NodeRouteDO alice_tee = NodeRouteDO.builder().routeId("3").srcNodeId("alice").dstNodeId("tee").srcNetAddress("127.0.0.1:28080").dstNetAddress("127.0.0.1:48080").build();
        nodeRouteRepository.save(alice_tee);
        NodeRouteDO tee_alice = NodeRouteDO.builder().routeId("4").srcNodeId("tee").dstNodeId("alice").srcNetAddress("127.0.0.1:48080").dstNetAddress("127.0.0.1:28080").build();
        nodeRouteRepository.save(tee_alice);
        NodeRouteDO bob_tee = NodeRouteDO.builder().routeId("5").srcNodeId("bob").dstNodeId("tee").srcNetAddress("127.0.0.1:38080").dstNetAddress("127.0.0.1:48080").build();
        nodeRouteRepository.save(bob_tee);
        NodeRouteDO tee_bob = NodeRouteDO.builder().routeId("6").srcNodeId("tee").dstNodeId("bob").srcNetAddress("127.0.0.1:48080").dstNetAddress("127.0.0.1:38080").build();
        nodeRouteRepository.save(tee_bob);
    }
}