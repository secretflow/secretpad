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

import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.Sha256Utils;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.service.dataproxy.DataProxyService;
import org.secretflow.secretpad.web.constant.AuthConstants;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Initialize db init resource
 *
 * @author yutu
 * @date 2024/04/22
 */
@RequiredArgsConstructor
@Slf4j
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DbDataInit implements CommandLineRunner {
    private final UserAccountsRepository userAccountsRepository;
    @Autowired
    private Environment environment;
    @Value("${secretpad.platform-type}")
    private String platformType;
    @Value("${secretpad.node-id}")
    private String nodeId;

    @Resource
    private DataProxyService dataProxyService;

    @Override
    public void run(String... args) throws Exception {
        initUserAndPwd();
        dataProxyService.updateDataSourceUseDataProxyInMaster();
    }

    public void initUserAndPwd() {
        String username;
        String password;
        try {
            username = environment.getProperty("secretpad.auth.pad_name", String.class, AuthConstants.USER_NAME);
        } catch (Exception e) {
            log.debug("initUserAndPwd failed", e);
            username = AuthConstants.USER_NAME;
        }

        try {
            password = environment.getProperty("secretpad.auth.pad_pwd", String.class, AuthConstants.getRandomPassword());
        } catch (Exception e) {
            log.debug("initUserAndPwd failed", e);
            password = AuthConstants.getRandomPassword();
        }


        AccountsDO accountsDO = AccountsDO.builder()
                .name(username)
                .passwordHash(Sha256Utils.hash(password))
                .ownerType(UserOwnerTypeEnum.fromString(platformType))
                .ownerId(nodeId)
                .instId("")
                .build();
        Optional<AccountsDO> accountsDOOptional = userAccountsRepository.findByName(accountsDO.getName());
        if (accountsDOOptional.isEmpty()) {
            userAccountsRepository.save(accountsDO);
        } else {
            log.info("user {} already exists update password", username);
            accountsDOOptional.get().setPasswordHash(Sha256Utils.hash(password));
            userAccountsRepository.save(accountsDOOptional.get());
        }
        FileUtils.writeToFile(accountsDO.getOwnerId());
    }
}
