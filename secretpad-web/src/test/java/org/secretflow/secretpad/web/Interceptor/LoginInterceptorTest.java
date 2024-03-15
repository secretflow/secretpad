/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.web.Interceptor;

import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.web.constant.AuthConstants;
import org.secretflow.secretpad.web.controller.ControllerTest;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author yutu
 * @date 2024/02/28
 */
@TestPropertySource(properties = {
        "secretpad.auth.enabled=true",
        "kusciaapi.protocol=notls",
        "secretpad.node-id=test"
})
public class LoginInterceptorTest extends ControllerTest {

    @MockBean
    private UserTokensRepository userTokensRepository;

    @Test
    void testInterceptorNoHeader() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1alpha1/approval/create").contentType("application/json");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":{\"code\":202011602,\"msg\":\"用户认证失败: The request header does not contain header!\"},\"data\":null}"));
    }

    @Test
    void testInterceptorHeaderError() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1alpha1/approval/create").contentType("application/json").header(AuthConstants.TOKEN_NAME, "123");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":{\"code\":202011602,\"msg\":\"用户认证失败: login is required\"},\"data\":null}"));
    }

    @Test
    void testInterceptorHeaderExpire() throws Exception {
        Optional<TokensDO> tokensDO = Optional.of(TokensDO.builder()
                .token("123")
                .gmtToken(LocalDateTime.now().minusYears(1L))
                .build());
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1alpha1/approval/create").contentType("application/json").header(AuthConstants.TOKEN_NAME, "123");
        Mockito.when(userTokensRepository.findByToken(Mockito.any())).thenReturn(tokensDO);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":{\"code\":202011602,\"msg\":\"用户认证失败: login is expire, please login again.\"},\"data\":null}"));
    }

    @Test
    void testInterceptorHeaderSessionNull() throws Exception {
        Optional<TokensDO> tokensDO = Optional.of(TokensDO.builder()
                .token("123")
                .gmtToken(LocalDateTime.now())
                .build());
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1alpha1/approval/create").contentType("application/json").header(AuthConstants.TOKEN_NAME, "123");
        Mockito.when(userTokensRepository.findByToken(Mockito.any())).thenReturn(tokensDO);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":{\"code\":202011602,\"msg\":\"用户认证失败: login is required\"},\"data\":null}"));
    }

    @Test
    void testInnerPortPath() throws Exception {
        Optional<TokensDO> tokensDO = Optional.of(TokensDO.builder()
                .token("123")
                .gmtToken(LocalDateTime.now())
                .build());
        MockHttpServletRequestBuilder requestBuilder = post("/api/v1alpha1/user/node/resetPassword").contentType("application/json").header(AuthConstants.TOKEN_NAME, "123");
        Mockito.when(userTokensRepository.findByToken(Mockito.any())).thenReturn(tokensDO);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":{\"code\":202011602,\"msg\":\"用户认证失败: login is required\"},\"data\":null}"));
    }
}