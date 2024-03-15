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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author yutu
 * @date 2024/03/11
 */
public final class IpFilter {
    private IpFilter() {
    }

    public static boolean isIpInRange(String ip, String cidr) throws UnknownHostException {
        String[] parts = cidr.split("/");
        String address = parts[0];
        int prefix;
        if (parts.length < 2) {
            prefix = 0;
        } else {
            prefix = Integer.parseInt(parts[1]);
        }
        long ipLong = ipToLong(InetAddress.getByName(ip));
        long subnetLong = ipToLong(InetAddress.getByName(address));
        long mask = -(1L << (32 - prefix));
        return (subnetLong & mask) == (ipLong & mask);
    }

    private static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }
}