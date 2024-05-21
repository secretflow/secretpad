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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskRepository;
import org.secretflow.secretpad.service.impl.SLSCloudLogServiceImpl;
import org.secretflow.secretpad.service.model.graph.GraphNodeCloudLogsRequest;
import org.secretflow.secretpad.service.properties.LogConfigProperties;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.QueriedLog;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.CreateLogStoreRequest;
import com.aliyun.openservices.log.request.CreateProjectRequest;
import com.aliyun.openservices.log.response.GetLogsResponse;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author chenmingliang
 * @date 2024/04/24
 */

@Import(CloudLogReadyControllerTest.TestConfig.class)
public class CloudLogReadyControllerTest extends ControllerTest {

    @Autowired
    private Client client;
    @MockBean
    private ProjectJobTaskRepository taskRepository;

    @MockBean
    private NodeRepository nodeRepository;

    @TestConfiguration
    public static class TestConfig {
        @MockBean
        private Client client;

        @Bean
        public SLSCloudLogServiceImpl slsCouldLogService(ProjectJobTaskRepository taskRepository, NodeRepository nodeRepository) throws LogException {
            LogConfigProperties.SLSConfig slsConfig = new LogConfigProperties.SLSConfig();
            slsConfig.setHost("host");
            slsConfig.setAk("ak");
            slsConfig.setSk("sk");
            slsConfig.setProject("project");
            return new SLSCloudLogServiceImpl(taskRepository, nodeRepository, client, slsConfig, "TEST");
        }

    }


    @Test
    public void testSLSUnReadyErr1() throws Exception {
        assertErrorCode(() -> {
            GraphNodeCloudLogsRequest request = FakerUtils.fake(GraphNodeCloudLogsRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(CloudLogController.class, "getCloudLog", GraphNodeCloudLogsRequest.class))
                    .content(JsonUtils.toJSONString(request));
        }, JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
    }

    @Test
    public void testSLSUnReady1() throws Exception {
        assertResponse(() -> {
            GraphNodeCloudLogsRequest request = new GraphNodeCloudLogsRequest();
            request.setProjectId("project");
            return MockMvcRequestBuilders.post(getMappingUrl(CloudLogController.class, "getCloudLog", GraphNodeCloudLogsRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    public void testSLSReady() throws Exception {
        assertResponse(() -> {
            GraphNodeCloudLogsRequest request = new GraphNodeCloudLogsRequest();
            request.setProjectId("project");
            request.setGraphNodeId("graph");
            Mockito.when(taskRepository.findLatestTasks(request.getProjectId(), request.getGraphNodeId())).thenReturn(Optional.of(FakerUtils.fake(ProjectTaskDO.class)));
            Map map = new HashMap();
            map.put("x-log-progress", "Complete");
            GetLogsResponse response = new GetLogsResponse(map);
            LogItem item = FakerUtils.fake(LogItem.class);
            QueriedLog queriedLog = new QueriedLog("", item);
            LogContent content = FakerUtils.fake(LogContent.class);
            item.SetLogContents(Lists.newArrayList(content));
            response.setLogs(Lists.newArrayList(queriedLog));
            Mockito.when(client.GetLogs(Mockito.anyString(), Mockito.anyString(), Mockito.eq(0), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(response);
            Mockito.when(client.createProject(Mockito.any(CreateProjectRequest.class))).thenReturn(null);
            Mockito.when(client.CreateLogStore(Mockito.any(CreateLogStoreRequest.class))).thenReturn(null);
            return MockMvcRequestBuilders.post(getMappingUrl(CloudLogController.class, "getCloudLog", GraphNodeCloudLogsRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

}
