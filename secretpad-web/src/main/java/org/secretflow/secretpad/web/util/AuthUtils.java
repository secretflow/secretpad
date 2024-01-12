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

package org.secretflow.secretpad.web.util;

import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.web.constant.AuthConstants;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * Authorization utils
 *
 * @author : xiaonan.fhn
 * @date 2023/06/21
 */
public class AuthUtils {

    /**
     * Find token from cookie list by cookie name
     *
     * @param cookies   cookie list
     * @param tokenName
     * @return {@link String }
     */
    @Deprecated
    public static String findTokenInCookie(Cookie[] cookies, String tokenName) {
        if (cookies == null || cookies.length == 0) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "The request header does not contain header!");
        }
        for (Cookie c : cookies) {
            if (c.getName().equals(tokenName)) {
                return c.getValue();
            }
        }
        throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "The request header does not contain header!");
    }

    /**
     * Find token from http header
     *
     * @param request request
     * @return {@link String }
     */
    public static String findTokenInHeader(HttpServletRequest request) {
        String token = request.getHeader(AuthConstants.TOKEN_NAME);
        if (StringUtils.isEmpty(token)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "The request header does not contain header!");
        }
        return token;
    }

}
