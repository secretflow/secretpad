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
public class ModelPartyPathResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 87654321L;

    @NotBlank
    @JsonProperty("nodeId")
    private String nodeId;

    @NotBlank
    @JsonProperty("nodeName")
    private String nodeName;

    @NotBlank
    @JsonProperty("dataSource")
    private String dataSource;

    @NotBlank
    @JsonProperty("dataSourcePath")
    private String dataSourcePath;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ModelPartyPathResponse that)) {
            return false;
        }

        return new EqualsBuilder().append(nodeId, that.nodeId).append(nodeName, that.nodeName).append(dataSource, that.dataSource).append(dataSourcePath, that.dataSourcePath).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(nodeId).append(nodeName).append(dataSource).append(dataSourcePath).toHashCode();
    }
}