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

package org.secretflow.secretpad.persistence.entity;

import org.secretflow.secretpad.persistence.converter.BaseObjectListJsonConverter;
import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;
import org.secretflow.secretpad.persistence.model.PartyDataSource;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/18
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "project_model_pack")
@Where(clause = "is_deleted = 0")
public class ProjectModelPackDO extends BaseAggregationRoot<ProjectModelPackDO> {

    @Id
    @Column(name = "modelId", nullable = false, length = 64)
    private String modelId;

    @Column(name = "initiator", nullable = false, length = 64)
    private String initiator;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Column(name = "model_name", nullable = false, length = 256)
    private String modelName;

    @Column(name = "model_desc", columnDefinition = "text")
    private String modelDesc;

    /**
     * {@link org.secretflow.secretpad.common.enums.ModelStatsEnum}
     */
    @Column(name = "model_stats", nullable = false, length = 1)
    private Integer modelStats;

    @Column(name = "serving_id", nullable = false, length = 64)
    private String servingId;

    @Column(name = "sample_tables", nullable = false, columnDefinition = "text")
    private String sampleTables;

    @Column(name = "model_list", nullable = false, columnDefinition = "text")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> modelList;

    @Column(name = "train_id", nullable = false)
    private String trainId;

    @Column(name = "graph_detail", columnDefinition = "text")
    private String graphDetail;

    @Column(name = "model_report_id", nullable = false)
    private String modelReportId;

    @Column(name = "model_datasource", nullable = false)
    @Convert(converter = PartyDataSourceConverter.class)
    private List<PartyDataSource> partyDataSources;


    @Converter
    public static class PartyDataSourceConverter extends BaseObjectListJsonConverter<PartyDataSource> {
        public PartyDataSourceConverter() {
            super(PartyDataSource.class);
        }
    }
}
