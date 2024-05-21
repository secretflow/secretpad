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
 * Job errorCode
 *
 * @author yansi
 * @date 2023/5/30
 */
public enum JobErrorCode implements ErrorCode {
    /**
     * The project job does not exist
     */
    PROJECT_JOB_NOT_EXISTS(202011901),
    /**
     * Failed to create the project job
     */
    PROJECT_JOB_CREATE_ERROR(202011902),
    /**
     * The project job task does not exist
     */
    PROJECT_JOB_TASK_NOT_EXISTS(202011903),
    /**
     * Failed to delete the project job
     */
    PROJECT_JOB_DELETE_ERROR(202011904),
    /**
     * Failed to get the project job cloud log
     */
    PROJECT_JOB_CLOUD_LOG_ERROR(202011905),

    /**
     * permission denied to get the project job cloud log
     */
    PROJECT_JOB_NODE_PERMISSION_ERROR(202011906),
    ;

    private final int code;

    JobErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "project_job." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
