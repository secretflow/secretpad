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
 * scheduled errorCode
 *
 * @author yutu
 * @date 2024/8/27
 */
public enum ScheduledErrorCode implements ErrorCode {
    /**
     * project job need success once
     */
    PROJECT_JOB_NEED_SUCCESS_ONCE(202015001),

    /**
     * project job not exist
     */
    PROJECT_JOB_NOT_EXIST(202015002),
    SCHEDULE_NOT_EXIST(202015003),
    SCHEDULE_UP_NOT_DEL(202015004),
    SCHEDULE_RUNNING_NOT_OFFLINE(202015005),
    REQUEST_IS_NULL(202015006),
    SCHEDULE_TASK_NOT_EXIST(202015007),
    SCHEDULE_TASK_STATUS_NOT_RUNNING(202015008),
    PROJECT_JOB_RESTART_ERROR(202015009),
    USER_NOT_OWNER(202015010),
    SCHEDULE_CREATE_ERROR(202015011);

    private final int code;

    ScheduledErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "scheduled." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
