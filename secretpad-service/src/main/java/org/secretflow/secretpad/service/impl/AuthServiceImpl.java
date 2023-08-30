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

import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User auth service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserAccountsRepository userAccountsRepository;

    @Autowired
    private UserTokensRepository userTokensRepository;

    @Override
    public String login(String name, String passwordHash) {
        AccountsDO user = getUserAccount(name);
        if (!user.getPasswordHash().equals(passwordHash)) {
            LOGGER.error("Password not correct! UserName = {}", name);
            throw SecretpadException.of(AuthErrorCode.USER_OR_PASSWORD_ERROR);
        }
        String token = UUIDUtils.newUUID();
        userTokensRepository.saveAndFlush(
                TokensDO.builder()
                        .name(user.getName())
                        .token(token)
                        .gmtToken(LocalDateTime.now())
                        .build()
        );
        return token;
    }

    @Override
    public void logout(String name, String token) {
        userTokensRepository.deleteByNameAndToken(name, token);
    }

    /**
     * Get user account data object by userName
     *
     * @param name userName
     * @return user account data object
     */
    private AccountsDO getUserAccount(String name) {
        Optional<AccountsDO> user = userAccountsRepository.findByName(name);
        if (user.isEmpty()) {
            LOGGER.error("Cannot find user {}", name);
            throw SecretpadException.of(AuthErrorCode.USER_OR_PASSWORD_ERROR);
        }
        return user.get();
    }

}
