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
 * Graph node task status enum
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
public enum GraphNodeTaskStatus {
    /**
     * The default state. For task, has no this status.
     */
    STAGING("Staging"),

    /**
     * For a node, it is connected and the schema is inferred.
     * For a pipeline, it is about to run.
     */
    INITIALIZED("Initialized"),

    /**
     * For a node, it is fired and still running by the backend.
     * For a pipeline, at least one of its nodes is still running.
     */
    RUNNING("Running"),

    /**
     * Stopped
     */
    STOPPED("Stopped"),

    /**
     * Finished, successful.
     */
    SUCCEED("Succeed"),

    /**
     * Finished, failed.
     */
    FAILED("Failed");
    private final String val;

    GraphNodeTaskStatus(String val) {
        this.val = val;
    }

    /**
     * Convert graph node task status from apiLite task status
     *
     * @param status apiLite task status
     * @return graph node task status class
     */
    public static GraphNodeTaskStatus formKusciaTaskStatus(String status) {
        return switch (status) {
            case "Succeeded" -> SUCCEED;
            case "Failed" -> FAILED;
            case "Running" -> RUNNING;
            default -> INITIALIZED;
        };
    }
}
