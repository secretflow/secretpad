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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.common.constant.ProjectConstants;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.sql.Update;

/**
 * Create project request
 *
 * @author yansi
 * @date 2023/5/25
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {
    /**
     * Project name
     */
    @Schema(description = "project name")
    @NotBlank(message = "name not empty")
    @Size(max = 32, message = "name max length is 32")
    private String name;
    /**
     * Project description
     */
    @Schema(description = "project description")
    @Size(max = 128, message = "description max length is 128")
    private String description;

    /**
     * computeMode mpc,tee
     */
    @Schema(description = "computeMode", requiredMode = Schema.RequiredMode.REQUIRED)
    private String computeMode = ProjectConstants.ComputeModeEnum.MPC.name();

    /**
     * tee node domainId
     */
    @Schema(description = "teeNodeId", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "teeNodeId not empty", groups = {Update.class})
    private String teeNodeId;

    private String computeFunc = ProjectConstants.ComputeFuncEnum.ALL.name();

}
