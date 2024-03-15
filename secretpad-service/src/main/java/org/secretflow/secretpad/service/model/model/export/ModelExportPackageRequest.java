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

package org.secretflow.secretpad.service.model.model.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * model export package request
 *
 * @author yutu
 * @date 2024/01/29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelExportPackageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 87654321L;

    @NotBlank
    @JsonProperty("projectId")
    private String projectId;

    @NotBlank
    @JsonProperty("graphId")
    private String graphId;

    @NotBlank
    @JsonProperty("trainId")
    private String trainId;

    @NotBlank
    @Length(max = 32)
    @JsonProperty("modelName")
    private String modelName;

    @Length(max = 200)
    @JsonProperty("modelDesc")
    private String modelDesc;

    @NotBlank
    @JsonProperty("graphNodeOutPutId")
    private String graphNodeOutPutId;

    @NotNull
    @Valid
    @JsonProperty("modelPartyConfig")
    private List<ModelPartyConfig> modelPartyConfig;

    @NotNull
    @Valid
    @JsonProperty("modelComponent")
    private List<ModelComponent> modelComponent;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ModelExportPackageRequest that)) {
            return false;
        }

        return new EqualsBuilder().append(projectId, that.projectId).append(graphId, that.graphId).append(modelName, that.modelName).append(modelDesc, that.modelDesc).append(graphNodeOutPutId, that.graphNodeOutPutId).append(modelPartyConfig, that.modelPartyConfig).append(modelComponent, that.modelComponent).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(projectId).append(graphId).append(modelName).append(modelDesc).append(graphNodeOutPutId).append(modelPartyConfig).append(modelComponent).toHashCode();
    }
}