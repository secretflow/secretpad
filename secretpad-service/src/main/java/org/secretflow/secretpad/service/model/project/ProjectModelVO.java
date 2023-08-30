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
 * Project job model view object
 *
 * @author jiezi
 * @date 2023/5/31
 */
@Getter
@Setter
public class ProjectModelVO {
    /**
     * Model id
     */
    private String modelId;
    /**
     * Model name
     */
    private String name;
    /**
     * Model join list
     */
    private List<ModelJoin> joins;
    /**
     * Model param list
     */
    private List<ModelParam> params;

    /**
     * Model join information
     */
    @Setter
    @Getter
    @Builder
    public static class ModelJoin {
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

    /**
     * Model param
     */
    @Getter
    @Setter
    @Builder
    public static class ModelParam {
        /**
         * Model param name
         */
        private String name;
        /**
         * Model param value
         */
        private String value;
    }
}
