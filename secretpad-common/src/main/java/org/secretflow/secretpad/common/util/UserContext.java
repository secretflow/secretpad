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

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

/**
 * @author yutu
 * @date 2023/08/09
 */
@Setter
@Getter
@ToString
public final class UserContext {
    private static final ThreadLocal<UserContextDTO> USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static String getUserName() {
        return getUser().getName();
    }

    public static UserContextDTO getUser() {
        UserContextDTO userContextDTO = USER.get();
        if (userContextDTO == null) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "auth failed");
        }
        return userContextDTO;
    }

    public static UserContextDTO getUserOrNotExist() {
        return USER.get();
    }

    public static void setBaseUser(UserContextDTO userContextDTO) {
        if (!ObjectUtils.isEmpty(userContextDTO) && userContextDTO.getVirtualUserForNode() == null) {
            userContextDTO.setVirtualUserForNode(false);
        }
        USER.set(userContextDTO);
    }

    public static void remove() {
        USER.remove();
    }
}