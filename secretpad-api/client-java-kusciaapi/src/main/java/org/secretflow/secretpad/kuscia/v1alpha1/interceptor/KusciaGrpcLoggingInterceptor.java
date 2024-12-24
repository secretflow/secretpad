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

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yutu
 * @date 2024/06/11
 */
@Slf4j
public class KusciaGrpcLoggingInterceptor implements ClientInterceptor {

    private final String domainId;

    public KusciaGrpcLoggingInterceptor(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        long startTime = System.currentTimeMillis();
        log.info("[kuscia] {}  Calling method: {}", domainId, method.getFullMethodName());

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                log.info("[kuscia] {} Request: {}", domainId, message);
                super.sendMessage(message);
            }

            @Override
            public void cancel(String message, Throwable t) {
                long endTime = System.currentTimeMillis();
                log.error("[kuscia] {} Cancel request: {} time: {}", domainId, message, endTime - startTime, t);
                super.cancel(message, t);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onMessage(RespT message) {
                        long endTime = System.currentTimeMillis();
                        log.info("[kuscia] {} Response: {} time: {}", domainId, message, endTime - startTime);
                        super.onMessage(message);
                    }
                }, headers);
            }
        };
    }
}
