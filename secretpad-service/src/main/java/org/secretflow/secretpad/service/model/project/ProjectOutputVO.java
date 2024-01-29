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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * project graph output
 * @author yutu
 * @date 2023/11/22
 */
@Getter
@Setter
@Builder
public class ProjectOutputVO {
    /**
     * Project id
     */
    @Schema(description = "project id")
    private String projectId;
    /**
     * Project name
     */
    @Schema(description = "project name")
    private String projectName;
    /**
     * Project description
     */
    @Schema(description = "project description")
    private String description;
    /**
     * List of added nodes output
     */
    @Schema(description = "List of added nodes output")
    private List<ProjectGraphOutputVO> nodes;
    /**
     * The count of graph
     */
    @Schema(description = "the count of graph")
    private Integer graphCount;
    /**
     * The count of job
     */
    @Schema(description = "the count of job")
    private Integer jobCount;
    /**
     * Start time of the project
     */
    @Schema(description = "start time of the project")
    private String gmtCreate;
    /**
     * computeMode pipeline: ,hub:
     */
    @Schema(description = "computeMode")
    private String computeMode;
}