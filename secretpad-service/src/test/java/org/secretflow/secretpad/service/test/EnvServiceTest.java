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

import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.impl.EnvServiceImpl;
import org.secretflow.secretpad.service.impl.InstServiceImpl;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author lufeng
 * @date 2024/8/6
 */
@ExtendWith(MockitoExtension.class)
public class EnvServiceTest {

    @Mock
    private ProjectNodeRepository projectNodeRepository;

    /**
     * test isCurrentInstEnvironment with current inst ID
     */
    @Test
    public void testIsCurrentInstEnvironmentWithCurrentInstID() {
        EnvServiceImpl envService = new EnvServiceImpl();
        assertTrue(envService.isCurrentInstEnvironment(InstServiceImpl.INST_ID));
    }


    /**
     * test ISCurrentInstEnvironment with non current inst ID
     */
    @Test
    public void testIsCurrentInstEnvironmentWithNonCurrentInstID() {
        EnvServiceImpl envService = new EnvServiceImpl();
        assertFalse(envService.isCurrentInstEnvironment("otherInstID"));
    }

    /**
     * test ISCurrentInstEnvironment with null inst ID
     */

    @Test
    public void testIsCurrentInstEnvironmentWithNullInstID() {
        EnvServiceImpl envService = new EnvServiceImpl();
        assertFalse(envService.isCurrentInstEnvironment(null));
    }

    @Test
    public void testFindLocalNodeId() {
        EnvServiceImpl envService = new EnvServiceImpl();

        ProjectJob.JobTask task = ProjectJob.JobTask.builder().parties(List.of("alice", "bob")).build();
        envService.setProjectNodeRepository(projectNodeRepository);
        List<ProjectNodeDO> projectNodeDOS = List.of(ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK("test", "alice")).build(),
                ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK("test", "bob")).build());

        P2pDataSyncProducerTemplate.nodeIds = null;
        Assertions.assertNull(envService.findLocalNodeId(task));
        P2pDataSyncProducerTemplate.nodeIds = new HashSet<>();
        P2pDataSyncProducerTemplate.nodeIds.add("alice");
        Assertions.assertNotNull(envService.findLocalNodeId(task));
        GraphContext.set(ProjectDO.builder().projectId("test").build(), GraphContext.GraphParties.builder().build());
        Assertions.assertNotNull(envService.findLocalNodeId(task));
        task = ProjectJob.JobTask.builder().parties(List.of("bob")).build();
        Mockito.when(projectNodeRepository.findByProjectId(Mockito.anyString())).thenReturn(null);
        Assertions.assertNull(envService.findLocalNodeId(task));
        Mockito.when(projectNodeRepository.findByProjectId(Mockito.anyString())).thenReturn(projectNodeDOS);
        Assertions.assertEquals("alice", envService.findLocalNodeId(task));
    }
}
