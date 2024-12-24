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

package org.secretflow.secretpad.service.configuration;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SecretFlowVersionConfig
 *
 * @author lufeng
 * @date 2023/4/22
 */
@Data
@Builder
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "secretpad.version")
public class SecretFlowVersionConfig {
    /**
     * secretpad image version
     */
    private String secretpadImage;
    /**
     * kuscia image version
     */
    private String kusciaImage;
    /**
     * secretflow image version
     */
    private String secretflowImage;
    /**
     * secretflow serving image version
     */
    private String secretflowServingImage;
    /**
     * tee app image version
     */
    private String teeAppImage;
    /**
     * tee dm image version
     */
    private String teeDmImage;
    /**
     * capsule manager sim image version
     */
    private String capsuleManagerSimImage;

    /**
     * data proxy image version
     */
    private String dataProxyImage;

    /**
     * scql image version
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String scqlImage;

}