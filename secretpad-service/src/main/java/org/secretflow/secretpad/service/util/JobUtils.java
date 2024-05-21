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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

/**
 * Job utils
 *
 * @author yansi
 * @date 2023/6/9
 */
public class JobUtils {
    /**
     * Generate taskId via jobId and graph nodeId
     *
     * @param jobId       target jobId
     * @param graphNodeId target graph nodeId
     * @return taskId
     */
    public static String genTaskId(String jobId, String graphNodeId) {
        return String.format("%s-%s", jobId, graphNodeId);
    }

    /**
     * Generate task outputId via jobId and outputId
     *
     * @param jobId    target jobId
     * @param outputId target outputId
     * @return task outputId
     */
    public static String genTaskOutputId(String jobId, String outputId) {
        return String.format("%s-%s", jobId, outputId);
    }

    /**
     * Generate extend taskId via jobId and graph nodeId
     *
     * @param jobId  target jobId
     * @param extend target graph nodeId
     * @return taskId
     */
    public static String genExtendTaskId(String jobId, String extend) {
        return String.format("%s--%s", jobId, extend);
    }

    /**
     * Parses the memory size string and returns the numeric part after removing the 'Gi' unit.
     *
     * @param memorySize A string representing the memory size with 'Gi' unit, e.g., "4Gi".
     * @return The numeric part of the memory size as an integer.
     * @throws NumberFormatException if the string cannot be parsed as an integer.
     */
    public static double parseMemorySize(String memorySize, String nodeId) {
        try {
            return Double.parseDouble(memorySize.replace("Gi", "").trim());
        } catch (NumberFormatException e) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, e, "Invalid memory value for nodeId: " + nodeId);
        }
    }
}
