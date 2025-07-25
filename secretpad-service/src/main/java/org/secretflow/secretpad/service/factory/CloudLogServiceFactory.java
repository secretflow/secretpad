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

package org.secretflow.secretpad.service.factory;

import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskRepository;
import org.secretflow.secretpad.service.ICloudLogService;
import org.secretflow.secretpad.service.impl.SLSCloudLogServiceImpl;
import org.secretflow.secretpad.service.properties.LogConfigProperties;
import org.secretflow.secretpad.service.util.ValidationUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Objects;

import static org.secretflow.secretpad.common.constant.SystemConstants.SKIP_TEST;

/**
 * @author chenmingliang
 * @date 2024/04/18
 */
@Configuration
@EnableConfigurationProperties(LogConfigProperties.class)
@Slf4j
@Profile(SKIP_TEST)
public class CloudLogServiceFactory {

    private final LogConfigProperties logConfigProperties;

    private final ProjectJobTaskRepository taskRepository;

    private final NodeRepository nodeRepository;

    @Value("${secretpad.platform-type}")
    private String platformType;

    @Value("${secretpad.node-id}")
    private String nodeId;


    public CloudLogServiceFactory(LogConfigProperties logConfigProperties, ProjectJobTaskRepository taskRepository, NodeRepository nodeRepository) {
        this.logConfigProperties = logConfigProperties;
        this.taskRepository = taskRepository;
        this.nodeRepository = nodeRepository;
    }

    public ICloudLogService getLogServiceInstance() {
        LogConfigProperties.SLSConfig sls = logConfigProperties.getSls();
        try {
            if (Objects.nonNull(sls) && ValidationUtil.allFieldsNotNull(sls)) {
                log.info("Detected that the cloud service has been configured, create cloud service instance");
                return new SLSCloudLogServiceImpl(
                        sls,
                        platformType,
                        nodeId,
                        taskRepository,
                        nodeRepository
                );
            } else {
                log.warn("cloud service configuration is not available, please check your configuration,like ak,sk,host");
            }
        } catch (Exception e) {
            // The cloud log is not mandatory; even if this Bean fails to be created, we still need to allow the system to start normally, just providing an error log will suffice.
            log.error("create cloud log service instance failed, error message:{}", e.getMessage());
        }

        return null;
    }
}
