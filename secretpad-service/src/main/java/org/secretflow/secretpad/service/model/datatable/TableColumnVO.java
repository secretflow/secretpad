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
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO.TableColumn;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Table column view object
 *
 * @author yansi
 * @date 2023/5/10
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnVO {
    /**
     * Column name
     */
    @Schema(description = "column name")
    private String colName;
    /**
     * Column type
     */
    @Schema(description = "column type")
    private String colType;
    /**
     * Column comment
     */
    @Schema(description = "column comment")
    private String colComment;

    /**
     * Convert table column view object from table column
     *
     * @param column table column
     * @return table column view object
     */
    public static TableColumnVO from(TableColumn column) {
        TableColumnVO columnVO = new TableColumnConfigVO();
        columnVO.setColName(column.getColName());
        columnVO.setColType(column.getColType());
        columnVO.setColComment(column.getColComment());
        return columnVO;
    }

    /**
     * Convert table column view object from table column data transfer object
     *
     * @param column table column data transfer object
     * @return table column view object
     */
    public static TableColumnVO from(DatatableDTO.TableColumnDTO column) {
        TableColumnVO columnVO = new TableColumnConfigVO();
        columnVO.setColName(column.getColName());
        columnVO.setColType(column.getColType());
        columnVO.setColComment(column.getColComment());
        return columnVO;
    }
}