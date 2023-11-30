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

import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Project job view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
public class ProjectJobVO extends ProjectJobBaseVO {
    /**
     * Check if the job is finished
     */
    private Boolean finished = false;
    /**
     * Graph view object
     */
    @Schema(description = "graph view object")
    private GraphDetailVO graph;

    ProjectJobVO(ProjectJobDO jobDO) {
        super(jobDO);
    }

    /**
     * Build a new project job view object from project job data object and graph detail view object
     *
     * @param jobDO project job data object
     * @param graph graph detail view object
     * @return a new project job view object
     */
    public static ProjectJobVO from(ProjectJobDO jobDO, GraphDetailVO graph) {
        ProjectJobVO vo = new ProjectJobVO(jobDO);
        vo.graph = graph;
        vo.finished = jobDO.isFinished();
        return vo;
    }
}

