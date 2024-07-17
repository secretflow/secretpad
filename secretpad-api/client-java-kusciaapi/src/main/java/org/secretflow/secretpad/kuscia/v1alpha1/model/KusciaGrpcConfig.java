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

package org.secretflow.secretpad.kuscia.v1alpha1.model;

import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Set;

/**
 * kuscia grpc config
 *
 * @author yutu
 * @date 2024/06/12
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class KusciaGrpcConfig {

    @NotNull(message = "domainId cannot be null or empty")
    private String domainId;

    @NotNull(message = "host cannot be null or empty")
    private String host;

    @Min(value = 0, message = "port must be less  than 0")
    @Max(value = 65535, message = "port must be greater than 65535")
    private int port;

    @NotNull(message = "protocol cannot be null or empty")
    private KusciaProtocolEnum protocol;

    @NotNull(message = "mode cannot be null or empty")
    private KusciaModeEnum mode;

    private String token;

    private String certFile;

    private String keyFile;

    public void validateAndProcess() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<KusciaGrpcConfig>> violations = validator.validate(this);

        for (ConstraintViolation<KusciaGrpcConfig> violation : violations) {
            throw new IllegalArgumentException("Invalid KusciaGrpcConfig: " + violation.getMessage());
        }

        if (this.host.contains(":")) {
            this.port = Integer.parseInt(host.split(":")[1]);
            this.host = host.split(":")[0];
        }
        switch (protocol) {
            case MTLS, TLS -> {
                if (StringUtils.isEmpty(certFile) || StringUtils.isEmpty(keyFile) || StringUtils.isEmpty(token)) {
                    throw new IllegalArgumentException("certFile,keyFile,token cannot be null when protocol is TLS or MTLS");
                }
                if (new File(this.token).exists()) {
                    try {
                        this.token = FileUtils.readFile2String(token);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Invalid KusciaGrpcConfig: token is not a file path", e);
                    }
                }
            }
            case NOTLS -> {
            }
            default ->
                    throw new IllegalArgumentException("Invalid KusciaGrpcConfig: protocol must be TLS or MTLS or NOTLS");
        }

    }
}