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

import org.secretflow.secretpad.service.configuration.ScqlConfig;
import org.secretflow.secretpad.service.configuration.SecretFlowVersionConfig;
import org.secretflow.secretpad.service.configuration.SecretpadComponentConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author lufeng
 * @date 2024/12/18
 */
@ExtendWith(MockitoExtension.class)
public class ScqlConfigTest {
    @InjectMocks
    private ScqlConfig scqlConfig;

    @Mock
    private SecretpadComponentConfig secretpadComponentConfig;

    @Mock
    private SecretFlowVersionConfig secretFlowVersionConfig;

    @Test
    public void testScqlConfig_case1() {
        ReflectionTestUtils.setField(scqlConfig, "scqlEnabled", false);
        scqlConfig.initialize();
    }
    @Test
    public void testScqlConfig_case2() {
        ReflectionTestUtils.setField(scqlConfig, "scqlEnabled", true);
        scqlConfig.initialize();
    }


}
