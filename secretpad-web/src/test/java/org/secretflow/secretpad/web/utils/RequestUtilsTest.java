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

package org.secretflow.secretpad.web.utils;

import org.secretflow.secretpad.web.util.RequestUtils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * @author yutu
 * @date 2024/08/05
 */
@Slf4j
public class RequestUtilsTest {

    private static final String TEST_URI = "/api/v1/test";
    public static final List<String> HEADERS = List.of("X-Forwarded-For",
            "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "X-Real-IP");

    @BeforeEach
    public void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(TEST_URI);
        HEADERS.forEach(header -> request.addHeader(header, ""));
        request.setRemoteAddr("128.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    public void cleanup() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void requestUtilsTest() {
        Assertions.assertNotNull(RequestUtils.getCurrentHttpRequest());
        Assertions.assertEquals(TEST_URI, RequestUtils.getCurrentRequestMethodURI());
        Assertions.assertEquals("128.0.0.1", RequestUtils.getRemoteHost());
    }
}