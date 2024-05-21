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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.test.context.TestPropertySource;

/**
 * @author chixian
 * @date 2024/03/29
 */
@Slf4j
@TestPropertySource(properties = {
        "kusciaapi.protocol=notls"
})
public class MasterRouteInitTest extends ControllerTest {

    @RepeatedTest(2)
    public void testRun() {
        log.info("Initializer node route for center mode");
    }
}
