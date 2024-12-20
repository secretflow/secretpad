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

package org.secretflow.secretpad.service.model.inst;

import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InstRegisterRequest {

    @JsonProperty("token")
    @NotBlank
    private String instToken;

    @JsonProperty("domainId")
    @NotBlank
    @Pattern(regexp = "^[a-z0-9]([a-z0-9.-]{0,61}[a-z0-9])?$", message = "domainId must be a valid domainId")
    private String nodeId;

    @NotBlank
    private String host;

    /**
     * port  for kuscia api
     */
    @Min(value = 0, message = "port must be less  than 0")
    @Max(value = 65535, message = "port must be greater than 65535")
    private int port;

    /**
     * port for transport
     */
    @Min(value = 0, message = "port must be less  than 0")
    @Max(value = 65535, message = "port must be greater than 65535")
    private int transPort;

    @NotBlank
    private String mode;

    @NotBlank
    private String protocol;

    @JsonIgnore
    @NotNull
    private MultipartFile certFile;

    @JsonIgnore
    @NotNull
    private MultipartFile keyFile;

    @JsonIgnore
    @NotNull
    private MultipartFile tokenFile;

    public boolean isValid() {
        // copy
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<InstRegisterRequest>> violations = validator.validate(this);

        for (ConstraintViolation<InstRegisterRequest> violation : violations) {
            throw new IllegalArgumentException("Invalid InstRegisterRequest: " + violation.getMessage());
        }

        if (!KusciaModeEnum.P2P.name().equalsIgnoreCase(mode)) {
            throw new IllegalArgumentException("Invalid mode, only p2p supported");
        }

        KusciaProtocolEnum.getByName(protocol);

        if (fileNameCheck()) {
            throw new IllegalArgumentException("Invalid file name, use client.crt client.pem token");
        }

        return fileSizeQualified(List.of(certFile, keyFile, tokenFile));
    }


    private boolean fileSizeQualified(List<MultipartFile> files) {
        return files.stream().allMatch(this::fileSizeQualified);
    }

    private boolean fileSizeQualified(MultipartFile file) {
        if (file == null) {
            return false;
        }
        return file.getSize() <= FileUtils.CERT_FILE_MAX_SIZE;
    }

    private boolean fileNameCheck() {
        return StringUtils.equals("client.crt", certFile.getName())
                && StringUtils.equals("client.pem", keyFile.getName())
                && StringUtils.equals("token", tokenFile.getName());
    }

}
