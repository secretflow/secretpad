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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaModeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;
import org.secretflow.secretpad.service.listener.KusciaRegisterListener;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author yutu
 * @date 2024/07/30
 */
public class KusciaRegisterListenerTest {


    @Test
    void testSerializableWrite() throws IOException {
        KusciaRegisterListener kusciaRegisterListener = new KusciaRegisterListener();
        kusciaRegisterListener.setKusciaPath("./config/kuscia/");
        kusciaRegisterListener.serializableWrite(KusciaGrpcConfig.builder()
                .domainId("test")
                .mode(KusciaModeEnum.P2P)
                .host("xxxxx")
                .port(10000)
                .protocol(KusciaProtocolEnum.NOTLS)
                .token("config/certs/token")
                .certFile("config/certs/client.crt")
                .keyFile("config/certs/client.pem")
                .build());
    }
}