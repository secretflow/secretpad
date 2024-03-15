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
package org.secretflow.secretpad.service.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author yutu
 * @date 2023/11/21
 */
@Data
@Configuration
@ConfigurationProperties("secretpad.component")
public class SecretpadComponentConfig {
    /**
     * hide components configs
     */
    private List<String> hide;
}

@Data
class ComponentConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 291568296509217011L;

    private String app;
    private String name;
    private boolean hide;
}