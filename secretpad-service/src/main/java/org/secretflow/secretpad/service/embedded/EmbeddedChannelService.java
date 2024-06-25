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

package org.secretflow.secretpad.service.embedded;

import org.secretflow.secretpad.manager.kuscia.EmbeddedNodeConfigProperties;
import org.secretflow.secretpad.manager.kuscia.KusciaAPIProperties;
import org.secretflow.secretpad.service.EnvService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.factory.TlsConfig;
import org.secretflow.v1alpha1.factory.embedded.EmbeddedNodeKusciaApiChannelFactory;
import org.secretflow.v1alpha1.kusciaapi.CertificateServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenmingliang
 * @date 2024/05/29
 */
@Slf4j
@Component
public class EmbeddedChannelService implements InitializingBean, ApplicationContextAware {

    private static final Map<String, CertificateServiceGrpc.CertificateServiceBlockingStub> certificate = new HashMap<>();
    private static final Map<String, DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub> datasource = new HashMap<>();

    @Resource
    protected EnvService envService;

    @Resource
    private KusciaAPIProperties kusciaAPIProperties;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (envService.isCenter()) {
            EmbeddedNodeConfigProperties embeddedNodeConfigProperties = applicationContext.getBean(EmbeddedNodeConfigProperties.class);
            TlsConfig tlsAliceConfig = new TlsConfig();
            tlsAliceConfig.setCaFile(embeddedNodeConfigProperties.getAliceCaFile());
            tlsAliceConfig.setCertFile(embeddedNodeConfigProperties.getAliceCertFile());
            tlsAliceConfig.setKeyFile(embeddedNodeConfigProperties.getAliceKeyFile());
            EmbeddedNodeKusciaApiChannelFactory aliceKusciaApiChannelFactory = new EmbeddedNodeKusciaApiChannelFactory(embeddedNodeConfigProperties.getAliceAddress(), embeddedNodeConfigProperties.getAliceTokenFile(), tlsAliceConfig, kusciaAPIProperties.getProtocol());


            TlsConfig tlsBobConfig = new TlsConfig();
            tlsBobConfig.setCaFile(embeddedNodeConfigProperties.getBobCaFile());
            tlsBobConfig.setCertFile(embeddedNodeConfigProperties.getBobCertFile());
            tlsBobConfig.setKeyFile(embeddedNodeConfigProperties.getBobKeyFile());
            EmbeddedNodeKusciaApiChannelFactory bobKusciaApiChannelFactory = new EmbeddedNodeKusciaApiChannelFactory(embeddedNodeConfigProperties.getBobAddress(), embeddedNodeConfigProperties.getBobTokenFile(), tlsBobConfig, kusciaAPIProperties.getProtocol());

            createDatasourceStub(aliceKusciaApiChannelFactory, bobKusciaApiChannelFactory);

            createCertificateStub(aliceKusciaApiChannelFactory, bobKusciaApiChannelFactory);

        }
    }

    private static void createCertificateStub(EmbeddedNodeKusciaApiChannelFactory aliceKusciaApiChannelFactory, EmbeddedNodeKusciaApiChannelFactory bobKusciaApiChannelFactory) {

        certificate.put("alice", CertificateServiceGrpc.newBlockingStub(aliceKusciaApiChannelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE));

        certificate.put("bob", CertificateServiceGrpc.newBlockingStub(bobKusciaApiChannelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE));
    }

    private static void createDatasourceStub(EmbeddedNodeKusciaApiChannelFactory aliceKusciaApiChannelFactory, EmbeddedNodeKusciaApiChannelFactory bobKusciaApiChannelFactory) {
        datasource.put("alice", DomainDataSourceServiceGrpc.newBlockingStub(aliceKusciaApiChannelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE));
        datasource.put("bob", DomainDataSourceServiceGrpc.newBlockingStub(bobKusciaApiChannelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public CertificateServiceGrpc.CertificateServiceBlockingStub getCertificateServiceBlockingStub(String nodeId) {
        if (envService.isEmbeddedNode(nodeId)) {
            return certificate.get(nodeId);
        }
        return null;
    }

    public DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub getDatasourceServiceBlockingStub(String nodeId) {
        if (envService.isEmbeddedNode(nodeId)) {
            return datasource.get(nodeId);
        }
        return null;
    }
}
