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

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.ResourceTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.AuthService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.SysResourcesBizService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User auth service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserAccountsRepository userAccountsRepository;

    @Autowired
    private UserTokensRepository userTokensRepository;

    @Autowired
    private ProjectNodeRepository projectNodeRepository;

    @Autowired
    private EnvService envService;

    @Autowired
    private SysResourcesBizService resourcesBizService;

    @Value("${secretpad.deploy-mode}")
    private String deployMode;

    @Override
    public UserContextDTO login(String name, String passwordHash) {
        PlatformTypeEnum plaformType = envService.getPlatformType();

        AccountsDO user = getUserAccount(name);
        if (!user.getPasswordHash().equals(passwordHash)) {
            LOGGER.error("Password not correct! UserName = {}", name);
            throw SecretpadException.of(AuthErrorCode.USER_OR_PASSWORD_ERROR);
        }
        String token = UUIDUtils.newUUID();

        UserContextDTO userContextDTO = new UserContextDTO();
        userContextDTO.setName(user.getName());
        userContextDTO.setOwnerId(user.getOwnerId());
        userContextDTO.setOwnerType(user.getOwnerType());
        userContextDTO.setToken(token);
        userContextDTO.setPlatformType(plaformType);
        userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());

        // fill project id and resource codes
        if (UserOwnerTypeEnum.EDGE.equals(user.getOwnerType())){
            List<ProjectNodeDO> byNodeId = projectNodeRepository.findByNodeId(user.getOwnerId());
            Set<String> projectIds = byNodeId.stream().map(t -> t.getUpk().getProjectId()).collect(Collectors.toSet());
            userContextDTO.setProjectIds(projectIds);

            Set<String> resourceCodeSet = resourcesBizService.queryResourceCodeByUsername(user.getOwnerType().toPermissionUserType(), ResourceTypeEnum.API, user.getName());
            userContextDTO.setApiResources(resourceCodeSet);
        }

        userContextDTO.setDeployMode(deployMode);
        TokensDO tokensDO = TokensDO.builder()
                .name(user.getName())
                .token(token)
                .gmtToken(LocalDateTime.now())
                .sessionData(userContextDTO.toJsonStr())
                .build();
        userTokensRepository.saveAndFlush(tokensDO);

        return userContextDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String name, String token) {
        userTokensRepository.deleteByNameAndToken(name, token);
    }

    /**
     * Get user account data object by userName
     *
     * @param name userName
     * @return user account data object
     */
    private AccountsDO getUserAccount(String name) {
        Optional<AccountsDO> user = userAccountsRepository.findByName(name);
        if (user.isEmpty()) {
            LOGGER.error("Cannot find user {}", name);
            throw SecretpadException.of(AuthErrorCode.USER_OR_PASSWORD_ERROR);
        }
        return user.get();
    }

}
