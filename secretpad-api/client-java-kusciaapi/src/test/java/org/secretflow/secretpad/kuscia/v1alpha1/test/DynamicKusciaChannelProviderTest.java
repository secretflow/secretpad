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

package org.secretflow.secretpad.kuscia.v1alpha1.test;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.DynamicKusciaChannelProvider;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.mock.MockKusciaGrpcServer;
import org.secretflow.secretpad.kuscia.v1alpha1.model.DynamicKusciaGrpcConfig;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author yutu
 * @date 2024/06/14
 */
public class DynamicKusciaChannelProviderTest {

    private static final KusciaGrpcConfig config = new KusciaGrpcConfig();

    static {
        config.setHost(MockKusciaGrpcServer.HOST);
        config.setPort(MockKusciaGrpcServer.PORT);
        config.setProtocol(KusciaProtocolEnum.NOTLS);
        config.setMode(KusciaModeEnum.P2P);
        config.setDomainId("alice");
    }

    @BeforeEach
    void setUp() {
        UserContext.setBaseUser(UserContextDTO.builder()
                .ownerId("alice")
                .platformNodeId("alice")
                .build());
    }

    @Test
    void testCurrentStubDomainServiceGrpc() {
        DynamicKusciaChannelProvider service = new DynamicKusciaChannelProvider();
        DynamicKusciaGrpcConfig dynamicKusciaGrpcConfig = new DynamicKusciaGrpcConfig();
        dynamicKusciaGrpcConfig.setNodes(new CopyOnWriteArraySet<>());
        service.setDynamicKusciaGrpcConfig(dynamicKusciaGrpcConfig);
        service.registerKuscia(config);
        service.registerKuscia(config);
        service.setNodeId("alice");
        DomainServiceGrpc.DomainServiceBlockingStub domainServiceBlockingStub = service.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class);
        Assertions.assertThrows(StatusRuntimeException.class, () -> domainServiceBlockingStub.queryDomain(null));
        Assertions.assertThrows(StatusRuntimeException.class, () -> domainServiceBlockingStub.createDomain(null));
        Assertions.assertThrows(StatusRuntimeException.class, () -> domainServiceBlockingStub.updateDomain(null));
        Assertions.assertThrows(StatusRuntimeException.class, () -> domainServiceBlockingStub.deleteDomain(null));
        Assertions.assertThrows(StatusRuntimeException.class, () -> domainServiceBlockingStub.batchQueryDomain(null));
        service.unRegisterKuscia(config);
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.currentStub(DomainServiceGrpc.DomainServiceBlockingStub.class));
    }
}