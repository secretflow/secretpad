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

package org.secretflow.secretpad.service.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chenmingliang
 * @date 2024/04/18
 */

@ConfigurationProperties(prefix = "secretpad.cloud-log")
@Getter
@Setter
public class LogConfigProperties {

    private SLSConfig sls;

    @Getter
    @Setter
    public static class SLSConfig {
        @NotBlank
        private String host;
        @NotBlank

        private String ak;
        @NotBlank

        private String sk;

        @NotBlank
        private String project;
    }
}
