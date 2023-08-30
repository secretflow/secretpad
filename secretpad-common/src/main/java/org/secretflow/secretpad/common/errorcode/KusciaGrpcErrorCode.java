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
 * @date 2023/08/09
 */
public enum KusciaGrpcErrorCode implements ErrorCode {

    /**
     * rpc error
     */
    RPC_ERROR(202012101),
    ;

    private final int code;

    KusciaGrpcErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "kuscia." + this.name();
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}