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

package org.secretflow.secretpad.service.model.datasource;

import org.secretflow.secretpad.common.annotation.OneOfType;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Getter
@Setter
public class CreateDatasourceRequest {

    @NotBlank(message = "ownerId id cannot be blank")
    private String ownerId;

    @NotEmpty(message = "nodes can not be empty, you should select at least one node")
    private List<String> nodeIds;

    @NotBlank(message = "type cannot be blank")
    @OneOfType(types = {DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE, DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE, DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE, DomainDatasourceConstants.DEFAULT_TDSQL_DATASOURCE_TYPE})
    private String type;

    @NotBlank(message = "name can not be empty")
    @Size(max = 32, message = "name length cannot exceed 32 characters")
    private String name;

    @NotNull
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "type",
            defaultImpl = OssDatasourceInfo.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = OssDatasourceInfo.class, name = DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE),
            @JsonSubTypes.Type(value = OdpsDatasourceInfo.class, name = DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE),
            @JsonSubTypes.Type(value = MysqlDatasourceInfo.class, name = DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE),
            @JsonSubTypes.Type(value = TdsqlDatasourceInfo.class, name = DomainDatasourceConstants.DEFAULT_TDSQL_DATASOURCE_TYPE)
    })
    private @Valid DataSourceInfo dataSourceInfo;
}
