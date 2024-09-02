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

import org.secretflow.secretpad.service.model.datatable.TableColumnConfigVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Project datatable view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
public class ProjectDatatableVO extends ProjectDatatableBaseVO {
    /**
     * Datatable column config list
     */
    @Schema(description = "datatable column config list")
    private List<TableColumnConfigVO> configs;

    /**
     * Build project datatable view object via filling datatableId, datatableName and datatable column config list
     *
     * @param datatableId   target datatableId
     * @param datatableName target datatableName
     * @param configs       datatable column config list
     */
    public ProjectDatatableVO(String datatableId, String datatableName, List<TableColumnConfigVO> configs) {
        super(datatableId, datatableName, null);
        this.setConfigs(configs);
    }
}
