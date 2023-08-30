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

package org.secretflow.secretpad.service.model.datatable;

import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.javatuples.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Authorized project view object
 *
 * @author yansi
 * @date 2023/5/10
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthProjectVO {
    /**
     * Project id
     */
    @Schema(description = "project id")
    private String projectId;

    /**
     * Project name
     */
    @Schema(description = "project name")
    private String name;

    /**
     * Association key list
     */
    @Schema(description = "association key list")
    private List<String> associateKeys;

    /**
     * Group key list
     */
    @Schema(description = "group key list")
    private List<String> groupKeys;

    /**
     * Label key list
     */
    @Schema(description = "label key list")
    private List<String> labelKeys;

    /**
     * Authorized time
     */
    @Schema(description = "authorized time")
    private String gmtCreate;

    /**
     * Convert authorized project view object list from pairs of project datatable data object and project data object
     *
     * @param pairs pairs of project datatable data object and project data object
     * @return authorized project view object list
     */
    public static List<AuthProjectVO> fromPairs(List<Pair<ProjectDatatableDO, ProjectDO>> pairs) {
        return pairs.stream().map(
                it -> new AuthProjectVO(
                        it.getValue1().getProjectId(),
                        it.getValue1().getName(),
                        it.getValue0().getAssociateKey(),
                        it.getValue0().getGroupKey(),
                        it.getValue0().getLabelKeys(),
                        DateTimes.toRfc3339(it.getValue0().getGmtCreate())
                )
        ).collect(Collectors.toList());
    }

}
