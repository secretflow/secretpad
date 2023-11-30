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

import org.secretflow.secretpad.common.enums.PermissionUserTypeEnum;
import org.secretflow.secretpad.common.enums.ResourceTypeEnum;
import org.secretflow.secretpad.persistence.entity.SysResourceDO;

import java.util.List;
import java.util.Set;

/**
 * System Resource Service
 *
 * @author beiwei
 * @date 2023/9/15
 */
public interface SysResourcesBizService {

    /**
     * query by username
     * @param userType userType
     * @param userId userId or userNodeId or nodeId
     * @return List of {@link SysResourceDO}
     */
    List<SysResourceDO> queryResourceByUsername(PermissionUserTypeEnum userType, String userId);

    /**
     * query resource code by username
     * @param userType user type
     * @param resourceType resource type
     * @param userId user id
     * @return resource code
     */
    Set<String> queryResourceCodeByUsername(PermissionUserTypeEnum userType, ResourceTypeEnum resourceType, String userId);

}
