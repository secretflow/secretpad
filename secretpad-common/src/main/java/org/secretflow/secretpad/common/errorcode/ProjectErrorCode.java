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
 * Project errorCode
 *
 * @author yansi
 * @date 2023/5/30
 */
public enum ProjectErrorCode implements ErrorCode {
    /**
     * Project does not exist
     */
    PROJECT_NOT_EXISTS(202011501),
    /**
     * Project inst does not exist
     */
    PROJECT_INST_NOT_EXISTS(202011502),
    /**
     * Project node does not exist
     */
    PROJECT_NODE_NOT_EXISTS(202011503),
    /**
     * Project data table does not exist
     */
    PROJECT_DATATABLE_NOT_EXISTS(202011504),
    /**
     * Project result not found
     */
    PROJECT_RESULT_NOT_FOUND(202011505),
    /**
     * The project graph is not empty
     */
    PROJECT_GRAPH_NOT_EMPTY(202011506),

    PROJECT_ARCHIVE_FAIL(202011507),

    PROJECT_UPDATE_FAIL(202011508),

    PROJECT_CAN_NOT_ARCHIVE(202011509),

    PROJECT_CAN_NOT_CREATE_ARCHIVE_VOTE(202011510),

    PROJECT_MODEL_NOT_FOUND(202011511),

    PROJECT_SERVING_NOT_FOUND(202011512),
    PROJECT_SERVING_NOT_SUCCESS(202011513),
    PROJECT_SERVING_NOT_OFFLINE(202011514),
    PROJECT_SERVING_NOT_DISCARD(202011515),

    PROJECT_FEATURE_TABLE_NOT_EXISTS(202011516),
    NON_OUR_CREATION_CAN_VIEWED(202011517),

    PROJECT_CREATE_FAILED(202011518),
    ;


    private final int code;

    ProjectErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "project." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
