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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.enums.PermissionTargetTypeEnum;
import org.secretflow.secretpad.common.enums.PermissionUserTypeEnum;
import org.secretflow.secretpad.common.enums.ResourceTypeEnum;
import org.secretflow.secretpad.persistence.entity.SysResourceDO;
import org.secretflow.secretpad.persistence.entity.SysRoleResourceRelDO;
import org.secretflow.secretpad.persistence.entity.SysUserPermissionRelDO;
import org.secretflow.secretpad.persistence.repository.SysResourceRepository;
import org.secretflow.secretpad.persistence.repository.SysRoleResourceRelRepository;
import org.secretflow.secretpad.persistence.repository.SysUserPermissionRelRepository;
import org.secretflow.secretpad.service.SysResourcesBizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author beiwei
 * @date 2023/9/15
 */
@Service
public class SysResourcesBizServiceImpl implements SysResourcesBizService {


    @Autowired
    private SysRoleResourceRelRepository roleResourceRelRepository;

    @Autowired
    private SysResourceRepository resourceRepository;

    @Autowired
    private SysUserPermissionRelRepository userPermissionRelRepository;


    private List<SysResourceDO> queryResourceByRoles(List<String> roles) {
        List<SysRoleResourceRelDO> byRoleIds = roleResourceRelRepository.findByRoleCodes(roles);

        List<String> resourceCodes = byRoleIds.stream().map(t -> t.getUpk().getResourceCode()).collect(Collectors.toList());
        return resourceRepository.findByCodes(resourceCodes);
    }

    /**
     *
     * @param userType userType
     * @param userId userId or userNodeId or nodeId
     * @return List of SysResourceDO
     */
    @Override
    public List<SysResourceDO> queryResourceByUsername(PermissionUserTypeEnum userType, String userId) {
        List<SysUserPermissionRelDO> userPermissionRelDOS = userPermissionRelRepository.findByName(userId);
        List<String> roleCodes = userPermissionRelDOS.stream()
                .filter(t -> userType.equals(t.getUserType()))
                .filter(t -> t.getTargetType().equals(PermissionTargetTypeEnum.ROLE))
                .map(t -> t.getUpk().getTargetCode())
                .collect(Collectors.toList());
        return queryResourceByRoles(roleCodes);
    }

    /**
     * query resource code by user name
     * @param userType user type
     * @param resourceType resource type
     * @param userId user id
     * @return resource code
     */
    @Override
    public Set<String> queryResourceCodeByUsername(PermissionUserTypeEnum userType, ResourceTypeEnum resourceType, String userId) {
        List<SysResourceDO> sysResourceDOS = queryResourceByUsername(userType, userId);
        return sysResourceDOS.stream()
                .filter(t -> t.getResourceType().equals(ResourceTypeEnum.API))
                .map(SysResourceDO::getResourceCode)
                .collect(Collectors.toSet());
    }
}
