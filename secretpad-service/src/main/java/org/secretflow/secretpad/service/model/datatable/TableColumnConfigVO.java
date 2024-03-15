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

import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO.TableColumnConfig;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Table column config view object
 *
 * @author yansi
 * @date 2023/5/10
 */
@Setter
@Getter
public class TableColumnConfigVO extends TableColumnVO {
    /**
     * Association key or not
     */
    @Schema(description = "association key or not")
    @JsonProperty("isAssociateKey")
    private boolean isAssociateKey;
    /**
     * Group key or not
     */
    @Schema(description = "group key or not")
    @JsonProperty("isGroupKey")
    private boolean isGroupKey;

    /**
     * Label key or not
     */
    @Schema(description = "label key or not")
    @JsonProperty("isLabelKey")
    private boolean isLabelKey;

    /**
     * key protection or not, false by default
     */
    @Schema(description = "key protection or not")
    @JsonProperty("isProtection")
    private boolean isProtection;

    /**
     * Convert table column config view object from table column config
     *
     * @param columnConfig table column config
     * @return table column config view object
     */
    public static TableColumnConfigVO from(TableColumnConfig columnConfig) {
        TableColumnConfigVO configVO = new TableColumnConfigVO();
        configVO.setColName(columnConfig.getColName());
        configVO.setColType(columnConfig.getColType());
        configVO.setColComment(columnConfig.getColComment());
        configVO.isAssociateKey = columnConfig.isAssociateKey();
        configVO.isGroupKey = columnConfig.isGroupKey();
        configVO.isLabelKey = columnConfig.isLabelKey();
        configVO.isProtection = columnConfig.isProtection();
        return configVO;
    }

    /**
     * Batch convert table column config view object from table column config
     *
     * @param columns table column config list
     * @return table column config view object list
     */
    public static List<TableColumnConfigVO> from(List<TableColumnConfig> columns) {
        return columns.stream().map(TableColumnConfigVO::from).collect(Collectors.toList());
    }
}
