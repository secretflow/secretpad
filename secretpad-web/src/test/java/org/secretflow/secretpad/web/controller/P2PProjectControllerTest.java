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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.service.model.project.ArchiveProjectRequest;
import org.secretflow.secretpad.service.model.project.CreateProjectRequest;
import org.secretflow.secretpad.service.model.project.UpdateProjectRequest;
import org.secretflow.secretpad.web.controller.p2p.P2PProjectController;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author chenmingliang
 * @date 2024/01/04
 */
public class P2PProjectControllerTest extends ControllerTest {

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private ProjectNodeRepository projectNodeRepository;

    @Test
    void createProject() throws Exception {
        assertResponse(() -> {
            CreateProjectRequest request = new CreateProjectRequest();
            request.setName("test");
            request.setDescription("test project");
            request.setComputeMode("mpc");

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_CREATE));

            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(ProjectDO.builder().projectId(PROJECT_ID).build()));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "createP2PProject", CreateProjectRequest.class))
                    .content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void listP2PProject() throws Exception {
        assertResponse(() -> {
            Mockito.when(projectRepository.findAll()).thenReturn(Collections.emptyList());
            Mockito.when(projectNodeRepository.findByNodeId(Mockito.anyString())).thenReturn(Collections.emptyList());
            Mockito.when(projectRepository.findAllById(Mockito.anySet())).thenReturn(Collections.emptyList());
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "listP2PProject"));
        });
        UserContext.remove();
    }

    private ProjectNodeDO buildProjectNodeDO() {
        return ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK(PROJECT_ID, "alice")).build();
    }

    private ProjectDO buildProjectDO() {
        return ProjectDO.builder().projectId(PROJECT_ID).ownerId("alice").build();
    }

    @Test
    void updateProject() throws Exception {
        assertResponseWithEmptyData(() -> {
            UpdateProjectRequest request = FakerUtils.fake(UpdateProjectRequest.class);
            request.setProjectId(PROJECT_ID);

            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_UPDATE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(buildProjectDO()));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "updateProject", UpdateProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }

    @Test
    void projectArchive() throws Exception {
        assertResponseWithEmptyData(() -> {
            ArchiveProjectRequest request = FakerUtils.fake(ArchiveProjectRequest.class);
            request.setProjectId(PROJECT_ID);
            ProjectDO projectDO = buildProjectDO();
            projectDO.setStatus(ProjectStatusEnum.REVIEWING.getCode());
            UserContext.getUser().setApiResources(Set.of(ApiResourceCodeConstants.PRJ_ARCHIVE));
            Mockito.when(projectNodeRepository.findById(Mockito.any())).thenReturn(Optional.of(buildProjectNodeDO()));
            Mockito.when(projectRepository.findById(Mockito.anyString())).thenReturn(Optional.of(projectDO));
            return MockMvcRequestBuilders.post(getMappingUrl(P2PProjectController.class, "projectArchive", ArchiveProjectRequest.class)).
                    content(JsonUtils.toJSONString(request));
        });
    }
}
