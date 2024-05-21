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

import org.secretflow.secretpad.service.configuration.IpBlockConfig;
import org.secretflow.secretpad.service.util.IpFilterUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author yutu
 * @date 2024/03/11
 */
public class IpFilterUtilTest {
    private final List<String> ipList = List
            .of("0.0.0.0/32",
                    "10.0.0.0/8",
                    "0.0.0.0/32",
                    "11.0.0.0/8",
                    "30.0.0.0/8",
                    "100.64.0.0/10",
                    "172.16.0.0/12",
                    "192.168.0.0/16",
                    "33.0.0.0/8"
            );

    @Test
    void test() {
        IpBlockConfig ipBlockConfig = new IpBlockConfig();
        ipBlockConfig.setEnable(true);
        ipBlockConfig.setList(ipList);
        IpFilterUtil ipFilterUtil = new IpFilterUtil(ipBlockConfig);
        Assertions.assertTrue(ipFilterUtil.isIpInRange("10.2.2.2"));
        Assertions.assertFalse(ipFilterUtil.isIpInRange("9.2.2.2"));
        Assertions.assertFalse(ipFilterUtil.isIpInRange("9.2.2"));
        String url = "https://12";
        Assertions.assertFalse(ipFilterUtil.urlIsIpInRange(url));
        url = "https://127.0.0.1";
        Assertions.assertFalse(ipFilterUtil.urlIsIpInRange(url));
        url = "https://10.0.0.1";
        Assertions.assertTrue(ipFilterUtil.urlIsIpInRange(url));
        ipBlockConfig.setEnable(false);
        ipFilterUtil = new IpFilterUtil(ipBlockConfig);
        Assertions.assertFalse(ipFilterUtil.isIpInRange("10.2.2.2"));
    }
}