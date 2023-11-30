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

import lombok.extern.slf4j.Slf4j;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.service.UserService;
import org.secretflow.secretpad.service.model.auth.UserCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAccountsRepository userAccountsRepository;

    @Override
    @Transactional
    public void create(UserCreateRequest request) {
        Optional<AccountsDO> accountsDOOptional = userAccountsRepository.findByName(request.getName());
        if (accountsDOOptional.isPresent()) {
            String warnStr = String.format("Cancel create account. The name(%s) already exists in db.", request.getName());
            log.warn(warnStr);
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, warnStr);
        }
        AccountsDO accountRequest = new AccountsDO();
        accountRequest.setOwnerType(request.getOwnerType());
        accountRequest.setOwnerId(request.getOwnerId());
        accountRequest.setName(request.getName());
        accountRequest.setPasswordHash(request.getPasswordHash());
        userAccountsRepository.saveAndFlush(accountRequest);
    }

}
