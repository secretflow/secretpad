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

package org.secretflow.secretpad.service.listener;

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.integration.job.JobManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Job synchronized listener
 *
 * @author yansi
 * @date 2023/6/12
 */
@Component
@ConditionalOnProperty(name = "job.sync.enabled", havingValue = "true", matchIfMissing = true)
public class JobSyncListener implements ApplicationListener<ApplicationReadyEvent> {
    @Autowired
    private JobManager jobManager;

    /**
     * Start to synchronize the job in ApiLite to secretPad
     *
     * @param event application ready event
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // todo: use backoff
        try {
            while (true) {
                jobManager.startSync();
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            throw SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, e);
        }
    }
}
