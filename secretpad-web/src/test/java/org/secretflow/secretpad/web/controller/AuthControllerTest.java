/*
 *   Copyright 2023 Ant Group Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.AccountsDO;
import org.secretflow.secretpad.persistence.repository.UserAccountsRepository;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.model.auth.LoginRequest;
import org.secretflow.secretpad.service.model.auth.LogoutRequest;
import org.secretflow.secretpad.web.constant.AuthConstants;
import org.secretflow.secretpad.web.util.AuthUtils;
import org.secretflow.secretpad.web.util.RequestUtils;
import org.secretflow.secretpad.web.utils.FakerUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Objects;
import java.util.Optional;

import static org.secretflow.secretpad.common.errorcode.AuthErrorCode.USER_IS_LOCKED;
import static org.secretflow.secretpad.common.errorcode.AuthErrorCode.USER_PASSWORD_ERROR;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Authorization controller test
 *
 * @author cml
 * @date 2023/07/27
 * @since 4.3
 */
class AuthControllerTest extends ControllerTest {

    @MockBean
    private UserAccountsRepository userAccountsRepository;

    @MockBean
    private UserTokensRepository userTokensRepository;

    @Test
    void login() throws Exception {
        HttpServletRequest currentHttpRequest = RequestUtils.getCurrentHttpRequest();
        String currentRequestMethodURI = RequestUtils.getCurrentRequestMethodURI();
        assert currentHttpRequest != null && currentRequestMethodURI != null;
        Assertions.assertThrows(SecretpadException.class, () -> AuthUtils.findTokenInHeader(currentHttpRequest));
        assertResponse(() -> {
            LoginRequest loginRequest = FakerUtils.fake(LoginRequest.class);
            AccountsDO accountsDO = FakerUtils.fake(AccountsDO.class);
            accountsDO.setPasswordHash(loginRequest.getPasswordHash());
            when(userAccountsRepository.findByName(loginRequest.getName())).thenReturn(Optional.of(accountsDO));
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "login", LoginRequest.class))
                    .content(JsonUtils.toJSONString(loginRequest));
        });
    }

    @RepeatedTest(5)
    void loginWithUserNotExit() throws Exception {
        assertErrorCodeWithAny(() -> {
            LoginRequest loginRequest = LoginRequest.builder().name("admin1").passwordHash("0").build();
            when(userAccountsRepository.findByName(loginRequest.getName())).thenReturn(Optional.empty());
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "login", LoginRequest.class))
                    .content(JsonUtils.toJSONString(loginRequest));
        }, USER_PASSWORD_ERROR, USER_IS_LOCKED);
    }

    @RepeatedTest(6)
    void loginWithUserExitButWrongPwd() throws Exception {
        assertErrorCodeWithAny(() -> {
            LoginRequest loginRequest = LoginRequest.builder().name("admin").passwordHash("0").build();
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "login", LoginRequest.class))
                    .content(JsonUtils.toJSONString(loginRequest));
        }, USER_PASSWORD_ERROR, USER_IS_LOCKED);
    }

    @Test
    void logout() throws Exception {
        assertResponse(() -> {
            LogoutRequest logoutRequest = FakerUtils.fake(LogoutRequest.class);
            logoutRequest.setName("alice");
            UserContextDTO user = UserContext.getUser();
            user.setPlatformNodeId("alice");
            user.setName("alice");
            doNothing().when(userTokensRepository).deleteByNameAndToken(logoutRequest.getName(), "token");
            return MockMvcRequestBuilders.post(getMappingUrl(AuthController.class, "logout", HttpServletRequest.class))
                    .content(Objects.requireNonNull(JsonUtils.toString(logoutRequest))).header(AuthConstants.TOKEN_NAME, "token");
        });
    }
}