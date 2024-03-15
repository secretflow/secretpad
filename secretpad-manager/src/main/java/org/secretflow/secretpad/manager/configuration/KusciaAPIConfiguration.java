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

package org.secretflow.secretpad.manager.configuration;

import org.secretflow.secretpad.manager.kuscia.KusciaAPIProperties;

import org.secretflow.v1alpha1.factory.KusciaAPIChannelFactory;
import org.secretflow.v1alpha1.kusciaapi.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ApiLiteConfiguration init apiLite client bean
 *
 * @author yansi
 * @date 2023/5/4
 */
@Configuration
@EnableConfigurationProperties({KusciaAPIProperties.class})
public class KusciaAPIConfiguration {

    /**
     * Create a new apiLite channel factory via api properties
     *
     * @param properties
     * @return a new apiLite channel factory
     */
    @Bean
    KusciaAPIChannelFactory apiLiteChannelFactory(KusciaAPIProperties properties) {
        return new KusciaAPIChannelFactory(properties.getAddress(), properties.getTokenFile(), properties.getTls(), properties.getProtocol());
    }

    /**
     * Create a new domain service blocking stub via apiLite channel factory
     *
     * @param channelFactory
     * @return a new domain service blocking stub
     */
    @Bean
    DomainServiceGrpc.DomainServiceBlockingStub domainServiceStub(KusciaAPIChannelFactory channelFactory) {
        return DomainServiceGrpc.newBlockingStub(channelFactory.newClientChannel());
    }

    /**
     * Create a new job service blocking stub via apiLite channel factory
     *
     * @param channelFactory
     * @return a new job service blocking stub
     */
    @Bean
    JobServiceGrpc.JobServiceBlockingStub jobServiceStub(KusciaAPIChannelFactory channelFactory) {
        return JobServiceGrpc.newBlockingStub(channelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE);
    }

    /**
     * Create a new domain route service blocking stub via apiLite channel factory
     *
     * @param channelFactory
     * @return a new domain route service blocking stub
     */
    @Bean
    DomainRouteServiceGrpc.DomainRouteServiceBlockingStub domainRouteServiceStub(KusciaAPIChannelFactory channelFactory) {
        return DomainRouteServiceGrpc.newBlockingStub(channelFactory.newClientChannel());
    }

    /**
     * Create a new domain data service blocking stub via apiLite channel factory
     *
     * @param channelFactory
     * @return a new domain data service blocking stub
     */
    @Bean
    DomainDataServiceGrpc.DomainDataServiceBlockingStub domainDataServiceStub(KusciaAPIChannelFactory channelFactory) {
        return DomainDataServiceGrpc.newBlockingStub(channelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE);
    }


    @Bean
    DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub dataGrantStub(KusciaAPIChannelFactory channelFactory) {
        return DomainDataGrantServiceGrpc.newBlockingStub(channelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE);
    }

    @Bean("certificateServiceBlockingStub")
    public CertificateServiceGrpc.CertificateServiceBlockingStub certificateServiceBlockingStub(KusciaAPIChannelFactory channelFactory) {
        return CertificateServiceGrpc.newBlockingStub(channelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE);
    }

    /**
     * Create a new KusciaDeployment service blocking stub via apiLite channel factory
     *
     * @param channelFactory channelFactory
     * @return a new KusciaDeployment service blocking stub
     */
    @Bean
    public ServingServiceGrpc.ServingServiceBlockingStub servingServiceBlockingStub(KusciaAPIChannelFactory channelFactory) {
        return ServingServiceGrpc.newBlockingStub(channelFactory.newClientChannel())
                .withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE);
    }
}
