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

import org.secretflow.secretpad.common.util.SpringContextUtil;
import org.secretflow.secretpad.persistence.entity.ProjectModelServingDO;
import org.secretflow.secretpad.service.util.HttpUtils;
import org.secretflow.secretpad.service.util.IpFilterUtil;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author yutu
 * @date 2024/02/29
 */
@Slf4j

public class HttpUtilsTest {
    private MockWebServer mockWebServer;

    @Test
    void notExistUrl() {
        try (MockedStatic<SpringContextUtil> springContextUtilMockedStatic = Mockito.mockStatic(SpringContextUtil.class)) {
            IpFilterUtil ipFilterUtil = Mockito.mock(IpFilterUtil.class);
            springContextUtilMockedStatic.when(() -> SpringContextUtil.getBean(IpFilterUtil.class)).thenReturn(ipFilterUtil);
            String url = "http://127.0.0.1:8080/test";
            Mockito.when(ipFilterUtil.urlIsIpInRange(url)).thenReturn(Boolean.FALSE);
            Assertions.assertThrows(ConnectException.class, () -> HttpUtils.get(url));
            Assertions.assertThrows(NullPointerException.class, () -> HttpUtils.post("http://127.0.0.1:8080/test", null));
        }
    }

    @Test
    void existUrl() throws IOException {
        try (MockedStatic<SpringContextUtil> springContextUtilMockedStatic = Mockito.mockStatic(SpringContextUtil.class)) {
            IpFilterUtil ipFilterUtil = Mockito.mock(IpFilterUtil.class);
            springContextUtilMockedStatic.when(() -> SpringContextUtil.getBean(IpFilterUtil.class)).thenReturn(ipFilterUtil);
            Mockito.when(ipFilterUtil.urlIsIpInRange(Mockito.anyString())).thenReturn(Boolean.FALSE);
            mockWebServer = new MockWebServer();
            mockWebServer.start();
            String mockResponse = "{\"status\":\"ok\"}";
            mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));
            String url = mockWebServer.url("/test").toString();
            Assertions.assertDoesNotThrow(() -> HttpUtils.get(url));
            mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));
            String postUrl = mockWebServer.url("/test").toString();
            Assertions.assertDoesNotThrow(() -> HttpUtils.post(postUrl, Map.of()));
            mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(200));
            String detectionUrl = mockWebServer.url("/test").toString();
            Assertions.assertDoesNotThrow(() -> HttpUtils.detection(detectionUrl));
            mockWebServer.shutdown();
        }
    }

    @Test
    void existUrlError() throws IOException {
        try (MockedStatic<SpringContextUtil> springContextUtilMockedStatic = Mockito.mockStatic(SpringContextUtil.class)) {
            IpFilterUtil ipFilterUtil = Mockito.mock(IpFilterUtil.class);
            springContextUtilMockedStatic.when(() -> SpringContextUtil.getBean(IpFilterUtil.class)).thenReturn(ipFilterUtil);
            Mockito.when(ipFilterUtil.urlIsIpInRange(Mockito.anyString())).thenReturn(Boolean.FALSE);
            mockWebServer = new MockWebServer();
            mockWebServer.start();
            String mockResponse = "{\"status\":\"ok\"}";
            mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(500));
            String url = mockWebServer.url("/test").toString();
            Assertions.assertThrows(IOException.class, () -> HttpUtils.get(url));
            mockWebServer.enqueue(new MockResponse().setBody(mockResponse).setResponseCode(500));
            String postUrl = mockWebServer.url("/test").toString();
            Assertions.assertThrows(IOException.class, () -> HttpUtils.post(postUrl, Map.of("name", "alice")));
            mockWebServer.shutdown();
        }
    }

    @Test
    void test1() {
        ProjectModelServingDO.PartyEndpoints partyEndpoints1 = new ProjectModelServingDO.PartyEndpoints();
        partyEndpoints1.setEndpoints("zrbwqmqa-service.alice.svc");
        partyEndpoints1.setNodeId("alice");
        ProjectModelServingDO.PartyEndpoints partyEndpoints2 = new ProjectModelServingDO.PartyEndpoints();
        partyEndpoints2.setNodeId("bob");
        List<ProjectModelServingDO.PartyEndpoints> partyEndpoints = List.of(partyEndpoints1, partyEndpoints2);
        AtomicReference<String> endpoints = new AtomicReference<>("");
        partyEndpoints.forEach(e -> {
            if (StringUtils.isNotEmpty(e.getEndpoints())) {
                endpoints.set(e.getEndpoints().replaceAll("(-service\\.).*(\\.svc)", "$1#$2"));
            }
        });
        partyEndpoints = partyEndpoints.stream().peek(e -> {
            if (StringUtils.isEmpty(e.getEndpoints())) {
                e.setEndpoints(endpoints.get().replace("#", e.getNodeId()));
            }
        }).toList();
        Map<String, String> endpointsMap = partyEndpoints.stream().collect(Collectors.toMap(ProjectModelServingDO.PartyEndpoints::getNodeId, ProjectModelServingDO.PartyEndpoints::getEndpoints));
        log.info("partyEndpoints {} {}", partyEndpoints, endpointsMap);
    }

}