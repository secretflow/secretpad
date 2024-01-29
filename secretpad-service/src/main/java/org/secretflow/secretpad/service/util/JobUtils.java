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
}
