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

package org.secretflow.secretpad.manager.integration.job.event;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author yutu
 * @date 2024/03/28
 */
@Slf4j
@Component
public class JobSyncErrorOrCompletedEventListener {

    @Autowired
    @Qualifier("jobManager")
    private AbstractJobManager jobManager;

    @Value("${secretpad.node-id}")
    private String nodeId;

    @Resource
    private Environment env;

    @EventListener
    public void onJobSyncEvent(JobSyncErrorOrCompletedEvent event) {
        UserContext.setBaseUser(UserContextDTO.builder().ownerId(nodeId).build());
        String[] activeProfiles = env.getActiveProfiles();
        if (!Arrays.asList(activeProfiles).contains(SystemConstants.TEST)) {
            jobManager.startSync();
        }
    }
}
