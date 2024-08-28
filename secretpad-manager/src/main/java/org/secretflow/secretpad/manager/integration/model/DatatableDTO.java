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

package org.secretflow.secretpad.manager.integration.model;

import org.secretflow.secretpad.common.constant.DomainDataConstants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;

import lombok.*;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: will be replaced by ApiLite
 * Datatable data transfer object
 *
 * @author jiezi
 * @date 2023/05/16
 */
@Getter
@Setter
@Builder
public class DatatableDTO {

    /**
     * Node id
     */
    private String nodeId;

    /**
     * Datatable id
     */
    private String datatableId;

    /**
     * Datatable name
     */
    private String datatableName;

    /**
     * Relative Uri
     */
    private String relativeUri;

    /**
     * Datasource id
     */
    private String datasourceId;

    /**
     * Datasource type
     */
    private String datasourceType;

    /**
     * Datasource name
     */
    private String datasourceName;

    /**
     * Datatable attributes
     */
    private Map<String, String> attributes;

    /**
     * Datatable status
     */
    private String status;

    /**
     * Datatable type
     */
    private String type;

    /**
     * Datatable schema
     * TODO: for ApiLite, batch query will not return this field.
     */
    private List<TableColumnDTO> schema;

    private OdpsPartitionParam partition;


    /**
     * Convert DatatableDTO from DomainData
     *
     * @param domainData
     * @return DatatableDTO
     */
    public static DatatableDTO fromDomainData(Domaindata.DomainData domainData) {
        return DatatableDTO.builder()
                .datatableId(domainData.getDomaindataId())
                .datatableName(domainData.getName())
                .nodeId(domainData.getAuthor())
                .datasourceType(domainData.getAttributesMap().get(DomainDatasourceConstants.DATASOURCE_TYPE) == null ? DomainDataConstants.DEFAULT_LOCAL_DATASOURCE_TYPE : domainData.getAttributesMap().get(DomainDatasourceConstants.DATASOURCE_TYPE))
                .datasourceName(domainData.getAttributesMap().get(DomainDatasourceConstants.DATASOURCE_NAME) == null ? DomainDataConstants.DEFAULT_LOCAL_DATASOURCE_NAME : domainData.getAttributesMap().get(DomainDatasourceConstants.DATASOURCE_NAME))
                .relativeUri(domainData.getRelativeUri())
                .datasourceId(domainData.getDatasourceId())
                .attributes(domainData.getAttributesMap())
                .status(domainData.getStatus())
                .type(domainData.getType())
                .schema(domainData.getColumnsList().stream().map(it ->
                                new TableColumnDTO(it.getName(), it.getType(), it.getComment()))
                        .collect(Collectors.toList()))
                .partition(ObjectUtils.isEmpty(domainData.getPartition()) ? null : OdpsPartitionParam.fromPartition(domainData.getPartition()))
                .build();
    }

    /**
     * Convert DatatableDTO schema to table column config list
     *
     * @param schema
     * @return table column config list
     */
    public static List<ProjectDatatableDO.TableColumnConfig> toTableConfig(List<TableColumnDTO> schema) {
        if (!CollectionUtils.isEmpty(schema)) {
            return schema.stream().map(columnDTO -> {
                ProjectDatatableDO.TableColumnConfig columnConfig = new ProjectDatatableDO.TableColumnConfig();
                columnConfig.setColName(columnDTO.getColName());
                columnConfig.setColType(columnDTO.getColType());
                columnConfig.setColComment(columnDTO.getColComment());
                return columnConfig;
            }).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableColumnDTO {

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

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class NodeDatatableId {

        /**
         * Node id
         */
        private String nodeId;

        /**
         * Datatable id
         */
        private String datatableId;

        private NodeDatatableId(String nodeId, String datatableId) {
            this.nodeId = nodeId;
            this.datatableId = datatableId;
        }

        /**
         * Create a new NodeDatatableId class by nodeId and datatableId
         *
         * @param nodeId
         * @param datatableId
         * @return new NodeDatatableId class
         */
        public static NodeDatatableId from(String nodeId, String datatableId) {
            return new NodeDatatableId(nodeId, datatableId);
        }

    }

}
