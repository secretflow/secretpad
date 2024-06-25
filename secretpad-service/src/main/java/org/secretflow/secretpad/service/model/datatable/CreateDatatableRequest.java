/*
 * Copyright 2024 Ant Group Co., Ltd.
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
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author lufeng
 * @date 2024/5/22
 */
@Data
public class CreateDatatableRequest {

    /**
     * node ID
     */
    @NotBlank
    private String nodeId;

    /**
     * table name
     */
    @NotBlank
    @Size(max = 32, message = "feature table must less then 32 characters")
    private String datatableName;

    /**
     * datasource id
     */
    @NotBlank
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
    @OneOfType(types = {DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE})
    private String datasourceType;

    /**
     * table description
     */
    @Size(max = 100, message = "feature table desc must less then 100 characters")
    private String desc;

    /**
     * table url
     */
    @NotBlank(message = "relativeUri cannot be blank")
    private String relativeUri;

    /**
     * table columns
     */
    @NotEmpty
    private List<TableColumnVO> columns;
}
