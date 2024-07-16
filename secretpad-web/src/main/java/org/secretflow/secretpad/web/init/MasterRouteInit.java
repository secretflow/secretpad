/*
 * Copyright 2024 Ant Group Co., Ltd.
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

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.secretflow.secretpad.common.constant.Constants.HTTP_PREFIX;
import static org.secretflow.secretpad.common.constant.Constants.KUSCIA_PROTOCOL;

/**
 * Initializer node route for center mode
 *
 * @author chixian
 * @date 2024/03/29
 */
@Profile(value = {SystemConstants.DEFAULT, SystemConstants.TEST, SystemConstants.DEV})
@RequiredArgsConstructor
@Slf4j
@Service
@Order(Ordered.LOWEST_PRECEDENCE)
public class MasterRouteInit implements CommandLineRunner {

    @Value("${kusciaapi.protocol:tls}")
    private String protocol;

    @Autowired
    private NodeRouteRepository nodeRouteRepository;

    @Override
    public void run(String... args) throws Exception {
        List<NodeRouteDO> nodeRouteList = nodeRouteRepository.findAllById(List.of("1", "2", "3"));
        if (CollectionUtils.isEmpty(nodeRouteList)) {
            log.warn("node router list is empty");
            return;
        }
        String kusciaProtocol = KUSCIA_PROTOCOL.equals(protocol) ? "http://" : "https://";
        log.info("kuscia protocol: {}", kusciaProtocol);
        for (NodeRouteDO nodeRouteDO : nodeRouteList) {
            if (nodeRouteDO.getSrcNetAddress().contains(HTTP_PREFIX) || nodeRouteDO.getDstNetAddress().contains(HTTP_PREFIX)) {
                log.info("router id {} the protocol has been added, srcNetAddress :{}, dstNetAddress :{}", nodeRouteDO.getRouteId(), nodeRouteDO.getSrcNetAddress(), nodeRouteDO.getDstNetAddress());
                continue;
            }
            nodeRouteDO.setDstNetAddress(kusciaProtocol + nodeRouteDO.getDstNetAddress());
            nodeRouteDO.setSrcNetAddress(kusciaProtocol + nodeRouteDO.getSrcNetAddress());
            nodeRouteRepository.save(nodeRouteDO);
            log.info("update node router id: {}, srcNetAddress is:{}, dstNetAddress is:{}", nodeRouteDO.getRouteId(), nodeRouteDO.getSrcNetAddress(), nodeRouteDO.getDstNetAddress());
        }
    }

}
