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
 * Datatable errorCode
 *
 * @author yansi
 * @date 2023/5/10
 */
public enum DatatableErrorCode implements ErrorCode {
    /**
     * Data table does not exist
     */
    DATATABLE_NOT_EXISTS(202011301),
    /**
     * Unsupported data table type
     */
    UNSUPPORTED_DATATABLE_TYPE(202011302),
    /**
     * Failed to query the data table
     */
    QUERY_DATATABLE_FAILED(202011303),
    /**
     * Failed to delete the data table
     */
    DELETE_DATATABLE_FAILED(202011304),
    /**
     * Datatable has been auth to project
     */
    DATATABLE_DUPLICATED_AUTHORIZED(202011305);

    private final int code;

    DatatableErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "datatable." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}