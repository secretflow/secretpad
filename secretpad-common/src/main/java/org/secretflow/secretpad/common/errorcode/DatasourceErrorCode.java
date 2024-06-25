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
 * @date 2024/05/24
 */
public enum DatasourceErrorCode implements ErrorCode {
    DATA_SOURCE_ENDPOINT_CONNECT_FAIL(202012501),

    DATA_SOURCE_CREATE_FAIL(202012502),
    DATA_SOURCE_BUCKET_NOT_EXIST(202012503),
    DATA_SOURCE_CREDENTIALS_INVALID(202012504),
    DATA_SOURCE_BUCKET_NOT_MATCH_ENDPOINT(202012506),

    DATA_SOURCE_ENDPOINT_API_PORT_ERROR(202012507),

    DATASOURCE_UNKNOWN_EXCEPTION(202012508),

    DATA_SOURCE_DELETE_FAIL(202012509),

    /**
     * Failed to query the data source
     */
    QUERY_DATASOURCE_FAILED(202012505),
    ;


    private final int code;

    DatasourceErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "datasource." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
