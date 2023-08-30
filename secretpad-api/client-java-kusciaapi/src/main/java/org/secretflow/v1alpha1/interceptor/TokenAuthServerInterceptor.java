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

package org.secretflow.v1alpha1.interceptor;

import io.grpc.*;
import org.secretflow.v1alpha1.constant.KusciaAPIConstants;

/**
 * To intercept the call with incorrect auth token
 *
 * @author yansi
 * @date 2023/6/20
 */
public class TokenAuthServerInterceptor implements ServerInterceptor {

    /**
     * Token for auth
     */
    private final String token;

    public TokenAuthServerInterceptor(String token) {
        this.token = token;
    }

    /**
     * Intercept the call with incorrect auth token
     *
     * @param call
     * @param headers
     * @param next
     * @param <ReqT>
     * @param <RespT>
     * @return listened request and response
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final String authToken = headers.get(Metadata.Key.of(KusciaAPIConstants.TOKEN_HEADER, Metadata.ASCII_STRING_MARSHALLER));

        if (authToken == null || !authToken.equals(token)) {
            throw new StatusRuntimeException(Status.UNAUTHENTICATED);
        }

        return next.startCall(call, headers);
    }
}
