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

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pPaddingNodeServiceImpl;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectGraphRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.dataproxy.DataProxyService;
import org.secretflow.secretpad.service.impl.InstServiceImpl;
import org.secretflow.secretpad.web.constant.AuthConstants;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate.ownerId_cache;

/**
 * Initializer node data for p2p mode
 *
 * @author wb-698356
 * @date 2023/12/14
 */
@Profile(value = {SystemConstants.P2P, SystemConstants.DEV})
@RequiredArgsConstructor
@Slf4j
@Service
@Order(Ordered.LOWEST_PRECEDENCE)
public class P2pDataInit implements CommandLineRunner {

    private final NodeService nodeService;
    private final ProjectRepository projectRepository;
    private final ProjectGraphRepository projectGraphRepository;

    private final UserAccountsRepository userAccountsRepository;

    private final NodeRepository nodeRepository;

    @Value("${secretpad.platform-type}")
    private String platformType;

    @Value("${secretpad.node-id}")
    private String nodeId;

    @Value("${secretpad.inst-name:DEFAULT_INST}")
    private String instName;

    @Resource
    private Environment environment;
    @Resource
    private P2pPaddingNodeServiceImpl p2pPaddingNodeService;
    @Resource
    private DataProxyService dataProxyService;

    @Override
    public void run(String... args) throws Exception {
        if (!PlatformTypeEnum.AUTONOMY.name().equals(platformType)) {
            return;
        }
        log.info("P2pDataInit start");
        try {
            nodeService.initialNode(instName);
            resumeAccountsInst();
        } catch (Exception e) {
            log.error("initialize node failed", e);
        }
        init_ownerId_cache();
    }

    private void resumeAccountsInst() {

        String username;
        try {
            username = environment.getProperty("secretpad.auth.pad_name", String.class, AuthConstants.USER_NAME);
        } catch (Exception e) {
            log.debug("initUserAndPwd failed", e);
            username = AuthConstants.USER_NAME;
        }
        Optional<AccountsDO> accountOp = userAccountsRepository.findByName(username);
        if (accountOp.isEmpty()) {
            throw SecretpadException.of(AuthErrorCode.USER_NOT_FOUND);
        }
        AccountsDO accountsDO = accountOp.get();
        String ownerId = nodeRepository.findByNodeId(nodeId).getInstId();
        accountsDO.setOwnerId(ownerId);
        accountsDO.setInstId(ownerId);
        userAccountsRepository.save(accountsDO);
        p2pPaddingNodeService.supInstInfo(accountsDO);
        InstServiceImpl.INST_ID = accountsDO.getInstId();
        FileUtils.writeToFile(accountsDO.getInstId());
        dataProxyService.updateDataSourceUseDataProxyInP2p(accountsDO.getInstId());
    }

    public void init_ownerId_cache() {
        for (ProjectDO project : projectRepository.findAll()) {
            ownerId_cache.put(project.getProjectId(), project.getOwnerId());
        }
        for (ProjectGraphDO projectGraphDO : projectGraphRepository.findAll()) {
            ownerId_cache.put(projectGraphDO.getProjectId() + "_" + projectGraphDO.getUpk().getGraphId(), projectGraphDO.getOwnerId());
        }
        log.info("ownerId_cache init {}", ownerId_cache);
    }
}
