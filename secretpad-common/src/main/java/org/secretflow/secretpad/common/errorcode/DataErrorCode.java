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
 * Data errorCode
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
public enum DataErrorCode implements ErrorCode {
    /**
     * File name empty
     */
    FILE_NAME_EMPTY(202011801),
    /**
     * The file type is not supported
     */
    FILE_TYPE_NOT_SUPPORT(202011802),
    /**
     * File already exists
     */
    FILE_EXISTS_ERROR(202011803),
    /**
     * File does not exist
     */
    FILE_NOT_EXISTS_ERROR(202011804),
    /**
     * Illegal parameter
     */
    ILLEGAL_PARAMS_ERROR(202011805),
    /**
     * File name duplication
     */
    NAME_DUPLICATION_ERROR(202011806),

    ;

    private final int code;

    DataErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "data." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }

}
