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

import lombok.*;

import java.util.List;

/**
 * Project federal table view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFedtableVO {
    /**
     * Federal table id
     */
    private String fedtableId;
    /**
     * Federal table name
     */
    private String name;
    /**
     * Federal table join list information
     */
    private List<FedtableJoin> joins;
    /**
     * Federal table column config list
     */
    private List<TableColumnConfigVO> schema;

    /**
     * Federal table join information
     */
    @Getter
    @Setter
    @Builder
    public static class FedtableJoin {
        /**
         * Node id
         */
        private String nodeId;
        /**
         * Datatable id
         */
        private String datatableId;
        /**
         * Relative uri
         */
        private String relativeUri;
    }
}
