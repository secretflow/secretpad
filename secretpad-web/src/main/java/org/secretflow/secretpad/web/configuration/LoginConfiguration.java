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

import org.secretflow.secretpad.web.interceptor.LoginInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for user login
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Configuration
public class LoginConfiguration implements WebMvcConfigurer {
    /**
     * The interceptor for user login
     */
    private final LoginInterceptor loginInterceptor;

    @Autowired
    public LoginConfiguration(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    /**
     * Add interceptors and path patterns to registry
     *
     * @param registry target interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/v1alpha1/**")
                .addPathPatterns("/sync")
                .addPathPatterns("/api/logout")
                .excludePathPatterns("/api/v1alpha1/inst/node/register");
    }
}
