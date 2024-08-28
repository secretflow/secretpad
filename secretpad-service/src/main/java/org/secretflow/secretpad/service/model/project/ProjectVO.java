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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Project view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
@Builder
public class ProjectVO {
    /**
     * vote invite patties's vote information
     */
    Set<PartyVoteInfoVO> partyVoteInfos;
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
     * List of added nodes
     */
    @Schema(description = "list of added nodes")
    private List<ProjectNodeVO> nodes;
    /**
     * List of added insts
     */
    @Schema(description = "list of added insts")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<ProjectInstVO> insts;
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
     * computeMode mpc,tee
     */
    @Schema(description = "computeMode")
    private String computeMode;
    /**
     * tee node domainId
     */
    @Schema(description = "teeNodeId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String teeNodeId;
    /**
     * project approval status
     * {@link org.secretflow.secretpad.common.enums.ProjectStatusEnum}
     */
    private String status;
    /**
     * project initiator nodeId
     */
    private String initiator;
    /**
     * project initiator nodeName
     */
    private String initiatorName;
    /**
     * computeFunc
     * {@link org.secretflow.secretpad.common.constant.ProjectConstants.ComputeFuncEnum}
     */
    private String computeFunc;

    /**
     * project vote id
     */
    private String voteId;

}
