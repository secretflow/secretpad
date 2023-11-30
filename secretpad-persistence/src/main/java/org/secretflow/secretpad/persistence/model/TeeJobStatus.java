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

package org.secretflow.secretpad.persistence.model;

import lombok.Getter;

/**
 * Tee job status enum
 *
 * @author xujiening
 * @date 2023/9/18
 */
@Getter
public enum TeeJobStatus {
    /**
     * Running status
     */
    RUNNING("Running"),

    /**
     * Finished, successful
     */
    SUCCESS("Success"),

    /**
     * Finished, failed
     */
    FAILED("Failed");

    private final String val;

    TeeJobStatus(String val) {
        this.val = val;
    }

    /**
     * Convert tee job status from apiLite job status
     *
     * @param status
     * @return tee job status class
     */
    public static TeeJobStatus formKusciaJobStatus(String status) {
        switch (status) {
            case "Succeeded":
                return SUCCESS;
            case "Failed":
                return FAILED;
            default:
                return RUNNING;
        }
    }
}
