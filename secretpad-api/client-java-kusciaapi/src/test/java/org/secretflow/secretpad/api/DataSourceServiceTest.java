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

package org.secretflow.secretpad.api;

import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author yutu
 * @date 2024/05/28
 */
@Slf4j
public class DataSourceServiceTest {
    private final static int MAX_INBOUND_MESSAGE_SIZE = 256 * 1024 * 1024;

    private String aliceAddress = "127.0.0.1:28083";
    private String bobAddress = "127.0.0.1:38083";
    private String address = "127.0.0.1:18083";

    //    @Test
    void testQuery() {
        ManagedChannel channel = NettyChannelBuilder.forTarget(aliceAddress)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
        DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub domainDataSourceServiceBlockingStub = DomainDataSourceServiceGrpc.newBlockingStub(channel);
        Domaindatasource.QueryDomainDataSourceResponse alice = domainDataSourceServiceBlockingStub.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDomainId("alice")
                .setDatasourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                .build());
        log.info("{}", alice);
        channel.shutdownNow();

        channel = NettyChannelBuilder.forTarget(bobAddress)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
        domainDataSourceServiceBlockingStub = DomainDataSourceServiceGrpc.newBlockingStub(channel);
        Domaindatasource.QueryDomainDataSourceResponse bob = domainDataSourceServiceBlockingStub.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDomainId("bob")
                .setDatasourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                .build());
        log.info("{}", bob);
        channel.shutdownNow();

        channel = NettyChannelBuilder.forTarget(address)
                .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
        DomainServiceGrpc.DomainServiceBlockingStub domainServiceBlockingStub = DomainServiceGrpc.newBlockingStub(channel);
        DomainOuterClass.QueryDomainResponse queryDomainResponse = domainServiceBlockingStub.queryDomain(DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId("alice").build());
        log.info("{}", queryDomainResponse);
        channel.shutdownNow();
    }
}