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

package org.secretflow.secretpad.web.kuscia;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.dataproxy.DataProxyService;
import org.secretflow.secretpad.web.SecretPadApplication;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author yutu
 * @date 2024/06/18
 */
@Slf4j
@ActiveProfiles(value = "test")
@SpringBootTest(classes = SecretPadApplication.class)
public class BaseKusciaTest {
    public static final String PROJECT_ID = "projectagdasvacaghyhbvscvyjnba";
    @MockBean
    protected DataProxyService dataProxyService;

    @BeforeAll
    public static void setup() throws IOException, InterruptedException {
        log.info("start to setup");
        ProcessBuilder pb = new ProcessBuilder("./config/setup.sh");
        Process process = pb.start();
        process.waitFor();
    }

    @BeforeEach
    public void initSession() {
        UserContext.setBaseUser(UserContextDTO.builder().ownerId("alice")
                .name("alice")
                .platformType(PlatformTypeEnum.CENTER)
                .platformNodeId("alice")
                .ownerType(UserOwnerTypeEnum.CENTER)
                .projectIds(Set.of(PROJECT_ID)).build());
        Mockito.doNothing().when(dataProxyService).updateDataSourceUseDataProxyInMaster();
        Mockito.doNothing().when(dataProxyService).updateDataSourceUseDataProxyInP2p(Mockito.anyString());
        File file = new File("./config/kuscia");
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.deleteOnExit();
                }
            }
        }
    }
}