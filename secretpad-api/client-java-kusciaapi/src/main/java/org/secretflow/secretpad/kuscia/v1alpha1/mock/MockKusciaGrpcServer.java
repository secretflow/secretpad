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

package org.secretflow.secretpad.kuscia.v1alpha1.mock;

import org.secretflow.secretpad.common.util.CertUtils;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.mock.interceptor.TokenAuthServerInterceptor;
import org.secretflow.secretpad.kuscia.v1alpha1.mock.service.*;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

/**
 * @author yutu
 * @date 2024/06/13
 */
@Slf4j
public class MockKusciaGrpcServer {
    public static final String TOKEN = "token";
    public static final String HOST = "localhost";
    public static final int PORT = 50051;
    private Server server;

    public KusciaGrpcConfig buildKusciaGrpcConfig(String domainId) {
        KusciaGrpcConfig config = new KusciaGrpcConfig();
        config.setHost(MockKusciaGrpcServer.HOST);
        config.setPort(MockKusciaGrpcServer.PORT);
        config.setProtocol(KusciaProtocolEnum.NOTLS);
        config.setMode(KusciaModeEnum.P2P);
        config.setDomainId(domainId);
        return config;
    }


    public void start() throws InterruptedException, IOException, CertificateException {
        start(PORT, KusciaProtocolEnum.NOTLS, null);
    }

    public void start(List<BindableService> bindableServices) throws InterruptedException, IOException, CertificateException {
        start(PORT, KusciaProtocolEnum.NOTLS, bindableServices);
    }

    public void start(int port, KusciaProtocolEnum protocol, List<BindableService> bindableServices) throws InterruptedException, IOException, CertificateException {
        ServerBuilder<?> serverBuilder;

        if (Objects.requireNonNull(protocol) == KusciaProtocolEnum.NOTLS) {
            serverBuilder = NettyServerBuilder.forPort(port);
        } else {
            serverBuilder = getServerBuilder(port);
        }
        if (!CollectionUtils.isEmpty(bindableServices)) {
            bindableServices.forEach(serverBuilder::addService);
        } else {
            serverBuilder.addService(new DomainService());
            serverBuilder.addService(new DomainDataService());
            serverBuilder.addService(new DomainDataGrantService());
            serverBuilder.addService(new DomainDatasourceService());
            serverBuilder.addService(new DomainRouteService());
            serverBuilder.addService(new JobService());
            serverBuilder.addService(new HealthService());
            serverBuilder.addService(new ServingService());
            serverBuilder.addService(new CertificateService());
        }

        server = serverBuilder.build().start();
        log.info("mock kuscia grpc server started, listening on {}", port);
    }

    private ServerBuilder<?> getServerBuilder(int port) throws IOException, InterruptedException, CertificateException {
        ServerBuilder<?> serverBuilder;
        initCerts();
        File serverCertFile = FileUtils.readFile("classpath:certs/server.crt");
        File serverPrivateKeyFile = FileUtils.readFile("classpath:certs/server.pem");
        X509Certificate[] serverTrustedCaCerts = {CertUtils.loadX509Cert("classpath:certs/ca.crt")};
        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(serverCertFile, serverPrivateKeyFile);
        GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL);
        sslContextBuilder.trustManager(serverTrustedCaCerts).clientAuth(ClientAuth.REQUIRE);
        serverBuilder = NettyServerBuilder.forPort(port)
                .sslContext(sslContextBuilder.build())
                .intercept(new TokenAuthServerInterceptor(TOKEN));
        return serverBuilder;
    }

    public void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void initCerts() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("./target/test-classes/scripts/init_kusciaapi_certs.sh");
        Process process = pb.start();
        process.waitFor();
    }
}