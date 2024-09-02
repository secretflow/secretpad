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

import org.secretflow.secretpad.common.constant.role.RoleCodeConstants;
import org.secretflow.secretpad.common.enums.PermissionTargetTypeEnum;
import org.secretflow.secretpad.common.enums.PermissionUserTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.SysUserPermissionRelDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.SysUserPermissionRelRepository;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.service.NodeUserService;
import org.secretflow.secretpad.service.model.auth.NodeUserCreateRequest;
import org.secretflow.secretpad.service.model.auth.NodeUserListByNodeIdRequest;
import org.secretflow.secretpad.service.model.auth.ResetNodeUserPwdRequest;
import org.secretflow.secretpad.service.model.auth.UserVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author beiwei
 * @date 2023/9/14
 */
@Service
@Slf4j
public class NodeUserServiceImpl implements NodeUserService {
    @Autowired
    private UserAccountsRepository userAccountsRepository;

    @Autowired
    private SysUserPermissionRelRepository permissionRelRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Override
    @Transactional
    public void create(NodeUserCreateRequest request) {
        NodeDO byNodeId = nodeRepository.findByNodeId(request.getNodeId());
        if (byNodeId == null) {
            String warnStr = String.format("Cancel create user. Invalid nodeId(%s).", request.getNodeId());
            log.warn(warnStr);
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, warnStr);
        }

        Optional<AccountsDO> accountsDOOptional = userAccountsRepository.findByName(request.getName());
        if (accountsDOOptional.isPresent()) {
            String warnStr = String.format("Cancel create node(%s) account. The name(%s) already exists in db.",
                    request.getNodeId(), request.getName());
            log.warn(warnStr);
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, warnStr);
        }
        AccountsDO accountRequest = new AccountsDO();
        accountRequest.setOwnerType(UserOwnerTypeEnum.EDGE);
        accountRequest.setOwnerId(request.getNodeId());
        accountRequest.setName(request.getName());
        accountRequest.setPasswordHash(request.getPasswordHash());
        accountRequest.setInstId("");
        userAccountsRepository.saveAndFlush(accountRequest);

        SysUserPermissionRelDO sysUserPermission = new SysUserPermissionRelDO();
        sysUserPermission.setUserType(PermissionUserTypeEnum.EDGE_USER);
        sysUserPermission.setTargetType(PermissionTargetTypeEnum.ROLE);
        SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
        upk.setUserKey(request.getName());
        upk.setTargetCode(RoleCodeConstants.EDGE_USER);
        sysUserPermission.setUpk(upk);
        permissionRelRepository.save(sysUserPermission);
    }


    @Override
    @Transactional
    public void resetPassword(ResetNodeUserPwdRequest request) {
        Optional<AccountsDO> accountsDOOptional = userAccountsRepository.findByName(request.getName());
        if (accountsDOOptional.isEmpty() || !accountsDOOptional.get().getOwnerId().equals(request.getNodeId())) {
            String warnStr = String.format("Cancel reset password. Do not exists name(%s) for nodeId(%s) in db.", request.getName(), request.getNodeId());
            log.warn(warnStr);
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, warnStr);
        }
        // update password
        AccountsDO accountsDO = accountsDOOptional.get();
        if (!StringUtils.equals(accountsDO.getPasswordHash(), request.getPasswordHash())) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "wrong password");
        }
        accountsDO.setPasswordHash(request.getNewPasswordHash());
        userAccountsRepository.saveAndFlush(accountsDO);
    }

    @Override
    public List<UserVO> listByNodeId(NodeUserListByNodeIdRequest request) {
        List<AccountsDO> byOwnerId = userAccountsRepository.findByOwnerId(request.getNodeId());
        return byOwnerId.stream().map(UserVO::fromAccountsDO).collect(Collectors.toList());

    }
}
