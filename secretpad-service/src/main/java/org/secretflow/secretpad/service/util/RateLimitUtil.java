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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author chenmingliang
 * @date 2024/06/03
 */
public final class RateLimitUtil {

    private RateLimitUtil() {
    }

    private static final Cache<String, RateLimiter> rateLimiters = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();


    public static void verifyRate() {
        try {
            RateLimiter rateLimiter = getRateLimiter(UserContext.getUserName());
            if (!rateLimiter.tryAcquire()) {
                throw SecretpadException.of(SystemErrorCode.REQUEST_FREQUENCY_ERROR);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static RateLimiter getRateLimiter(String userName) throws ExecutionException {
        return rateLimiters.get(userName, () -> RateLimiter.create(5.0 / 60));
    }
}
