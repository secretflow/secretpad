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

package org.secretflow.secretpad.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Add response header filter
 *
 * @author yansi
 * @date 2023/7/1
 */
@Component
public class AddResponseHeaderFilter extends OncePerRequestFilter {
    @Autowired
    private SecretPadResponse secretPadResponse;

    /**
     * Filter response header via map of extra headers
     *
     * @param request     httpServletRequest
     * @param response    httpServletResponse
     * @param filterChain filter chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Map<String, String> extraResponseHeaders = secretPadResponse.getExtraHeaders();
        if (!CollectionUtils.isEmpty(extraResponseHeaders)) {
            extraResponseHeaders.forEach((key, value) -> {
                response.addHeader(key, value);
            });
        }
        filterChain.doFilter(request, response);
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "secretpad.response")
    public static class SecretPadResponse {
        private Map<String, String> extraHeaders;
    }
}
