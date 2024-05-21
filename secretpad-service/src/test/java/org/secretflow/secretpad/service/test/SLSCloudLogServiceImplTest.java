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
import org.secretflow.secretpad.service.impl.SLSCloudLogServiceImpl;
import org.secretflow.secretpad.service.properties.LogConfigProperties;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.CreateLogStoreRequest;
import com.aliyun.openservices.log.request.GetLogStoreRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author chenmingliang
 * @date 2024/05/14
 */
@ExtendWith(MockitoExtension.class)
public class SLSCloudLogServiceImplTest {

    @Mock
    Client client;

    @Mock
    ProjectJobTaskRepository taskRepository;

    @Mock
    NodeRepository nodeRepository;

    @Test
    public void testCreateLogStore() throws LogException {
        Mockito.when(client.CreateLogStore(Mockito.any(CreateLogStoreRequest.class))).thenReturn(null);
        LogConfigProperties.SLSConfig slsConfig = new LogConfigProperties.SLSConfig();
        slsConfig.setProject("projectName");
        SLSCloudLogServiceImpl slsCouldLogService = new SLSCloudLogServiceImpl(null, null, client, slsConfig, "TEST");
        invokeCreateLogstore(slsCouldLogService);

    }

    @Test
    public void queryOrCreateProject() throws LogException {
        Mockito.when(client.GetProject(Mockito.anyString())).thenReturn(null);
        LogConfigProperties.SLSConfig slsConfig = new LogConfigProperties.SLSConfig();
        slsConfig.setProject("projectName");
        SLSCloudLogServiceImpl slsCouldLogService = new SLSCloudLogServiceImpl(taskRepository, nodeRepository, client, slsConfig, "TEST");
        invokeGetProject(slsCouldLogService);

    }

    @Test
    public void queryQueryLogStore() throws LogException {
        Mockito.when(client.GetLogStore(Mockito.any(GetLogStoreRequest.class))).thenReturn(null);
        LogConfigProperties.SLSConfig slsConfig = new LogConfigProperties.SLSConfig();
        slsConfig.setProject("projectName");
        SLSCloudLogServiceImpl slsCouldLogService = new SLSCloudLogServiceImpl(null, null, client, slsConfig, "TEST");
        invokeGetLogStore(slsCouldLogService);

    }

    private void invokeGetLogStore(SLSCloudLogServiceImpl slsCouldLogService) {
        Method privateMethod = ReflectionUtils.findMethod(SLSCloudLogServiceImpl.class, "queryLogstore", String.class, String.class);
        if (privateMethod != null) {
            privateMethod.setAccessible(true);
            try {
                privateMethod.invoke(slsCouldLogService, "logStoreName", "projectName");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void invokeGetProject(SLSCloudLogServiceImpl slsCouldLogService) {
        Method privateMethod = ReflectionUtils.findMethod(SLSCloudLogServiceImpl.class, "queryOrCreateProject", String.class);
        if (privateMethod != null) {
            privateMethod.setAccessible(true);
            try {
                privateMethod.invoke(slsCouldLogService, "projectName");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void invokeCreateLogstore(SLSCloudLogServiceImpl slsCouldLogService) {
        Method privateMethod = ReflectionUtils.findMethod(SLSCloudLogServiceImpl.class, "createLogstore", String.class, String.class);
        if (privateMethod != null) {
            privateMethod.setAccessible(true);
            try {
                privateMethod.invoke(slsCouldLogService, "logStoreName", "projectName");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
