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
 * Institution errorCode
 *
 * @author yansi
 * @date 2023/5/9
 */
public enum InstErrorCode implements ErrorCode {
    /**
     * Institution does not exist
     */
    INST_NOT_EXISTS(202011200),


    INST_NOT_MATCH_NODE(202011201),


    /**
     * beyond the max node limitation
     */
    INST_NODE_COUNT_LIMITED(202011202),

    /**
     * not login inst
     */
    INST_MISMATCH_LOGIN(202011203),

    /**
     * check fail
     */
    INST_REGISTER_CHECK_FAILED(202011204),

    /**
     * token not same in db
     */
    INST_TOKEN_MISMATCH(202011205),

    /**
     * more than one Institution
     */
    INST_DUPLICATED(202011206),

    /**
     * file operation fail
     */
    INST_FILE_OPERATION_FAILED(202011207),
    /**
     * token used
     */
    INST_TOKEN_USED(202011208),

    /**
     * initiator inst node not match
     */
    INITIATOR_INST_NODE_MISMATCH(202011209),

    /**
     *  invitee inst node not match
     */
    INVITEE_INST_NODE_MISMATCH(202011210),
    ;


    private final int code;

    InstErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "inst." + this.name();
    }

    @Override
    public Integer getCode() {
        return this.code;
    }
}