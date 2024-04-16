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
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectFeatureTableRepository;
import org.secretflow.secretpad.service.sync.edge.EdgeDataSyncServiceImpl;
import org.secretflow.secretpad.web.SecretPadApplication;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author yutu
 * @date 2024/02/23
 */
@Slf4j
@TestPropertySource(properties = {
        "secretpad.gateway=localhost:9001",
        "server.port=8443"
})
@ActiveProfiles({SystemConstants.TEST})
@SpringBootTest(classes = SecretPadApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CenterDataSyncControllerTest {
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
                .platformType(PlatformTypeEnum.CENTER)
                .ownerType(UserOwnerTypeEnum.CENTER)
                .projectIds(Set.of("test")).build());
    }


    @Resource
    private EdgeDataSyncServiceImpl edgeDataSyncService;
    @Resource
    private NodeRepository nodeRepository;

    @Resource
    private ProjectFeatureTableRepository projectFeatureTableRepository;

    @Test
    void testStart() {
        edgeDataSyncService.start();
        nodeRepository.deleteAuthentic("test");
        nodeRepository.save(NodeDO.builder().name("test").nodeId("test").controlNodeId("test").mode(0).build());
        nodeRepository.delete(NodeDO.builder().name("test").nodeId("test").controlNodeId("test").mode(0).build());
        ProjectFeatureTableDO.UPK upk = new ProjectFeatureTableDO.UPK("project", "node", "featuretable");
        projectFeatureTableRepository.saveAndFlush(ProjectFeatureTableDO.builder()
                .upk(upk)
                .featureTable(FeatureTableDO.builder().upk(new FeatureTableDO.UPK("f", "n")).type("http").url("http://1.1.1.1").desc("desc").status("").featureTableName("s").columns(new ArrayList<>()).build())
                .source(ProjectDatatableDO.ProjectDatatableSource.IMPORTED)
                .tableConfig(new ArrayList<>())
                .build());

        projectFeatureTableRepository.deleteById(upk);

    }
}