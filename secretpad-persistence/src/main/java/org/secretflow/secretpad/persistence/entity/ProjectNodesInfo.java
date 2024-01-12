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

package org.secretflow.secretpad.persistence.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Project nodes info interface
 *
 * @author zhiyin
 * @date 2023/10/19
 */
public interface ProjectNodesInfo extends Serializable {
    /**
     * if getNodeIds() == null use projectId find project_node_do get nodeIds
     *
     * @return if getNodeIds() == null use projectId find project_node_do get nodeIds
     */
    String getProjectId();

    /**
     * data sync filter by this list nodeIds
     *
     * @return data belong which nodeIds
     */
    List<String> getNodeIds();
}
