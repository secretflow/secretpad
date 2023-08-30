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

package org.secretflow.v1alpha1.factory;

import org.secretflow.secretpad.common.util.CertUtils;
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
import io.grpc.stub.MetadataUtils;
import org.secretflow.v1alpha1.constant.KusciaAPIConstants;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * ApiLite channel factory
 *
 * @author yansi
 * @date 2023/5/8
 */
public class KusciaAPIChannelFactory {
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

    public KusciaAPIChannelFactory(String address, String tokenFile, TlsConfig tlsConfig) {
        this.address = address;
        this.tokenFile = tokenFile;
        this.tlsConfig = tlsConfig;
    }

    /**
     * Create a new client channel
     *
     * @return a new client
     */
    public ManagedChannel newClientChannel() {
        // init ssl context
        SslContextBuilder clientContextBuilder = SslContextBuilder.forClient();
        GrpcSslContexts.configure(clientContextBuilder, SslProvider.OPENSSL);
        // load client certs
        try {
            File certFile = FileUtils.readFile(tlsConfig.getCertFile());
            File keyFile = FileUtils.readFile(tlsConfig.getKeyFile());
            X509Certificate[] clientTrustedCaCerts = {CertUtils.loadX509Cert(tlsConfig.getCaFile())};
            SslContext sslContext = clientContextBuilder
                    .keyManager(certFile, keyFile)
                    .trustManager(clientTrustedCaCerts)
                    .build();

            String token = FileUtils.readFile2String(tokenFile);

            Metadata metadata = new Metadata();
            Metadata.Key<String> key = Metadata.Key.of(KusciaAPIConstants.TOKEN_HEADER, Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(key, token);
            ClientInterceptor tokenInterceptor = MetadataUtils.newAttachHeadersInterceptor(metadata);

            // new client channel
            return NettyChannelBuilder.forTarget(address)
                    .intercept(tokenInterceptor)
                    .negotiationType(NegotiationType.TLS)
                    .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                    .sslContext(sslContext)
                    .build();
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}