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

import org.secretflow.secretpad.persistence.model.ResultKind;

import lombok.*;

/**
 * Project result basic view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResultBaseVO {

    /**
     * Result kind enum
     */
    private ResultKind kind;
    /**
     * Ref id, domain data id in ApiLite
     */
    private String refId;

    /**
     * Build a new project result basic view object via merged project result
     *
     * @param result merged project result
     * @return a new project result basic view object
     */
    public static ProjectResultBaseVO of(MergedProjectResult result) {
        return new ProjectResultBaseVO(result.getKind(), result.getRefId());
    }
}
