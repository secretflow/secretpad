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

package org.secretflow.v1alpha1.factory.embedded;

import org.secretflow.secretpad.common.util.FileUtils;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.constant.KusciaAPIConstants;
import org.secretflow.v1alpha1.factory.TlsConfig;

import java.io.File;
import java.io.IOException;

import static org.secretflow.secretpad.common.constant.KusciaConstants.KUSCIA_PROTOCOL_NOTLS;

/**
 * AliceKusciaApiChannelFactory.
 *
 * @author cml
 * @date 2023/10/27
 */
@Slf4j
public class EmbeddedNodeKusciaApiChannelFactory {
    private final String protocol;
    /**
     * ApiLite address
     */
    private final String address;

    /**
     * ApiLite token file
     */
    private final String tokenFile;

    /**
     * ApiLite tls config
     */
    private final TlsConfig tlsConfig;
    private final static int MAX_INBOUND_MESSAGE_SIZE = 256 * 1024 * 1024;

    public EmbeddedNodeKusciaApiChannelFactory(String address, String tokenFile, TlsConfig tlsConfig, String protocol) {
        this.address = address;
        this.tokenFile = tokenFile;
        this.tlsConfig = tlsConfig;
        this.protocol = protocol;
    }

    /**
     * Create a new client channel
     *
     * @return a new client
     */
    public ManagedChannel newClientChannel() {
        try {
            if (protocol.equals(KUSCIA_PROTOCOL_NOTLS)) {
                return NettyChannelBuilder.forTarget(address)
                        .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                        .negotiationType(NegotiationType.PLAINTEXT)
                        .build();
            }
            // init ssl context
            SslContextBuilder clientContextBuilder = SslContextBuilder.forClient();
            GrpcSslContexts.configure(clientContextBuilder, SslProvider.OPENSSL);
            // load client certs
            File certFile = FileUtils.readFile(tlsConfig.getCertFile());
            File keyFile = FileUtils.readFile(tlsConfig.getKeyFile());
            SslContext sslContext = clientContextBuilder
                    .keyManager(certFile, keyFile)
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            String token = FileUtils.readFile2String(tokenFile);

            Metadata metadata = new Metadata();
            Metadata.Key<String> key = Metadata.Key.of(KusciaAPIConstants.TOKEN_HEADER, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(key, token);
            ClientInterceptor tokenInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);
            return NettyChannelBuilder.forTarget(address)
                    .intercept(tokenInterceptor)
                    .negotiationType(NegotiationType.TLS)
                    .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                    .sslContext(sslContext)
                    .build();
        } catch (IOException e) {
            log.error("init kuscia grpc client error", e);
            return null;
        }
    }
}
