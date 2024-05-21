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

package org.secretflow.secretpad.web.constant;

/**
 * Authorization Constants
 *
 * @author : xiaonan.fhn
 * @date 2023/05/26
 */
public class AuthConstants {
    public static final String TOKEN_NAME = "User-Token";
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "12#$qwER";


    /**
     * get tokenName
     *
     * @param platformType
     * @param platformNodeId
     * @return {@link String }
     */

    @Deprecated
    public static String getTokenName(String platformType, String platformNodeId) {
        return TOKEN_NAME + "_" + platformType + "_" + platformNodeId;
    }

    public static final String CSRF_SAME_SITE = "SameSite";

    public static final String CSRF_SAME_SITE_VALUE = "Strict";
}
