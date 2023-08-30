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

import org.secretflow.secretpad.persistence.model.GraphJobStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project job status data
 *
 * @author yansi
 * @date 2023/6/21
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectJobStatus {
    /**
     * Project id
     */
    private String projectId;
    /**
     * Job id
     */
    private String jobId;
    /**
     * Graph job status
     */
    private GraphJobStatus status;

    /**
     * Whether the graph job status is finished
     *
     * @return whether finished
     */
    public boolean isFinished() {
        return this.status == GraphJobStatus.SUCCEED || this.status == GraphJobStatus.FAILED || this.status == GraphJobStatus.STOPPED;
    }
}
