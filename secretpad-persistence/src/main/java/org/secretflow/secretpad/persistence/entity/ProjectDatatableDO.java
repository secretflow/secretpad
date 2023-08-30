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

package org.secretflow.secretpad.persistence.entity;

import org.secretflow.secretpad.persistence.converter.BaseObjectListJsonConverter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.Where;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project datatable data object
 *
 * @author xiaonan
 * @date 2023/5/25
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "project_datatable")
@SQLDelete(sql = "update project_datatable set is_deleted = 1 where datatable_id = ? and node_id =? and project_id = ?")
@SQLInsert(sql = "insert or replace into project_datatable (is_deleted, source, table_configs, datatable_id, node_id, project_id) values (?, ?, ?, ?, ?, ?)")
@Where(clause = "is_deleted = 0")
public class ProjectDatatableDO extends BaseAggregationRoot<ProjectDatatableDO> {

    /**
     * Project datatable unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Project datatable configuration list
     */
    @Column(name = "table_configs", nullable = false, columnDefinition = "text")
    @Convert(converter = TableConfigConverter.class)
    private List<TableColumnConfig> tableConfig = new ArrayList<>();

    /**
     * Project datatable source
     * IMPORTED or CREATED
     */
    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectDatatableSource source;

    /**
     * Get associateKey list from project datatable configuration list
     *
     * @return associateKey list
     */
    public List<String> getAssociateKey() {
        return this.getTableConfig().stream().filter(TableColumnConfig::isAssociateKey).map(TableColumn::getColName).collect(Collectors.toList());
    }

    /**
     * Get groupKey list from project datatable configuration list
     *
     * @return groupKey list
     */
    public List<String> getGroupKey() {
        return this.getTableConfig().stream().filter(TableColumnConfig::isGroupKey).map(TableColumn::getColName).collect(Collectors.toList());
    }

    /**
     * Get labelKey list from project datatable configuration list
     *
     * @return labelKey list
     */
    public List<String> getLabelKeys() {
        return this.getTableConfig().stream().filter(TableColumnConfig::isLabelKey).map(TableColumn::getColName).collect(Collectors.toList());
    }

    /**
     * Project datatable source class
     */
    public enum ProjectDatatableSource {
        /**
         * Imported from user operation
         */
        IMPORTED,
        /**
         * Created from project job
         */
        CREATED,
    }

    /**
     * Create a new project datatable DO class
     */
    public static class Factory {
        public static ProjectDatatableDO newProjectDatatable(String projectId, String nodeId, String datatableId,
                                                             List<TableColumnConfig> configs) {
            return ProjectDatatableDO.builder()
                    .upk(new UPK(projectId, nodeId, datatableId))
                    .source(ProjectDatatableSource.IMPORTED)
                    .tableConfig(configs)
                    .build();
        }
    }

    /**
     * Project datatable unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class UPK implements Serializable {
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
        @Column(name = "datatable_id", nullable = false, length = 64)
        private String datatableId;
    }

    /**
     * Project datatable basic configuration
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableColumn {
        /**
         * Column name
         */
        private String colName;
        /**
         * Column type
         */
        private String colType;
        /**
         * Column comment
         */
        private String colComment;
    }

    /**
     * Project datatable unique configuration, different from other projects
     */
    @Setter
    @Getter
    public static class TableColumnConfig extends TableColumn {
        /**
         * Whether association key
         */
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        @JsonProperty("isAssociateKey")
        private boolean isAssociateKey = false;
        /**
         * Whether group key
         */
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        @JsonProperty("isGroupKey")
        private boolean isGroupKey = false;
        /**
         * Whether label column
         */
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        @JsonProperty("isLabelKey")
        private boolean isLabelKey = false;

        /**
         * Create a new project datatable configuration via params
         *
         * @param column
         * @param isAssociateKey
         * @param isGroupKey
         * @param isLabelKey
         * @return project datatable configuration
         */
        public static TableColumnConfig from(TableColumn column, boolean isAssociateKey,
                                             boolean isGroupKey, boolean isLabelKey) {
            TableColumnConfig configVO = new TableColumnConfig();
            configVO.setColName(column.getColName());
            configVO.setColType(column.getColType());
            configVO.setColComment(column.getColComment());
            configVO.isAssociateKey = isAssociateKey;
            configVO.isGroupKey = isGroupKey;
            configVO.isLabelKey = isLabelKey;
            return configVO;
        }
    }

    @Converter
    public static class TableConfigConverter extends BaseObjectListJsonConverter<TableColumnConfig> {
        public TableConfigConverter() {
            super(TableColumnConfig.class);
        }
    }
}
