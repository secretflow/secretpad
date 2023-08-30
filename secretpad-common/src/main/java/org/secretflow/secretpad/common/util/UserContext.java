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

package org.secretflow.secretpad.common.util;

import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import org.apache.commons.lang3.StringUtils;

/**
 * @author yutu
 * @date 2023/08/09
 */
public final class UserContext {
    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static String getUserName() {
        return getUser();
    }

    public static String getUser() {
        String userName = USER.get();
        if (StringUtils.isEmpty(userName)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "auth failed");
        }
        return userName;
    }

    public static void setBaseUser(String userName) {
        USER.set(userName);
    }

    public static void remove() {
        USER.remove();
    }
}