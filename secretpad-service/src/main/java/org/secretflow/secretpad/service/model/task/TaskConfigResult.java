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

package org.secretflow.secretpad.service.model.task;

/**
 * @author lufeng
 * @date 2024/11/18
 */
public class TaskConfigResult {
    private String taskInputConfig;
    private String appImage;

    // Constructor, getters and setters
    public TaskConfigResult(String taskInputConfig, String appImage) {
        this.taskInputConfig = taskInputConfig;
        this.appImage = appImage;
    }

    public String getTaskInputConfig() {
        return taskInputConfig;
    }

    public String getAppImage() {
        return appImage;
    }
}