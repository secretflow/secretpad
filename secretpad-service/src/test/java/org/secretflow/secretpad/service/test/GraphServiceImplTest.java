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

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;
import org.secretflow.secretpad.persistence.repository.ProjectGraphRepository;
import org.secretflow.secretpad.service.impl.GraphServiceImpl;
import org.secretflow.secretpad.service.model.graph.UpdateGraphMetaRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GraphServiceImplTest {

    @Mock
    private ProjectGraphRepository graphRepository;

    @InjectMocks
    private GraphServiceImpl graphService;

    @BeforeEach
    public void setup() {
        UserContextDTO userContextDTO = UserContextDTO.builder()
                .ownerId("userOwnerId")
                .platformType(PlatformTypeEnum.AUTONOMY)
                .build();
        UserContext.setBaseUser(userContextDTO);
    }

    /**
     * [Single Test Case] Test Scenario: Test normal conditions
     */
    @Test
    public void testUpdateGraphMeta_NormalCase() {
        UserContextDTO user = UserContext.getUser();
        user.setPlatformType(PlatformTypeEnum.CENTER);

        when(graphRepository.findById(any(ProjectGraphDO.UPK.class))).thenReturn(Optional.of(createProjectGraphDO()));

        UpdateGraphMetaRequest request = new UpdateGraphMetaRequest("projectId", "graphId", "newName");
        graphService.updateGraphMeta(request);

        verify(graphRepository, times(1)).save(any(ProjectGraphDO.class));
    }

    /**
     * Test Scenario: Test if graph DO Optional is empty
     */
    @Test
    public void testUpdateGraphMeta_GraphDOOptionalEmpty() {
        when(graphRepository.findById(any(ProjectGraphDO.UPK.class))).thenReturn(Optional.empty());

        UpdateGraphMetaRequest request = new UpdateGraphMetaRequest("projectId", "graphId", "newName");
        assertThrows(SecretpadException.class, () -> graphService.updateGraphMeta(request));
    }

    /**
     * Test Scenario:
     */
    @Test
    public void testUpdateGraphMeta_AutonomyPlatformAndOwnerIdNotMatch() {
        when(graphRepository.findById(any(ProjectGraphDO.UPK.class))).thenReturn(Optional.of(createProjectGraphDO()));
        UpdateGraphMetaRequest request = new UpdateGraphMetaRequest("projectId", "graphId", "newName");
        assertThrows(SecretpadException.class, () -> graphService.updateGraphMeta(request));
    }

    @Test
    public void testUpdateGraphMeta_AutonomyPlatformAndOwnerIdMatch() {
        UserContextDTO user = UserContext.getUser();
        user.setPlatformType(PlatformTypeEnum.AUTONOMY);
        user.setOwnerId("ownerId");

        when(graphRepository.findById(any(ProjectGraphDO.UPK.class))).thenReturn(Optional.of(createProjectGraphDO()));
        UpdateGraphMetaRequest request = new UpdateGraphMetaRequest("projectId", "graphId", "newName");
        graphService.updateGraphMeta(request);

        verify(graphRepository, times(1)).save(any(ProjectGraphDO.class));
    }

    private ProjectGraphDO createProjectGraphDO() {
        ProjectGraphDO projectGraphDO = new ProjectGraphDO();
        projectGraphDO.setId(1L);
        projectGraphDO.setName("name");
        projectGraphDO.setOwnerId("ownerId");
        return projectGraphDO;
    }
}