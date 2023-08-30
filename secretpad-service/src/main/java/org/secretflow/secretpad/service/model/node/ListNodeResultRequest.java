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

package org.secretflow.secretpad.service.model.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * List node result request
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ListNodeResultRequest {

    /**
     * Node id
     */
    @Schema(description = "node id")
    String nodeId;

    /**
     * How many pieces of data are in each page
     */
    @Schema(description = "page size")
    Integer pageSize;

    /**
     * What page is currently requested? Note that starting at 1 represents the first page
     */
    @Schema(description = "page number")
    Integer pageNumber;

    /**
     * Rules for filtering by kind, not filled when listing all outputs
     */
    @Schema(description = "kind filters")
    List<String> kindFilters;

    /**
     * Rules for producer filtering by data vendor, not filled when listing all outputs
     */
    @Schema(description = "data vendor filter")
    String dataVendorFilter;

    /**
     * Filter by any name, such as table name, project, training stream and so on
     * Note: Because the current version (20230630) does not use name, the front end is required to fill in domain data id here
     * Filter by ID
     */
    @Schema(description = "result name filter")
    String nameFilter;

    /**
     * The rules are sorted by time
     * 1. Ascending：ascending
     * 2. Descending：descending
     */
    @Schema(description = "time sorting rule")
    String timeSortingRule;

}
