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
 * User errorCode
 *
 * @author lihaixin
 * @date 2023/12/28
 */
public enum UserErrorCode implements ErrorCode {

    /**
     * User update password error inconsistent
     */
    USER_UPDATE_PASSWORD_ERROR_INCONSISTENT(202012001),

    /**
     * User update password error same
     */
    USER_UPDATE_PASSWORD_ERROR_SAME(202012002),

    /**
     * User update password error incorrect
     */
    USER_UPDATE_PASSWORD_ERROR_INCORRECT(202012003),


    ;

    private final int code;

    UserErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "user." + this.name();
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
