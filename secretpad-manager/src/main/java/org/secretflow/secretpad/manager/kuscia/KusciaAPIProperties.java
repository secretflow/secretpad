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

package org.secretflow.secretpad.manager.kuscia;

import lombok.Data;
import org.secretflow.v1alpha1.factory.TlsConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ApiLite channel properties
 *
 * @author yansi
 * @date 2023/5/8
 */
@Data
@ConfigurationProperties(prefix = "kusciaapi", ignoreInvalidFields = true)
public class KusciaAPIProperties {
    /**
     * ApiLite address
     */
    private String address;
    /**
     * ApiLite token file
     */
    private String tokenFile;
    /**
     * ApiLite tls config
     */
    private TlsConfig tls = new TlsConfig();
}
