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

package org.secretflow.secretpad.web.interceptor;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PermissionUserTypeEnum;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.ResourceTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.SysResourcesBizService;
import org.secretflow.secretpad.web.util.AuthUtils;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Login interceptor
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Expiration time
     * one hour
     */
    private static final long EXPIRE = 60 * 60 * 24;

    private final UserTokensRepository userTokensRepository;

    private final ProjectNodeRepository projectNodeRepository;
    private final EnvService envService;

    private final SysResourcesBizService sysResourcesBizService;

    @Value("${secretpad.auth.enabled:true}")
    private boolean enable;

    @Value("${server.http-port-inner}")
    private Integer innerHttpPort;

    @Value("${secretpad.deploy-mode}")
    private String deployMode;

    @Resource
    private InnerPortPathConfig innerPortPathConfig;

    @Autowired
    public LoginInterceptor(UserTokensRepository userTokensRepository, EnvService envService,
                            SysResourcesBizService sysResourcesBizService, ProjectNodeRepository projectNodeRepository) {
        this.userTokensRepository = userTokensRepository;
        this.envService = envService;
        this.sysResourcesBizService = sysResourcesBizService;
        this.projectNodeRepository = projectNodeRepository;
    }

    private UserContextDTO createTmpUserForPlatformType(PlatformTypeEnum platformType) {
        if (envService.getPlatformType().equals(PlatformTypeEnum.CENTER)) {
            UserContextDTO userContextDTO = new UserContextDTO();
            userContextDTO.setName("admin");
            userContextDTO.setOwnerId("kuscia-system");
            userContextDTO.setOwnerType(UserOwnerTypeEnum.CENTER);
            userContextDTO.setToken("token");
            userContextDTO.setPlatformType(platformType);
            userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());
            userContextDTO.setDeployMode(deployMode);
            return userContextDTO;
        } else if (PlatformTypeEnum.TEST.equals(envService.getPlatformType())) {
            return UserContext.getUser();
        }
        UserContextDTO userContextDTO = new UserContextDTO();
        userContextDTO.setName("admin");
        userContextDTO.setOwnerId("nodeId");
        userContextDTO.setOwnerType(UserOwnerTypeEnum.EDGE);
        userContextDTO.setToken("token");
        userContextDTO.setPlatformType(platformType);
        userContextDTO.setPlatformNodeId(envService.getPlatformNodeId());
        userContextDTO.setDeployMode(deployMode);
        return userContextDTO;
    }

    /**
     * Check if intercept the request
     *
     * @param request  httpServletRequest
     * @param response httpServletResponse
     * @param handler  handler
     * @return true is passed, false is intercepted
     */
    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        if (!enable) {
            UserContextDTO admin = createTmpUserForPlatformType(envService.getPlatformType());
            UserContext.setBaseUser(admin);
            return true;
        }
        if (isOptionsVerb(request)) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("Process by port: {}", request.getLocalPort());
        }
        if (innerHttpPort.equals(request.getLocalPort())) {
            processByNodeRpcRequest(request);
        } else {
            processByUserRequest(request, response);
        }
        return true;
    }


    private void processByNodeRpcRequest(HttpServletRequest request) {
        String sourceNodeId = request.getHeader("kuscia-origin-source");
        if (StringUtils.isBlank(sourceNodeId)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "Cannot find node id in header for rpc.");
        }
        UserContextDTO virtualUser = new UserContextDTO();
        virtualUser.setVirtualUserForNode(true);
        virtualUser.setName(sourceNodeId);
        virtualUser.setOwnerId(sourceNodeId);
        virtualUser.setOwnerType(UserOwnerTypeEnum.EDGE);
        virtualUser.setToken("token");
        virtualUser.setPlatformType(PlatformTypeEnum.EDGE);
        virtualUser.setPlatformNodeId(envService.getPlatformNodeId());
        virtualUser.setDeployMode(deployMode);


        // TODO cache

        // fill project id
        List<ProjectNodeDO> byNodeId = projectNodeRepository.findByNodeId(sourceNodeId);
        Set<String> projectIds = byNodeId.stream().map(t -> t.getUpk().getProjectId()).collect(Collectors.toSet());
        virtualUser.setProjectIds(projectIds);

        // fill interface resource
        Set<String> resourceCodeSet = sysResourcesBizService.queryResourceCodeByUsername(PermissionUserTypeEnum.NODE, ResourceTypeEnum.API, sourceNodeId);
        virtualUser.setApiResources(resourceCodeSet);

        UserContext.setBaseUser(virtualUser);
    }

    private void processByUserRequest(HttpServletRequest request, HttpServletResponse response) {
        refuseByOutPortInvokeInnerPort(request, response);
        String token = AuthUtils.findTokenInHeader(request);
        Optional<TokensDO> tokensDO = userTokensRepository.findByToken(token);
        if (tokensDO.isEmpty()) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "login is required");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gmtToken = tokensDO.get().getGmtToken();
        long until = gmtToken.until(now, ChronoUnit.SECONDS);
        if (until > EXPIRE) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "login is expire, please login again.");
        }
        userTokensRepository.saveAndFlush(
                TokensDO.builder()
                        .name(tokensDO.get().getName())
                        .token(tokensDO.get().getToken())
                        .gmtToken(LocalDateTime.now())
                        .sessionData(tokensDO.get().getSessionData())
                        .build()
        );

        String sessionData = tokensDO.get().getSessionData();
        if (StringUtils.isBlank(sessionData)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "login is required");
        }
        UserContextDTO userContextDTO = UserContextDTO.fromJson(sessionData);
        UserContext.setBaseUser(userContextDTO);
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler,
                                Exception ex) {
        UserContext.remove();
    }

    private boolean isOptionsVerb(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    /**
     * uri only invoke by innerPort
     *
     * @param request request
     */
    private void refuseByOutPortInvokeInnerPort(HttpServletRequest request, HttpServletResponse response) {
        if (innerPortPathConfig.getPath().contains(request.getServletPath())) {
            returnJson(response);
        }
    }

    private void returnJson(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        try (PrintWriter writer = response.getWriter()) {
            writer.print("404");
            writer.flush();
        } catch (IOException e) {
            log.error("LoginInterceptor refuseByOutPortInvokeInnerPort returnJson error.", e);
        }
    }
}
