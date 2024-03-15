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
package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.errorcode.FeatureTableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.IpFilter;
import org.secretflow.secretpad.service.configuration.IpBlockConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author yutu
 * @date 2024/03/11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpFilterUtil {
    private final IpBlockConfig ipBlockConfig;

    public boolean isIpInRange(String ip) {
        if (StringUtils.isEmpty(ip) || !ipBlockConfig.isEnable()) {
            return false;
        }
        for (String i : ipBlockConfig.getList()) {
            try {
                if (IpFilter.isIpInRange(ip, i)) {
                    return true;
                }
            } catch (UnknownHostException e) {
                log.warn("UnknownHostException {}", ip);
            }
        }
        return false;
    }

    public String extractIPFromURL(String url) {
        try {
            URL urlObj = new URL(url);
            InetAddress addr = InetAddress.getByName(urlObj.getHost());
            return addr.getHostAddress();
        } catch (Exception e) {
            log.error("url is error", e);
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_IP_NOT_KNOWN, e, url);
        }
    }

    public boolean urlIsIpInRange(String url) {
        String ip = extractIPFromURL(url);
        return isIpInRange(ip);
    }
}