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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datatable column config param
 *
 * @author yansi
 * @date 2023/5/25
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TableColumnConfigParam {
    /**
     * Column name, it can not be blank
     */
    @NotBlank
    @Schema(description = "column name")
    private String colName;
    /**
     * Association key or not. False by default
     */
    @Schema(description = "association key or not, false by default")
    @JsonProperty("isAssociateKey")
    private boolean isAssociateKey = false;
    /**
     * Group key or not. False by default
     */
    @Schema(description = "group key or not, false by default")
    @JsonProperty("isGroupKey")
    private boolean isGroupKey = false;
    /**
     * Label key or not. False by default
     */
    @Schema(description = "label key or not, false by default")
    @JsonProperty("isLabelKey")
    private boolean isLabelKey = false;
}