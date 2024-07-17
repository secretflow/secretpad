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

package org.secretflow.secretpad.kuscia.v1alpha1.factory.impl;

import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.factory.KusciaApiChannelFactory;
import org.secretflow.secretpad.kuscia.v1alpha1.interceptor.KusciaGrpcLoggingInterceptor;
import org.secretflow.secretpad.kuscia.v1alpha1.interceptor.TokenAuthClientInterceptor;
import org.secretflow.secretpad.kuscia.v1alpha1.listener.ManagedChannelStateListener;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;

import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author yutu
 * @date 2024/06/12
 */
@Slf4j
public class GrpcKusciaApiChannelFactory implements KusciaApiChannelFactory {


    private final AtomicReference<ConnectivityState> state = new AtomicReference<>(ConnectivityState.SHUTDOWN);
    private final KusciaGrpcConfig kusciaGrpcConfig;
    private final ClientInterceptor loggingInterceptor;
    private final ClientInterceptor tokenAuthClientInterceptor;
    private ManagedChannel channel;
    private final static int MAX_INBOUND_MESSAGE_SIZE = 256 * 1024 * 1024;


    public GrpcKusciaApiChannelFactory(KusciaGrpcConfig kusciaGrpcConfig) {
        Assert.notNull(kusciaGrpcConfig, "KusciaGrpcConfig must not be null");
        kusciaGrpcConfig.validateAndProcess();
        this.kusciaGrpcConfig = kusciaGrpcConfig;
        this.loggingInterceptor = new KusciaGrpcLoggingInterceptor(kusciaGrpcConfig.getDomainId());
        this.tokenAuthClientInterceptor = new TokenAuthClientInterceptor(kusciaGrpcConfig.getToken(), kusciaGrpcConfig.getDomainId());
    }

    @Override
    public ManagedChannel getChannel() {
        synchronized (this) {
            if (!state.get().equals(ConnectivityState.SHUTDOWN)) {
                return channel;
            }
            assertInitialized();
            initChannel();
            return channel;
        }
    }

    @Override
    public void shutdownNow() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    @Override
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    @Override
    public ConnectivityState getState() {
        return state.get();
    }

    @Override
    public boolean isAvailable() {
        return state.get().equals(ConnectivityState.READY);
    }

    private void initChannel() {
        NettyChannelBuilder nettyChannelBuilder = NettyChannelBuilder
                .forAddress(kusciaGrpcConfig.getHost(), kusciaGrpcConfig.getPort())
                .intercept(loggingInterceptor)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE);

        if (kusciaGrpcConfig.getProtocol() == KusciaProtocolEnum.NOTLS) {
            nettyChannelBuilder.usePlaintext();
        } else {
            SslContextBuilder clientContextBuilder = SslContextBuilder.forClient();
            GrpcSslContexts.configure(clientContextBuilder, SslProvider.OPENSSL);

            SslContext sslContext = null;
            try {
                File cert = FileUtils.readFile(kusciaGrpcConfig.getCertFile());
                File key = FileUtils.readFile(kusciaGrpcConfig.getKeyFile());
                sslContext = clientContextBuilder
                        .keyManager(cert, key)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();
            } catch (SSLException e) {
                log.error("Failed to create ssl context", e);
            } catch (FileNotFoundException e) {
                log.error("Failed to create ssl context, cert or key file not found", e);
            }

            nettyChannelBuilder
                    .sslContext(sslContext)
                    .intercept(tokenAuthClientInterceptor)
                    .useTransportSecurity();
        }
        channel = nettyChannelBuilder.build();
        new ManagedChannelStateListener(channel, kusciaGrpcConfig.getDomainId(), state);
    }


    private void assertInitialized() {
        ConnectivityState current = state.get();
        if (ConnectivityState.SHUTDOWN.equals(current)) {
            return;
        }
        throw new IllegalStateException(String.format("GrpcKusciaApiChannelFactory is %s", current));
    }
}