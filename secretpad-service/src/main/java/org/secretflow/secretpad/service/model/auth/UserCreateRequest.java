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

import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Setter
@Getter
@ToString
public class UserCreateRequest {
    /**
     * CENTER or EDGE
     */
    @NotBlank
    private UserOwnerTypeEnum ownerType;

    @NotBlank
    private String ownerId;
    /**
     * User name
     */
    @Schema(description = "user name")
    @NotBlank
    private String name;

    /**
     * User password
     */
    @NotBlank
    @Schema(description = "user password")
    private String passwordHash;


}
