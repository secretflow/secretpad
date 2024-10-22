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

package org.secretflow.secretpad.scheduled.service;

import org.secretflow.secretpad.persistence.entity.ProjectScheduleTaskDO;
import org.secretflow.secretpad.scheduled.model.ScheduledIdRequest;

/**
 * @author yutu
 * @date 2024/08/21
 */
public interface ISecretpadScheduledService {
    /**
     * add scheduler
     */
    boolean addScheduler(String name, String group, ProjectScheduleTaskDO data, String cron);

    /**
     * pause scheduler
     */
    boolean pauseScheduler(String jobDetailName, String jobDetailGroup);

    /**
     * pause scheduler
     */
    boolean pauseScheduler(String jobDetailGroup);

    /**
     * resume scheduler
     */
    boolean resumeScheduler(String jobDetailName, String jobDetailGroup);

    /**
     * build scheduler id
     */
    String buildSchedulerId(ScheduledIdRequest scheduledIdRequest);
}