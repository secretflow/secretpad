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
 * System errorCode
 *
 * @author yansi
 * @date 2023/5/10
 */
public enum SystemErrorCode implements ErrorCode {
    /**
     * Parameter validation error
     */
    VALIDATION_ERROR(202011100),
    /**
     * Unknown error
     */
    UNKNOWN_ERROR(202011101),
    /**
     * Out of range error
     */
    OUT_OF_RANGE_ERROR(202011102),
    /**
     * Todo error
     */
    TODO_ERROR(202011199),
    /**
     * http response 4xx error
     */
    HTTP_4XX_ERROR(202011103),
    /**
     * http response 404 error
     */
    HTTP_404_ERROR(202011104),
    /**
     * http response 5xx error
     */
    HTTP_5XX_ERROR(202011105),

    ENCODE_ERROR(202011106),

    SIGNATURE_ERROR(2020111107),
    VERIFY_SIGNATURE_ERROR(2020111108),
    /**
     * sse error
     */
    SSE_ERROR(202011109),

    /**
     * sync error
     */
    SYNC_ERROR(202011110),
    REMOTE_CALL_ERROR(2020111011),

    REQUEST_FREQUENCY_ERROR(2020111012),

    ;

    private final int code;

    SystemErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "system." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
