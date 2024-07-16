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

import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.factory.KusciaApiChannelFactory;
import org.secretflow.secretpad.kuscia.v1alpha1.factory.impl.GrpcKusciaApiChannelFactory;
import org.secretflow.secretpad.kuscia.v1alpha1.mock.MockKusciaGrpcServer;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author yutu
 * @date 2024/06/13
 */
@Slf4j
public class KusciaApiChannelFactoryTest {

    private MockKusciaGrpcServer mockKusciaGrpcServer;

    @BeforeEach
    void setUp() throws Exception {
        mockKusciaGrpcServer = new MockKusciaGrpcServer();
        mockKusciaGrpcServer.start();
    }

    @AfterEach
    void tearDown() {
        mockKusciaGrpcServer.shutdown();
    }

    @Test
    void test() {
        KusciaGrpcConfig config = new KusciaGrpcConfig();
        config.setHost(MockKusciaGrpcServer.HOST);
        config.setPort(MockKusciaGrpcServer.PORT);
        config.setProtocol(KusciaProtocolEnum.NOTLS);
        config.setMode(KusciaModeEnum.P2P);
        config.setDomainId("alice");
        KusciaApiChannelFactory factory = new GrpcKusciaApiChannelFactory(config);
        factory.getChannel();
        factory.shutdown();
    }
}