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

package org.secretflow.secretpad.service.model.graph;

import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;

import lombok.Builder;
import lombok.Data;

/**
 * Graph meta view object
 *
 * @author yansi
 * @date 2023/5/25
 */
@Data
@Builder
public class GraphMetaVO {
    /**
     * Project id
     */
    private String projectId;
    /**
     * Graph id
     */
    private String graphId;
    /**
     * Graph name
     */
    private String name;
    /**
     * Graph owner id
     */
    private String ownerId;

    /**
     * Build graph meta view object from project graph data object
     *
     * @param graphDO project graph data object
     * @return graph meta view object
     */
    public static GraphMetaVO fromDO(ProjectGraphDO graphDO) {
        return GraphMetaVO.builder()
                .projectId(graphDO.getUpk().getProjectId())
                .graphId(graphDO.getUpk().getGraphId())
                .name(graphDO.getName())
                .ownerId(graphDO.getOwnerId())
                .build();
    }
}
