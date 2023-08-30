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

package org.secretflow.secretpad.web.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for cross-origin resource sharing
 *
 * @author yansi
 * @date 2023/5/30
 */
@Configuration
@ConditionalOnProperty(name = "secretpad.cors.enabled", havingValue = "true")
public class CorsConfig implements WebMvcConfigurer {
    /**
     * The method types which be allowed
     */
    private static final String[] ALLOW_METHODS = new String[]{"GET", "POST", "PUT", "OPTIONS", "DELETE"};
    /**
     * The headers which be allowed
     */
    private static final String[] ALLOW_HEADERS = {"DNT", "X-CustomHeader", "Keep-Alive", "User-Agent", "X-Requested-With", "If-Modified-Since", "Cache-Control", "Content-Type", "Authorization"};

    /**
     * Add cross-origin resource sharing mapping to registry
     *
     * @param registry cross-origin resource registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedHeaders(ALLOW_HEADERS)
                .allowedMethods(ALLOW_METHODS)
                .maxAge(3600);
    }
}
