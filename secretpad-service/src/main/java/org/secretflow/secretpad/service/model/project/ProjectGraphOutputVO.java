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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @author yutu
 * @date 2023/10/05
 */
@Getter
@Setter
@Builder
public class ProjectGraphOutputVO {
    /**
     * graphId
     */
    private String graphId;
    /**
     * graphNodeId
     */
    private String graphNodeId;
    /**
     * outputs
     */
    private List<String> outputs;

    public static ProjectGraphOutputVO from(ProjectGraphNodeDO projectGraphNodeDO) {
        if (ObjectUtils.isEmpty(projectGraphNodeDO)) {
            return null;
        }
        return ProjectGraphOutputVO.builder()
                .graphId(projectGraphNodeDO.getUpk().getGraphId())
                .graphNodeId(projectGraphNodeDO.getUpk().getGraphNodeId())
                .outputs(projectGraphNodeDO.getOutputs())
                .build();
    }

}