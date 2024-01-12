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

import org.secretflow.secretpad.common.dto.EnvDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;

/**
 * environment service
 *
 * @author beiwei
 * @date 2023/9/13
 */
public interface EnvService {

    /**
     * get platform type
     *
     * @return {@link PlatformTypeEnum}
     */
    PlatformTypeEnum getPlatformType();

    /**
     * get platform nade id
     *
     * @return platform node id
     */
    String getPlatformNodeId();

    /**
     * get environment value
     *
     * @return {@link EnvDTO}
     */
    EnvDTO getEnv();

    /**
     * check center platform
     *
     * @return boolean
     */
    Boolean isCenter();

    /**
     * check autonomy platform
     * @return boolean
     */
    Boolean isAutonomy();

    /**
     * check embedded node
     *
     * @param nodeID nodeID
     * @return boolean
     */
    Boolean isEmbeddedNode(String nodeID);

    /**
     * check Current Node Environment
     *
     * @param nodeID nodeId
     * @return boolean
     */
    Boolean isCurrentNodeEnvironment(String nodeID);

    Boolean isP2pEdge();

}
