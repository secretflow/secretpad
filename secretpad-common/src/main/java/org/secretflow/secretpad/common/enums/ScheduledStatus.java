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

package org.secretflow.secretpad.common.enums;

import java.util.Locale;

/**
 * scheduled status
 *
 * @author yutu
 * @date 2024/8/28
 */
public enum ScheduledStatus {

    /**
     * UP
     */
    UP("UP"),

    /**
     * DOWN
     */
    DOWN("DOWN"),

    /**
     * To be run
     */
    TO_BE_RUN("TO_BE_RUN"),
    RUNNING("Running"),

    /**
     * Stopped
     */
    STOPPED("Stopped"),

    /**
     * Stopping
     */
    STOPPING("Stopping"),


    /**
     * Finished, successful
     */
    SUCCEED("Succeed"),

    /**
     * Finished, failed
     */
    FAILED("Failed");

    private final String val;

    ScheduledStatus(String val) {
        this.val = val;
    }

    public static ScheduledStatus from(String val) {
        return switch (val.toUpperCase(Locale.ROOT)) {
            case "UP" -> UP;
            case "DOWN" -> DOWN;
            case "TO_BE_RUN" -> TO_BE_RUN;
            case "FAILED" -> FAILED;
            case "STOPPED" -> STOPPED;
            case "STOPPING" -> STOPPING;
            case "SUCCESS", "SUCCEED" -> SUCCEED;
            default -> RUNNING;
        };
    }
}
