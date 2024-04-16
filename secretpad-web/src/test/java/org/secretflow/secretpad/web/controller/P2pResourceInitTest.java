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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.web.init.P2pDataInit;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate.ownerId_cache;

/**
 * @author yutu
 * @date 2024/02/27
 */
@ActiveProfiles({SystemConstants.DEV})
@TestPropertySource(properties = {
        "secretpad.deploy-mode=MPC",
        "secretpad.platform-type=AUTONOMY",
        "secretpad.node-id=test",
})
@Slf4j
public class P2pResourceInitTest extends ControllerTest {

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private P2pDataInit p2pDataInit;

    @Test
    void init() {
        if (!projectRepository.existsById("test")) {
            projectRepository.save(ProjectDO.builder()
                    .status(0)
                    .name("test555555")
                    .computeFunc("")
                    .computeMode("")
                    .projectId("test555555")
                    .ownerId("alice")
                    .build());
        }
        p2pDataInit.init_ownerId_cache();
        log.info("init {}", ownerId_cache);
        projectRepository.deleteAllAuthentic();
    }
}