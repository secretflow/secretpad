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

package org.secretflow.secretpad.kuscia.v1alpha1.interceptor;

import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaAPIConstants;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yutu
 * @date 2024/06/11
 */
@Slf4j
public class TokenAuthClientInterceptor implements ClientInterceptor {

    private final String token;
    private final String domainId;

    public TokenAuthClientInterceptor(String token, String domainId) {
        this.token = token;
        this.domainId = domainId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

        return new ForwardingClientCall.SimpleForwardingClientCall<>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(Metadata.Key.of(KusciaAPIConstants.TOKEN_HEADER, Metadata.ASCII_STRING_MARSHALLER), token);
                log.info("[{}] add token header: {} {}", domainId, KusciaAPIConstants.TOKEN_HEADER, token);
                super.start(responseListener, headers);
            }
        };
    }
}