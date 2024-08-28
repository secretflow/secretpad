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

package org.secretflow.secretpad.web.controller.p2p;

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.constant.resource.ApiResourceCodeConstants;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.project.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cml
 * @date 2023/11/28
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/p2p/project")
public class P2PProjectController {

    private final ProjectService projectService;

    public P2PProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }


    /**
     * Create a new project api
     *
     * @param request create project request
     * @return successful SecretPadResponse with create project view object
     */
    @ResponseBody
    @ApiResource(code = ApiResourceCodeConstants.PRJ_CREATE)
    @PostMapping(value = "/create", consumes = "application/json")
    @Operation(summary = "create a new project", description = "create a new project")
    public SecretPadResponse<CreateProjectVO> createP2PProject(@Valid @RequestBody CreateProjectRequest request) {
        return SecretPadResponse.success(new CreateProjectVO(projectService.createP2PProject(request)));
    }

    /**
     * List project api
     *
     * @return successful SecretPadResponse with project view object list
     */
    @ResponseBody
    @PostMapping(value = "/list", consumes = "application/json")
    @Operation(summary = "list project", description = "list project")
    @ApiResource(code = ApiResourceCodeConstants.PRJ_LIST)
    public SecretPadResponse<List<ProjectVO>> listP2PProject() {
        return SecretPadResponse.success(projectService.listP2PProject());
    }

    /**
     * Update project api
     *
     * @param request update project request
     * @return successful SecretPadResponse with null data
     */
    @ResponseBody
    @PostMapping(value = "/update", consumes = "application/json")
    @Operation(summary = "update project", description = "update project")
    @DataResource(field = "projectId", resourceType = DataResourceTypeEnum.PROJECT_ID)
    @ApiResource(code = ApiResourceCodeConstants.PRJ_UPDATE)
    public SecretPadResponse<Void> updateProject(@Valid @RequestBody UpdateProjectRequest request) {
        projectService.updateP2PProject(request);
        return SecretPadResponse.success();
    }


    /**
     * Archive project api
     *
     * @return successful SecretPadResponse with project archive
     */
    @ResponseBody
    @PostMapping(value = "/archive", consumes = "application/json")
    @Operation(summary = "archive project", description = "archive project")
    @ApiResource(code = ApiResourceCodeConstants.PRJ_ARCHIVE)
    public SecretPadResponse<List<ProjectVO>> projectArchive(@Valid @RequestBody ArchiveProjectRequest archiveProjectRequest) {
        projectService.archiveProject(archiveProjectRequest);
        return SecretPadResponse.success();
    }

    @ResponseBody
    @PostMapping(value = "/participants", consumes = "application/json")
    @Operation(summary = "project participants", description = "project participants")
    @ApiResource(code = ApiResourceCodeConstants.PRJ_PARTICIPANTS)
    public SecretPadResponse<ProjectParticipantsDetailVO> projectParticipants(@Valid @RequestBody ProjectParticipantsRequest request) {
        return SecretPadResponse.success(projectService.getProjectParticipants(request));
    }


}
