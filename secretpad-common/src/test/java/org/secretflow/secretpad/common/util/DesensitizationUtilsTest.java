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

package org.secretflow.secretpad.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author yutu
 * @date 2024/08/08
 */
@Slf4j
public class DesensitizationUtilsTest {
    @Test
    void testDesensitization() {
        String s = DesensitizationUtils.akSkDesensitize("1233112312313");
        log.info("akSkDesensitize {}", s);
        String s1 = DesensitizationUtils.idCardDesensitize("1233112312313");
        log.info("idCardDesensitize {}", s1);
        String s2 = DesensitizationUtils.mobileDesensitize("13332111111");
        log.info("mobileDesensitize {}", s2);
    }
}