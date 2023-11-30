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

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * @author cml
 * @date 2023/11/17
 */
@Configuration
@ConditionalOnProperty(name = "secretpad.platform-type", havingValue = "CENTER")
@Getter
@Setter
public class EmbeddedNodeConfigProperties {

    @Value("${kusciaapi-lite-alice.address}")
    private String aliceAddress;
    @Value("${kusciaapi-lite-alice.tls.cert-file}")
    private String aliceCertFile;
    @Value("${kusciaapi-lite-alice.tls.key-file}")
    private String aliceKeyFile;
    @Value("${kusciaapi-lite-alice.tls.ca-file}")
    private String aliceCaFile;
    @Value("${kusciaapi-lite-alice.token-file}")
    private String aliceTokenFile;


    @Value("${kusciaapi-lite-bob.address}")
    private String bobAddress;
    @Value("${kusciaapi-lite-bob.tls.cert-file}")
    private String bobCertFile;
    @Value("${kusciaapi-lite-bob.tls.key-file}")
    private String bobKeyFile;
    @Value("${kusciaapi-lite-bob.tls.ca-file}")
    private String bobCaFile;
    @Value("${kusciaapi-lite-bob.token-file}")
    private String bobTokenFile;


}
