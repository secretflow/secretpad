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

package org.secretflow.secretpad.web.filter;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.web.util.AuthUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * @author yutu
 * @date 2023/10/26
 */
@Profile(value = {SystemConstants.EDGE})
@WebFilter
@Component
@Log4j2
@Configuration
@ConfigurationProperties("edge")
@Data
@RequiredArgsConstructor
public class EdgeRequestFilter implements Filter, Ordered {
    @Value("${secretpad.gateway}")
    private String kusciaLiteGateway;
    @Value("${secretpad.center-platform-service}")
    private String routeHeader;
    @Value("${secretpad.node-id}")
    private String nodeId;
    private List<String> forward;
    private List<String> include;
    private final UserTokensRepository userTokensRepository;
    private final ObjectMapper jacksonObjectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final EnvService envService;

    /**
     * Expiration time
     * one hour
     */
    private static final long EXPIRE = 60 * 60 * 24;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getServletPath();
        log.info("uri raw {}", uri);
        if (forward.contains(uri)) {
            try {
                checkUserLogin(req);
            } catch (SecretpadException e) {
                SecretPadResponse<Object> objectSecretPadResponse =
                        new SecretPadResponse<>(new SecretPadResponse.SecretPadResponseStatus(e.getErrorCode().getCode(), e.getMessage()), null);

                response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                String s = convertObjectToJson(objectSecretPadResponse);
                OutputStream out = response.getOutputStream();
                out.write(s.getBytes(StandardCharsets.UTF_8));
                out.flush();
                return;
            }
            log.info("----------- edge forward {} ", uri);
            String redirectUrl = "http://" + kusciaLiteGateway + uri;
            MultiValueMap<String, String> headers = parseRequestHeader(req);
            byte[] body = parseRequestBody(req);
            @SuppressWarnings(value = {"rawtypes"})
            RequestEntity requestEntity = new RequestEntity(body, headers, HttpMethod.POST, URI.create(redirectUrl));
            restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
            ResponseEntity<String> result = restTemplate.exchange(requestEntity, String.class);
            String resultBody = result.getBody();
            HttpHeaders resultHeaders = result.getHeaders();
            MediaType contentType = resultHeaders.getContentType();
            if (contentType != null) {
                resp.setContentType(contentType.toString());
            }
            resp.setCharacterEncoding("UTF-8");
            PrintWriter writer = resp.getWriter();
            assert resultBody != null;
            writer.write(resultBody);
            writer.flush();
        } else {
            if (uri.startsWith("/api/v1alpha1") && !include.contains(uri)) {
                response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getOutputStream().flush();
                return;
            }
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private byte[] parseRequestBody(HttpServletRequest request) throws IOException {
        InputStream inputStream = request.getInputStream();
        return StreamUtils.copyToByteArray(inputStream);
    }

    private MultiValueMap<String, String> parseRequestHeader(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String headerName : headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for (String headerValue : headerValues) {
                httpHeaders.add(headerName, headerValue);
            }
        }
        httpHeaders.remove("host");
        httpHeaders.add("host", routeHeader);
        httpHeaders.add("kuscia-origin-source", nodeId);
        return httpHeaders;
    }

    private void checkUserLogin(HttpServletRequest request) {
        String token = AuthUtils.findTokenInHeader(request);
        Optional<TokensDO> tokensDO = userTokensRepository.findByToken(token);
        if (tokensDO.isEmpty()) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "Cannot find token in db, user not login in.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime gmtToken = tokensDO.get().getGmtToken();
        long until = gmtToken.until(now, ChronoUnit.SECONDS);
        if (until > EXPIRE) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "The login session is expire, please login again.");
        }
        String sessionData = tokensDO.get().getSessionData();
        if (StringUtils.isBlank(sessionData)) {
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, "The login session is null, please login again.");
        }
    }

    public String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        return jacksonObjectMapper.writeValueAsString(object);
    }
}