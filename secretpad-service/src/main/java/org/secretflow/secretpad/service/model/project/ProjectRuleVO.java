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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Project rule view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Setter
@Getter
@Builder
public class ProjectRuleVO {
    /**
     * Rule id
     */
    private String ruleId;
    /**
     * Rule name
     */
    private String name;
    /**
     * Rule join list
     */
    private List<RuleJoin> joins;
    /**
     * Rule content
     */
    private String content;

    /**
     * Rule join
     */
    @Setter
    @Getter
    @Builder
    public static class RuleJoin {
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
