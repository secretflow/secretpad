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

package org.secretflow.secretpad.manager.integration.datasource.odps;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * odps config
 *
 * @author yutu
 * @date 2024/07/23
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OdpsConfig implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "odps accessId cannot be null or empty")
    private String accessId;

    @NotNull(message = "odps accessKey cannot be null or empty")
    private String accessKey;

    @NotNull(message = "odps project cannot be null or empty")
    private String project;

    @NotNull(message = "odps endpoint cannot be null or empty")
    private String endpoint;

    public void validate() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<OdpsConfig>> violations = validator.validate(this);

        for (ConstraintViolation<OdpsConfig> violation : violations) {
            throw new IllegalArgumentException("Invalid OdpsConfig: " + violation.getMessage());
        }
    }
}