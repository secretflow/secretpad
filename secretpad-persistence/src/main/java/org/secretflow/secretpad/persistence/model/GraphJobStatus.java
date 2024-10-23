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
 * Graph job status enum
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
public enum GraphJobStatus {

    /**
     * Running status
     * For a node, it is fired and still running by the backend.
     * For a pipeline, at least one of its nodes is still running.
     */
    RUNNING("Running"),

    /**
     * Stopped
     */
    STOPPED("Stopped"),

    /**
     * Finished, successful
     */
    SUCCEED("Succeed"),

    /**
     * Finished, failed
     */
    FAILED("Failed");

    private final String val;

    GraphJobStatus(String val) {
        this.val = val;
    }

    /**
     * Convert graph job status from apiLite job status
     *
     * @param status apiLite job status
     * @return graph job status class
     */
    public static GraphJobStatus formKusciaJobStatus(String status) {
        return switch (status) {
            case "Succeeded" -> SUCCEED;
            case "Failed" -> FAILED;
            default -> RUNNING;
        };
    }
}
