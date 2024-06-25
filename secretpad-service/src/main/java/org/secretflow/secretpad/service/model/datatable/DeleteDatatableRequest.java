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

import org.secretflow.secretpad.common.annotation.OneOfType;
import org.secretflow.secretpad.common.constant.DomainDataConstants;
import org.secretflow.secretpad.common.enums.DataTableTypeEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Delete datatable request
 *
 * @author guyu
 * @date 2023/8/18
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDatatableRequest {
    /**
     * Node id
     */
    @Schema(description = "node id")
    private String nodeId;
    /**
     * Datatable id
     */
    @Schema(description = "datatable id")
    private String datatableId;
    /**
     * Tee node id, it can be blank and has default value
     */
    @Schema(description = "tee node id")
    private String teeNodeId;
    /**
     * Datasource id, it can be blank and has default value
     */
    @Schema(description = "datasource id")
    private String datasourceId;
    /**
     * Relative uri
     */
    @Schema(description = "relative uri")
    private String relativeUri;

    @Schema(description = "table type")
    @OneOfType(types = {DomainDataConstants.HTTP_DATATABLE_TYPE, DomainDataConstants.DEFAULT_DATATABLE_TYPE})
    private String type = DataTableTypeEnum.CSV.name();
}
