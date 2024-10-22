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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.integration.datasource.odps.OdpsConfig;
import org.secretflow.secretpad.manager.integration.datasource.odps.OdpsManager;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author yutu
 * @date 2024/07/23
 */
@Slf4j
public class OdpsTest {

    @Test
    void testOdpsBuild() {
        OdpsManager odpsManager = new OdpsManager();

        Assertions.assertThrows(IllegalArgumentException.class, () -> odpsManager.testConnection(null));

        OdpsConfig odpsConfig = OdpsConfig.builder()
                .accessId("***")
                .accessKey("***")
                .endpoint("http://xxxxxx/api")
                .project("1213")
                .build();
        Assertions.assertThrows(SecretpadException.class, () -> odpsManager.testConnection(odpsConfig));
    }
}