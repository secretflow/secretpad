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

package org.secretflow.secretpad.manager.integration.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Parameters for listing node results
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Data
@Builder
public class ListResultParam {

    /**
     * Node id
     */
    private String nodeId;

    /**
     * The page size of paging
     */
    private Integer pageSize;

    /**
     * The page number of paging
     */
    private Integer pageNumber;

    /**
     * The filters of kind condition
     */
    private List<String> kindFilters;

    /**
     * The filter of data vendor
     */
    private String dataVendorFilter;

    /**
     * The filter of name
     */
    private String nameFilter;
    /**
     * The rules are sorted by time
     * 1. Ascending order：ascending
     * 2. Descending order：descending
     * 3. No sort order: other or null
     */
    private String timeSortingRule;
}
