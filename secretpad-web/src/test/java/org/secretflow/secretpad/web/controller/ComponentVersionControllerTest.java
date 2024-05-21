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


import org.secretflow.secretpad.service.configuration.SecretFlowVersionConfig;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * ComponentVersionControllerTest
 *
 * @author lufeng
 * @date 2023/4/24
 */
public class ComponentVersionControllerTest extends ControllerTest {


    @Mock
    private SecretFlowVersionConfig secretFlowVersionConfig;

    @Resource
    private ComponentVersionController componentVersionController;

    /**
     * Test TEE deployment mode
     *
     * @throws Exception
     */

    @Test
    void listTEEComponents() throws Exception {
        ReflectionTestUtils.setField(componentVersionController, "deployMode", "TEE");
        assertResponse(() -> MockMvcRequestBuilders.post(getMappingUrl(ComponentVersionController.class, "listVersion")));
    }

    /**
     * Test MPC deployment mode
     *
     * @throws Exception
     */

    @Test
    void listMPCComponents() throws Exception {
        ReflectionTestUtils.setField(componentVersionController, "deployMode", "MPC");
        assertResponse(() -> MockMvcRequestBuilders.post(getMappingUrl(ComponentVersionController.class, "listVersion")));
    }

    /**
     * Test ALL-IN-ONE deployment mode
     *
     * @throws Exception
     */

    @Test
    void listALLINONEComponents() throws Exception {
        ReflectionTestUtils.setField(componentVersionController, "deployMode", "ALL-IN-ONE");
        assertResponse(() -> MockMvcRequestBuilders.post(getMappingUrl(ComponentVersionController.class, "listVersion")));
    }

}