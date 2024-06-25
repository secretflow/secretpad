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

import org.secretflow.secretpad.common.annotation.OneOfType;
import org.secretflow.secretpad.manager.integration.model.DatatableSchema;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

/**
 * @author : xiaonan.fhn
 * @version : 0.1 2023/05/15 16:46
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDataByDataSourceRequest {

    /**
     * nodeId
     */
    @Schema(description = "nodeId")
    private String nodeId;

    /**
     * name
     */
    @Schema(description = "name")
    private String name;

    /**
     * tablePath
     */
    @Schema(description = "tablePath")
    private String tablePath;

    /**
     * datasourceId
     */
    @Schema(description = "datasourceId")
    private String datasourceId;
    /**
     * datasource name
     */
    @NotBlank
    private String datasourceName;

    /**
     * datatable type
     */
    @NotBlank
    @OneOfType(types = {"oss"})
    private String datasourceType;


    /**
     * description
     */
    @Schema(description = "description")
    private String description;

    /**
     * datatableSchema
     */
    @Schema(description = "datatableSchema")
    private List<DatatableSchema> datatableSchema;

}
