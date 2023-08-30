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

import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.TokensDO;
import org.secretflow.secretpad.persistence.repository.UserTokensRepository;
import org.secretflow.secretpad.web.util.AuthUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Login interceptor
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Expiration time
     * one hour
     */
    private static final long EXPIRE = 60 * 60 * 24;

    private final UserTokensRepository userTokensRepository;

    @Value("${secretpad.auth.enabled:true}")
    private boolean enable;

    @Autowired
    public LoginInterceptor(UserTokensRepository userTokensRepository) {
        this.userTokensRepository = userTokensRepository;
    }

    /**
     * Check if intercept the request
     *
     * @param request  httpServletRequest
     * @param response httpServletResponse
     * @param handler
     * @return true is passed, false is intercepted
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!enable) {
            UserContext.setBaseUser("admin");
            return true;
        }
        if (isOptionsVerb(request)) {
            return true;
        }
        Cookie cookie = AuthUtils.findTokenCookie(request.getCookies());
        String token = cookie.getValue();
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
        userTokensRepository.saveAndFlush(
                TokensDO.builder()
                        .name(tokensDO.get().getName())
                        .token(tokensDO.get().getToken())
                        .gmtToken(LocalDateTime.now())
                        .build()
        );
        UserContext.setBaseUser(tokensDO.get().getName());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        UserContext.remove();
    }

    private boolean isOptionsVerb(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

}
