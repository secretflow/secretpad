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

package org.secretflow.secretpad.common.errorcode;

/**
 * Authorization errorCode
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
public enum AuthErrorCode implements ErrorCode {
    /**
     * User or password error
     */
    USER_OR_PASSWORD_ERROR(202011601),
    /**
     * Authorization failure
     */
    AUTH_FAILED(202011602),
    ;

    private final int code;

    AuthErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "auth." + this.name();
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
