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
 * @author yutu
 * @date 2023/08/07
 */
public enum NodeRouteErrorCode implements ErrorCode {
    /**
     * route exist error
     */
    NODE_ROUTE_ALREADY_EXISTS(202012901),
    /**
     * route create error
     */
    NODE_ROUTE_CREATE_ERROR(202012902),
    /**
     * route does not exist
     */
    NODE_ROUTE_NOT_EXIST_ERROR(202012903),
    /**
     * route delete error
     */
    NODE_ROUTE_DELETE_ERROR(202012904),
    /**
     * route update error
     */
    NODE_ROUTE_UPDATE_ERROR(202012905),
    /**
     * route config error
     */
    NODE_ROUTE_CONFIG_ERROR(202012906),
    /**
     * src node and dest node same
     */
    SRC_NODE_AND_DEST_NODE_SAME(202012907);

    private final int code;

    NodeRouteErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "nodeRoute." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}