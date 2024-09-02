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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.persistence.projection.ProjectInstProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * @author lufeng
 * @date 2024/7/9
 */
@Data
@Builder
public class ProjectInstVO {

    /**
     * inst id
     */
    @Schema(description = "inst id")
    private String instId;
    /**
     * inst name
     */
    @Schema(description = "inst name")
    private String instName;

    /**
     * convert ProjectInstProjection to ProjectInstVO
     *
     * @param pip
     * @return
     */
    public static ProjectInstVO from(ProjectInstProjection pip) {
        return ProjectInstVO.builder()
                .instId(pip.getProjectInstDO().getNodeId())
                .instName(pip.getInstName())
                .build();
    }

}
