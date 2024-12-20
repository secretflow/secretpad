/*
 * Copyright 2023 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.web.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Request utils
 *
 * @author yansi
 * @date 2023/5/10
 */
@Slf4j
public class RequestUtils {
    /**
     *
     **/
    public static final String LOCAL_IP_V6 = "0:0:0:0:0:0:0:1";
    public static final String LOCAL_IP_BASE64 = "MTI3LjAuMC4x";
    public static final List<String> HEADERS = List.of("X-Forwarded-For",
            "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "X-Real-IP");

    /**
     * Get current httpRequest from request holder attributes
     *
     * @return http servlet request
     */
    public static HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        return null;
    }

    /**
     * Get current request URI from request
     *
     * @return
     */
    public static String getCurrentRequestMethodURI() {
        HttpServletRequest currentHttpRequest = getCurrentHttpRequest();
        if (Objects.isNull(currentHttpRequest)) {
            return StringUtils.EMPTY;
        }
        return currentHttpRequest.getRequestURI();
    }

    public static String getRemoteHost() {
        String ipAddresses = "";
        HttpServletRequest request = getCurrentHttpRequest();
        Assert.notNull(request, "request is null");
        Optional<String> first = HEADERS.stream().filter(header -> isValidIP(request, header)).findFirst();
        if (first.isPresent()) {
            ipAddresses = request.getHeader(first.get());
            if (!ipAddresses.isEmpty()) {
                ipAddresses = ipAddresses.split(",")[0];
                return ipAddresses;
            }
        }
        /* not found */
        if (StringUtils.isEmpty(ipAddresses)) {
            ipAddresses = request.getRemoteAddr();
            if (LOCAL_IP_V6.equalsIgnoreCase(ipAddresses) || LOCAL_IP_BASE64.equalsIgnoreCase(Base64.getEncoder().encodeToString(ipAddresses.getBytes(StandardCharsets.UTF_8)))) {
                try {
                    ipAddresses = getHostIP();
                } catch (SocketException e) {
                    log.error("getHostIP error", e);
                }
            }
        }
        return ipAddresses;
    }


    private static boolean isValidIP(HttpServletRequest request, String header) {
        String ipAddresses = request.getHeader(header);
        return ipAddresses != null && !ipAddresses.isEmpty() && !"unknown".equalsIgnoreCase(ipAddresses);
    }


    private static String getHostIP() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();

        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                continue;
            }

            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress ip = addresses.nextElement();
                if (ip instanceof Inet4Address) {
                    return ip.getHostAddress();
                }
            }
        }
        return null;
    }

}
