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

package org.secretflow.secretpad.manager.integration.model;

import org.secretflow.secretpad.common.enums.ModelStatsEnum;
import org.secretflow.secretpad.persistence.entity.ProjectModelPackDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.model.PartyDataSource;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author yutu
 * @date 2024/01/30
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelExportDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 876598765L;

    private String projectId;

    private String graphId;
    private String initiator;
    private String modelId;

    private String modelName;

    private String modelDesc;

    private String sampleTables;

    private List<String> modelList;

    private String jobId;

    private String taskId;
    private String trainId;
    private String graphDetail;
    private String modelReportId;

    private List<PartyDataSource> partyDataSources;

    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private GraphNodeTaskStatus status = GraphNodeTaskStatus.INITIALIZED;

    private String errMsg;

    public static ProjectModelPackDO of(ModelExportDTO modelExportDTO) {
        if (modelExportDTO == null) {
            return null;
        }
        return ProjectModelPackDO.builder()
                .modelId(modelExportDTO.getModelId())
                .initiator(modelExportDTO.getInitiator())
                .projectId(modelExportDTO.getProjectId())
                .modelName(modelExportDTO.getModelName())
                .modelDesc(modelExportDTO.getModelDesc())
                .modelStats(ModelStatsEnum.INIT.getCode())
                .sampleTables(modelExportDTO.getSampleTables())
                .trainId(modelExportDTO.getTrainId())
                .modelList(modelExportDTO.getModelList())
                .graphDetail(modelExportDTO.getGraphDetail())
                .modelReportId(modelExportDTO.getModelReportId())
                .partyDataSources(modelExportDTO.getPartyDataSources())
                .build();
    }
}