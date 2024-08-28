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

import org.secretflow.secretpad.manager.integration.model.OdpsPartitionParam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Project datatable basic view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
@AllArgsConstructor
public class ProjectDatatableBaseVO {
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

    private OdpsPartitionParam partition;
}
