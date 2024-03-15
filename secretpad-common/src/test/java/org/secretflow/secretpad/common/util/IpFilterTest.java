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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.secretflow.secretpad.common.util.IpFilter.isIpInRange;

/**
 * @author yutu
 * @date 2024/03/11
 */
public class IpFilterTest {
    @Test
    void test() throws Exception {
        String cidr = "33.0.0.0/8";
        String ipToCheck = "33.123.45.67";
        Assertions.assertTrue(isIpInRange(ipToCheck, cidr));
        ipToCheck = "34.123.45.67";
        Assertions.assertFalse(isIpInRange(ipToCheck, cidr));
    }
}