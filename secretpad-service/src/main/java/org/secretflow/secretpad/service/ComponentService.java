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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.component.ComponentVersion;
import org.secretflow.secretpad.service.model.graph.CompListVO;
import org.secretflow.secretpad.service.model.graph.ComponentKey;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;

import com.secretflow.spec.v1.ComponentDef;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component service interface
 *
 * @author yansi
 * @date 2023/6/7
 */
public interface ComponentService {
    Map<String, ComponentDef> SF_HIDE_COMPONENTS = new ConcurrentHashMap<>();

    /**
     * List components and build component list view object
     *
     * @return component list view object
     */
    Map<String, CompListVO> listComponents();

    /**
     * Get component by component key
     *
     * @param key component key
     * @return componentDef
     */
    ComponentDef getComponent(ComponentKey key);

    /**
     * Batch get component list by component keys
     *
     * @param keys component keys
     * @return componentDef list
     */
    List<ComponentDef> batchGetComponent(List<ComponentKey> keys);

    /**
     * List components from international location file then collect to map
     *
     * @return Map of file string and component
     */
    Object listComponentI18n();

    /**
     * Check component if secretpad component via graph node information
     *
     * @param node graph node information
     * @return true is secretpad component, false is not
     */
    boolean isSecretpadComponent(GraphNodeInfo node);

    /**
     * list component version
     *
     * @return SecretFlowVersionConfig
     */
    ComponentVersion listComponentVersion(String deployMode);
}