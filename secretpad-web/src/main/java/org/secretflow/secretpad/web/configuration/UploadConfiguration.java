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

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * Configuration for Upload
 *
 * @author : xiaonan.fhn
 * @date 2023/07/03
 */
@Configuration
public class UploadConfiguration {
    private static final String NOT_LIMIT_SIZE = "-1";
    @Value("${secretpad.upload-file.max-file-size:-1}")
    private String maxFileSize;
    @Value("${secretpad.upload-file.max-request-size:-1}")
    private String maxRequestSize;

    /**
     * Create a new bean for multipart config element
     *
     * @return a new multipart config element bean
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        if (!maxFileSize.equals(NOT_LIMIT_SIZE)) {
            factory.setMaxFileSize(DataSize.parse(maxFileSize));
        }
        if (!maxRequestSize.equals(NOT_LIMIT_SIZE)) {
            factory.setMaxRequestSize(DataSize.parse(maxRequestSize));
        }
        return factory.createMultipartConfig();
    }

}
