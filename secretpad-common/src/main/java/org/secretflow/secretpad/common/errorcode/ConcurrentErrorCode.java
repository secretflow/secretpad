/*
 * Copyright 2024 Ant Group Co., Ltd.
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
 * @author chenmingliang
 * @date 2024/07/04
 */
public enum ConcurrentErrorCode implements ErrorCode {

    TASK_INTERRUPTED_ERROR(202012601),
    TASK_EXECUTION_ERROR(202012602),
    TASK_TIME_OUT_ERROR(202012603),
    ;


    private final int code;

    ConcurrentErrorCode(int code) {
        this.code = code;
    }

    @Override

    public String getMessageKey() {
        return "concurrent." + this.name();
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}
