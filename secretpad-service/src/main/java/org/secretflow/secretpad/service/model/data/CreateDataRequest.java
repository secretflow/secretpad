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

package org.secretflow.secretpad.service.model.data;

import org.secretflow.secretpad.manager.integration.model.DatatableSchema;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

/**
 * Create data request
 *
 * @author : xiaonan.fhn
 * @date 2023/5/15
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDataRequest {

    /**
     * Node id
     */
    @NotBlank
    @Schema(description = "node id")
    private String nodeId;

    /**
     * The data file name, it must be the same as that of the source file
     */
    @Schema(description = "data file name")
    @NotBlank
    private String name;

    /**
     * The real name of the file, passed only to the back end, is the field that the user needs to manipulate, derived from the value returned by the back end in the uplink mouth
     */
    @Schema(description = "real name of the file")
    private String realName;

    /**
     * Specific table name, user manually filled
     */
    @Schema(description = "specific table name")
    @NotBlank
    private String tableName;

    /**
     * Datatable description
     */
    @Schema(description = "datatable description")
    private String description;

    /**
     * Datatable schema
     */
    @Schema(description = "datatable schema")
    private List<DatatableSchema> datatableSchema;


}
