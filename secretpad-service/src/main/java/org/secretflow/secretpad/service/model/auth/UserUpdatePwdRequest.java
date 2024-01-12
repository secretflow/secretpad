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

package org.secretflow.secretpad.service.model.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * User update pwd request
 *
 * @author lihaixin
 * @date 2023/12/11
 */
@Data
public class UserUpdatePwdRequest {

    /**
     * User name
     */
    @Schema(description = "user name")
    private String name;

    /**
     * User  old password
     */
    @NotBlank
    @Length(min = 8, message = "password length is greater than 8 ")
    @Schema(description = "user old password")
    private String oldPasswordHash;

    /**
     * User new password
     */
    @NotBlank
    @Length(min = 8, message = "password length is greater than 8 ")
    @Schema(description = "user new password")
    private String newPasswordHash;

    /**
     * User confirm password
     */
    @NotBlank
    @Length(min = 8, message = "password length is greater than 8 ")
    @Schema(description = "user confirm password")
    private String confirmPasswordHash;

}
