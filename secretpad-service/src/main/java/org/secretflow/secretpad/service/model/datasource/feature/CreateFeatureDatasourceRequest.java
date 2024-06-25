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

package org.secretflow.secretpad.service.model.datasource.feature;

import org.secretflow.secretpad.common.annotation.OneOfType;
import org.secretflow.secretpad.service.model.datatable.TableColumnVO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/24
 */
@Getter
@Setter
public class CreateFeatureDatasourceRequest {

    @NotBlank
    private String nodeId;

    @NotBlank
    @Size(max = 32, message = "feature table must less then 32 characters")
    private String featureTableName;

    @NotBlank
    @OneOfType(types = {"HTTP"})
    private String type;

    @Size(max = 100, message = "feature table desc must less then 100 characters")
    private String desc;

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "Invalid URL format")
    private String url;

    @NotEmpty
    private List<TableColumnVO> columns;

    private String datasourceId;
}
