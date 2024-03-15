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

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainRpc;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author yutu
 * @date 2023/10/13
 */
@Profile(value = {SystemConstants.EDGE, SystemConstants.DEV})
@Slf4j
@RequiredArgsConstructor
@Service
public class EdgeResourceInit implements CommandLineRunner {

    private final NodeRepository nodeRepository;
    private final KusciaDomainRpc kusciaDomainRpc;

    private final UserAccountsRepository userAccountsRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("init edge initialize resource start");
        List<AccountsDO> all = userAccountsRepository.findAll();
        AccountsDO accountsDO = all.get(0);
        String ownerId = accountsDO.getOwnerId();
        NodeDO nodeDO = nodeRepository.findByNodeId(accountsDO.getOwnerId());
        DomainOuterClass.QueryDomainResponseData domainData;
        DomainOuterClass.QueryDomainResponse response = kusciaDomainRpc.queryDomainNoCheck(DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId(ownerId).build());
        if (ObjectUtils.isNotEmpty(response) && response.getStatus().getCode() == 0) {
            domainData = response.getData();
            if (ObjectUtils.isEmpty(nodeDO)) {
                nodeDO = new NodeDO();
            }
            nodeDO.setNodeId(domainData.getDomainId());
            nodeDO.setControlNodeId(domainData.getDomainId());
            nodeDO.setName("");
            nodeDO.setToken("");
            nodeDO.setType(DomainConstants.DomainTypeEnum.normal.name());
            nodeDO.setIsDeleted(Boolean.FALSE);
            nodeDO.setMode(1);

            UserContextDTO userContextDTO = new UserContextDTO();
            userContextDTO.setName("admin");
            UserContext.setBaseUser(userContextDTO);

            nodeRepository.saveAndFlush(nodeDO);

            UserContext.remove();
        } else {
            log.error("kuscia lite {} , not ready", ownerId);
        }
        log.info("init edge initialize resource end");
    }
}