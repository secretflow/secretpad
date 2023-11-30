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

package org.secretflow.secretpad.persistence.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Count projection data
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountProjection {
    /**
     * Count projection id
     */
    private String id;
    /**
     * Count
     */
    private Long count;

    /**
     * Collect target count projection list to Map by count projection id and count
     *
     * @param counts
     * @return Map of count projection id and count
     */
    public static Map<String, Long> toMap(List<CountProjection> counts) {
        return counts.stream().collect(Collectors.toMap(CountProjection::getId, CountProjection::getCount));
    }
}

