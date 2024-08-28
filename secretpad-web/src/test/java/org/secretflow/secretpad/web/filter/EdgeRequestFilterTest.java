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

package org.secretflow.secretpad.web.filter;

import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.web.constant.AuthConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author yutu
 * @date 2024/08/08
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class EdgeRequestFilterTest {

    private EdgeRequestFilter edgeRequestFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @Mock
    private UserTokensRepository userTokensRepository;
    @Mock
    private ObjectMapper jacksonObjectMapper;
    @Mock
    private EnvService envService;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        edgeRequestFilter = new EdgeRequestFilter(userTokensRepository, jacksonObjectMapper, envService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
    }

    @Test
    void testDoFilter() throws Exception {
        when(jacksonObjectMapper.writeValueAsString(Mockito.any())).thenReturn("ok");
        ServletOutputStream mock = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mock);
        edgeRequestFilter.setForward(List.of("/test"));
        edgeRequestFilter.setInclude(List.of("/api/v1alpha1/test1"));
        when(request.getServletPath()).thenReturn("/test");
        edgeRequestFilter.doFilter(request, response, chain);
        when(request.getServletPath()).thenReturn("/api/v1alpha1/test1");
        edgeRequestFilter.doFilter(request, response, chain);

        when(request.getServletPath()).thenReturn("/test");
        Enumeration headerNames = mock(Enumeration.class);
        when(request.getHeaderNames()).thenReturn(headerNames);
        when(request.getHeader(AuthConstants.TOKEN_NAME)).thenReturn("123");
        TokensDO tokensDO = TokensDO.builder()
                .gmtToken(LocalDateTime.now())
                .token("123")
                .sessionData("123")
                .build();
        when(userTokensRepository.findByToken(Mockito.any())).thenReturn(Optional.of(tokensDO));
        ResponseEntity<String> result = ResponseEntity.ok("ok");
//        when(restTemplate.exchange(Mockito.any(), String.class)).thenReturn(result);
        Assertions.assertThrows(ResourceAccessException.class, () -> edgeRequestFilter.doFilter(request, response, chain));
    }
}