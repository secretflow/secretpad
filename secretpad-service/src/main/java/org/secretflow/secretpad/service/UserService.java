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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.service.model.auth.UserCreateRequest;
import org.secretflow.secretpad.service.model.auth.UserUpdatePwdRequest;

/**
 * @author beiwei
 * @date 2023/9/13
 */
public interface UserService {


    /**
     * Create user
     *
     * @param userCreateRequest
     */

    void create(UserCreateRequest userCreateRequest);


    /**
     * Update user pwd by userName
     *
     * @param userUpdatePwdRequest
     */
    void updatePwd(UserUpdatePwdRequest userUpdatePwdRequest);


    /**
     * Get user by userName
     *
     * @param userName
     * @return {@link AccountsDO }
     */

    AccountsDO getUserByName(String userName);


    /**
     * user lock
     *
     * @param accountsDO
     */

    void userLock(AccountsDO accountsDO);

    /**
     * user unlock
     *
     * @param accountsDO
     */

    void userUnlock(AccountsDO accountsDO);

}
