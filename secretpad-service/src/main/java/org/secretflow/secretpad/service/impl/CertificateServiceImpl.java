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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.kuscia.EmbeddedNodeConfigProperties;
import org.secretflow.secretpad.manager.kuscia.grpc.KusciaCertificateRpc;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;

import jakarta.annotation.Resource;
import org.secretflow.v1alpha1.factory.TlsConfig;
import org.secretflow.v1alpha1.factory.embedded.EmbeddedNodeKusciaApiChannelFactory;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.secretflow.v1alpha1.kusciaapi.CertificateServiceGrpc;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cml
 * @date 2023/11/15
 */
@Service
public class CertificateServiceImpl implements CertificateService, InitializingBean, ApplicationContextAware {


    private static final Map<String, CertificateServiceGrpc.CertificateServiceBlockingStub> certificateServiceBlockingStubMap = new HashMap<>();

    private ApplicationContext applicationContext;

    @Resource
    private EnvService envService;

    @Resource
    private KusciaCertificateRpc kusciaCertificateRpc;

    @Override
    public Certificate.GenerateKeyCertsResponse generateCertByNodeID(String nodeID) {
        Certificate.GenerateKeyCertsResponse generateKeyCertsResponse;
        if (envService.isEmbeddedNode(nodeID)) {
            generateKeyCertsResponse = certificateServiceBlockingStubMap.get(nodeID).generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().setCommonName("vote").setKeyType("PKCS#8").build());
            if (generateKeyCertsResponse.getStatus().getCode() != 0) {
                throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, generateKeyCertsResponse.getStatus().getMessage());
            }
        } else {
            generateKeyCertsResponse = kusciaCertificateRpc.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().setCommonName("vote").setKeyType("PKCS#8").build());
        }
        return generateKeyCertsResponse;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (envService.isCenter()) {
            EmbeddedNodeConfigProperties embeddedNodeConfigProperties = applicationContext.getBean(EmbeddedNodeConfigProperties.class);
            TlsConfig tlsAliceConfig = new TlsConfig();
            tlsAliceConfig.setCaFile(embeddedNodeConfigProperties.getAliceCaFile());
            tlsAliceConfig.setCertFile(embeddedNodeConfigProperties.getAliceCertFile());
            tlsAliceConfig.setKeyFile(embeddedNodeConfigProperties.getAliceKeyFile());
            EmbeddedNodeKusciaApiChannelFactory aliceKusciaApiChannelFactory = new EmbeddedNodeKusciaApiChannelFactory(embeddedNodeConfigProperties.getAliceAddress(), embeddedNodeConfigProperties.getAliceTokenFile(), tlsAliceConfig);

            CertificateServiceGrpc.CertificateServiceBlockingStub aliceCertificateServiceBlockingStub = CertificateServiceGrpc.newBlockingStub(aliceKusciaApiChannelFactory.newClientChannel())
                    .withMaxInboundMessageSize(Integer.MAX_VALUE)
                    .withMaxOutboundMessageSize(Integer.MAX_VALUE);

            TlsConfig tlsBobConfig = new TlsConfig();
            tlsBobConfig.setCaFile(embeddedNodeConfigProperties.getBobCaFile());
            tlsBobConfig.setCertFile(embeddedNodeConfigProperties.getBobCertFile());
            tlsBobConfig.setKeyFile(embeddedNodeConfigProperties.getBobKeyFile());
            EmbeddedNodeKusciaApiChannelFactory bobKusciaApiChannelFactory = new EmbeddedNodeKusciaApiChannelFactory(embeddedNodeConfigProperties.getBobAddress(), embeddedNodeConfigProperties.getBobTokenFile(), tlsBobConfig);

            CertificateServiceGrpc.CertificateServiceBlockingStub bobCertificateServiceBlockingStub = CertificateServiceGrpc.newBlockingStub(bobKusciaApiChannelFactory.newClientChannel())
                    .withMaxInboundMessageSize(Integer.MAX_VALUE)
                    .withMaxOutboundMessageSize(Integer.MAX_VALUE);

            certificateServiceBlockingStubMap.put("alice", aliceCertificateServiceBlockingStub);
            certificateServiceBlockingStubMap.put("bob", bobCertificateServiceBlockingStub);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
