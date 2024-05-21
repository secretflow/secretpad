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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskRepository;
import org.secretflow.secretpad.service.factory.CloudLogServiceFactory;
import org.secretflow.secretpad.service.properties.LogConfigProperties;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.exception.LogException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author chenmingliang
 * @date 2024/05/11
 */
@ExtendWith(MockitoExtension.class)
public class CloudLogServiceFactoryTest {

    @Mock
    LogConfigProperties logConfigProperties;

    @Mock
    ProjectJobTaskRepository taskRepository;

    @Mock
    NodeRepository nodeRepository;

    @Mock
    Client client;

    @Test
    void test() throws LogException {
        LogConfigProperties logConfigProperties = new LogConfigProperties();
        LogConfigProperties.SLSConfig slsConfig = new LogConfigProperties.SLSConfig();
        slsConfig.setAk("ak");
        slsConfig.setSk("sl");
        slsConfig.setHost("hs");
        slsConfig.setProject(";pro");
        logConfigProperties.setSls(slsConfig);

        CloudLogServiceFactory cloudLogServiceFactory = new CloudLogServiceFactory(logConfigProperties, taskRepository, nodeRepository);
        cloudLogServiceFactory.getLogServiceInstance();
    }
}
