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

package org.secretflow.secretpad.service.model.node;

import org.secretflow.secretpad.manager.integration.model.NodeResultDTO;
import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Node results view object
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Builder
@Setter
@Getter
@ToString
public class NodeResultsVO {

    /**
     * Domain data id
     */
    @Schema(description = "domain data id")
    private String domainDataId;

    /**
     * Node result name
     */
    @Schema(description = "node result name")
    private String productName;

    /**
     * Node result datatable type
     */
    @Schema(description = "datatable type")
    private String datatableType;

    /**
     * Node result source projectId
     */
    @Schema(description = "node result source project id")
    private String sourceProjectId;

    /**
     * Node result source project name
     */
    @Schema(description = "node result source project name")
    private String sourceProjectName;

    /**
     * Relative uri
     */
    @Schema(description = "uri")
    private String relativeUri;

    /**
     * Job id
     */
    @Schema(description = "job id")
    private String jobId;

    /**
     * The training flow the node result belongs to
     */
    @Schema(description = "training flow")
    private String trainFlow;

    /**
     * Result pull from tee status
     * Status：FAILED/SUCCESS/RUNNING
     */
    @Schema(description = "result pull from tee status")
    private String pullFromTeeStatus;
    /**
     * Result pull from tee error message
     */
    @Schema(description = "result pull from tee error message if failed")
    private String pullFromTeeErrMsg;
    /**
     * Start time of the node result
     */
    @Schema(description = "start time")
    private String gmtCreate;

    /**
     * project computeMode mpc,tee
     */
    @Schema(description = "computeMode")
    private String computeMode;

    /**
     * Build node results view object from node result data transfer object
     *
     * @param nodeResultDTO node result data transfer object
     * @return node results view object
     */
    public static NodeResultsVO fromNodeResultDTO(NodeResultDTO nodeResultDTO, TeeNodeDatatableManagementDO managementDO) {
        return NodeResultsVO.builder()
                .domainDataId(nodeResultDTO.getDomainDataId())
                .productName(nodeResultDTO.getResultName())
                .datatableType(nodeResultDTO.getResultKind())
                .sourceProjectId(nodeResultDTO.getSourceProjectId())
                .sourceProjectName(nodeResultDTO.getSourceProjectName())
                .relativeUri(nodeResultDTO.getRelativeUri())
                .jobId(nodeResultDTO.getJobId())
                .trainFlow(nodeResultDTO.getTrainFlow())
                .pullFromTeeStatus(null == managementDO ? "" : managementDO.getStatus().name())
                .pullFromTeeErrMsg(null == managementDO ? "" : managementDO.getErrMsg())
                .gmtCreate(nodeResultDTO.getGmtCreate())
                .computeMode(nodeResultDTO.getComputeMode())
                .build();
    }

}
