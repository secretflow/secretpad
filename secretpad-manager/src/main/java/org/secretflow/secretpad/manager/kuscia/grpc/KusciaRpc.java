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

package org.secretflow.secretpad.manager.kuscia.grpc;

import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import org.secretflow.v1alpha1.common.Common;

/**
 * @author yutu
 * @date 2023/08/09
 */
public interface KusciaRpc {
    default void checkResponse(Common.Status status) {
        if (status.getCode() != 0) {
            throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, status.getMessage());
        }
    }

}