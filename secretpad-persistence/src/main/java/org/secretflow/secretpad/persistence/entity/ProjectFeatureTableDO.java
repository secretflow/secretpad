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

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/26
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "project_feature_table")
public class ProjectFeatureTableDO extends BaseAggregationRoot<ProjectFeatureTableDO> {

    @EmbeddedId
    private ProjectFeatureTableDO.UPK upk;

    @Column(name = "table_configs", nullable = false, columnDefinition = "text")
    @Convert(converter = ProjectDatatableDO.TableConfigConverter.class)
    private List<ProjectDatatableDO.TableColumnConfig> tableConfig = new ArrayList<>();

    /**
     * Project datatable source
     * IMPORTED or CREATED
     */
    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectDatatableDO.ProjectDatatableSource source;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumns({
            @JoinColumn(name = "feature_table_id", referencedColumnName = "feature_table_id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "node_id", referencedColumnName = "node_id",
                    insertable = false, updatable = false),
            @JoinColumn(name = "datasource_id", referencedColumnName = "datasource_id",
                    insertable = false, updatable = false)
    })
    private FeatureTableDO featureTable;

    @Override
    @JsonIgnore
    public String getProjectId() {
        return this.upk.projectId;
    }

    @Override
    @JsonIgnore
    public String getNodeId() {
        if (StringUtils.equals(DomainConstants.DomainEmbeddedNodeEnum.tee.name(), this.upk.nodeId)) {
            return null;
        }
        return this.upk.nodeId;
    }

    @Override
    @JsonIgnore
    public List<String> getNodeIds() {
        return super.getNodeIds();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 2140509531215517812L;
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId;
        /**
         * Node id
         */
        @Column(name = "node_id", nullable = false, length = 64)
        private String nodeId;
        /**
         * Datatable id
         */
        @Column(name = "feature_table_id", nullable = false, length = 64)
        private String featureTableId;

        @Column(name = "datasource_id", nullable = false, length = 64)
        private String datasourceId;
    }

    public static class Factory {
        public static ProjectFeatureTableDO newProjectFeatureTable(String projectId, String nodeId, String featureTableId,
                                                                   List<ProjectDatatableDO.TableColumnConfig> configs, String datasourceId) {
            return ProjectFeatureTableDO.builder()
                    .upk(new ProjectFeatureTableDO.UPK(projectId, nodeId, featureTableId, StringUtils.isBlank(datasourceId) ? DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID : datasourceId))
                    .source(ProjectDatatableDO.ProjectDatatableSource.IMPORTED)
                    .tableConfig(configs)
                    .build();
        }
    }
}
