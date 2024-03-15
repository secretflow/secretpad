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
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * model export status request
 *
 * @author yutu
 * @date 2024/01/29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelPartyPathRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 87654321L;

    @NotBlank
    @JsonProperty("projectId")
    private String projectId;

    @NotBlank
    @JsonProperty("graphNodeId")
    private String graphNodeId;

    @NotBlank
    @JsonProperty("graphNodeOutPutId")
    private String graphNodeOutPutId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ModelPartyPathRequest that)) {
            return false;
        }

        return new EqualsBuilder().append(projectId, that.projectId).append(graphNodeId, that.graphNodeId).append(graphNodeOutPutId, that.graphNodeOutPutId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(projectId).append(graphNodeId).append(graphNodeOutPutId).toHashCode();
    }
}