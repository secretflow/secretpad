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
import lombok.Getter;
import lombok.Setter;

/**
 * Node result data transfer object
 *
 * @author : xiaonan.fhn
 * @date 2023/06/08
 */
@Builder
@Setter
@Getter
public class NodeResultDTO {

    /**
     * Data id in domain
     */
    private String domainDataId;

    /**
     * Result name
     */
    private String resultName;

    /**
     * Result kind
     */
    private String resultKind;

    /**
     * Project id from source
     */
    private String sourceProjectId;

    /**
     * Project name from source
     */
    private String sourceProjectName;

    /**
     * Training flow
     */
    private String trainFlow;

    /**
     * Start time
     */
    private String gmtCreate;

    /**
     * Relative Uri
     */
    private String relativeUri;

    /**
     * Job id
     */
    private String jobId;

    /**
     * project computeMode
     */
    private String computeMode;

}
