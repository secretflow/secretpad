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

package org.secretflow.secretpad.service.model.datatable;

import org.secretflow.secretpad.manager.integration.model.DatatableDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Datatable view object
 *
 * @author yansi
 * @date 2023/5/10
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DatatableVO {

    /**
     * Datatable id
     */
    @Schema(description = "datatable id")
    private String datatableId;
    /**
     * Datatable name
     */
    @Schema(description = "datatable name")
    private String datatableName;
    /**
     * Datatable status
     * Status：Available，Unavailable
     */
    @Schema(description = "datatable status")
    private String status;
    /**
     * The data source id which it belongs to
     */
    private String datasourceId;
    /**
     * Relative uri
     */
    private String relativeUri;
    /**
     * Datatable type
     */
    private String type;

    /**
     * Datatable description
     */
    @Schema(description = "datatable description")
    private String description;
    /**
     * Datatable table column view object list
     */
    @Schema(description = "datatable table schema")
    private List<TableColumnVO> schema;
    /**
     * Authorized project list
     */
    @Schema(description = "authorized project list")
    private List<AuthProjectVO> authProjects;

    /**
     * Convert datatable view object from datatable data transfer object and authorized project list
     *
     * @param dto          datatable data transfer object
     * @param authProjects authorized project list
     * @return datatable view object
     */
    public static DatatableVO from(DatatableDTO dto, List<AuthProjectVO> authProjects) {
        return DatatableVO.builder()
                .datatableId(dto.getDatatableId())
                .datatableName(dto.getDatatableName())
                .status(dto.getStatus())
                .datasourceId(dto.getDatasourceId())
                .relativeUri(dto.getRelativeUri())
                .type(dto.getType())
                .description(dto.getAttributes().getOrDefault("description", ""))
                .schema(dto.getSchema().stream().map(TableColumnVO::from).collect(Collectors.toList()))
                .authProjects(authProjects)
                .build();
    }
}
