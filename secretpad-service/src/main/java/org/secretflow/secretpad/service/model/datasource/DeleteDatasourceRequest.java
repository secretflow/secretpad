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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Getter
@Setter
public class DeleteDatasourceRequest {

    @NotBlank(message = "ownerId id cannot be blank")
    private String ownerId;

    @NotBlank(message = "datasource id cannot be blank")
    private String datasourceId;

    @NotBlank(message = "type cannot be blank")
    @OneOfType(types = {DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE, DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE, DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE})
    private String type;
}
