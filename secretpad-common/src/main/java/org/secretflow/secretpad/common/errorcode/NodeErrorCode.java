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
 * Node errorCode
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
public enum NodeErrorCode implements ErrorCode {
    /**
     * Node already exists
     */
    NODE_ALREADY_EXIST_ERROR(202011401),
    /**
     * Failed to create the node
     */
    NODE_CREATE_ERROR(202011402),
    /**
     * Node does not exist
     */
    NODE_NOT_EXIST_ERROR(202011403),
    /**
     * Failed to delete the node
     */
    NODE_DELETE_ERROR(202011404),
    /**
     * Domain data does not exist
     */
    DOMAIN_DATA_NOT_EXISTS(202011405),
    /**
     * node update error
     */
    NODE_UPDATE_ERROR(202011406),
    /**
     * Domain token error
     */
    NODE_TOKEN_ERROR(202011407),
    /**
     * Domain token empty error
     */
    NODE_TOKEN_IS_EMPTY_ERROR(202011408),
    ;

    private final int code;

    NodeErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "node." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }

}
