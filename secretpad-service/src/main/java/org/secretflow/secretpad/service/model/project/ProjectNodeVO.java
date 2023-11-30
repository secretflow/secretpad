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

import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.projection.ProjectNodeProjection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project node view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
@Builder
public class ProjectNodeVO {
    /**
     * Node id
     */
    @Schema(description = "node id")
    private String nodeId;
    /**
     * Node name
     */
    @Schema(description = "node name")
    private String nodeName;

    /**
     * Node Type
     */
    @Schema(description = "node type")
    private String nodeType;
    /**
     * Datatable list of the node, it is empty for list requests
     */
    @Schema(description = "datatable list of the node, it is empty for list requests")
    private List<ProjectDatatableBaseVO> datatables;

    /**
     * Build a new project node view object from project node projection and datatable data transfer object list
     *
     * @param pnp project node projection
     * @param dts datatable data transfer object list
     * @return a new project node view object
     */
    public static ProjectNodeVO from(ProjectNodeProjection pnp, @Nullable List<DatatableDTO> dts) {
        return ProjectNodeVO.builder()
                .nodeId(pnp.getProjectNodeDO().getUpk().getNodeId())
                .datatables(CollectionUtils.isEmpty(dts) ? null
                        : dts.stream().map(dt -> new ProjectDatatableBaseVO(dt.getDatatableId(),
                        dt.getDatatableName())).collect(Collectors.toList()))
                .nodeName(pnp.getNodeName())
                .nodeType(pnp.getNodeType())
                .build();
    }

    /**
     * Datatable bundle class
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class DatatableBundle {
        /**
         * Project datatable data object
         */
        private ProjectDatatableDO projectDatatableDO;
        /**
         * Datatable data transfer object
         */
        private DatatableDTO datatableDTO;
    }
}
