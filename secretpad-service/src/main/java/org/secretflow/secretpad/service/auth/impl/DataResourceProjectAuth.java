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
package org.secretflow.secretpad.service.auth.impl;

import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * project resource auth check
 *
 * @author yeuxie
 * @date 2023-10-20
 */
@Component
public class DataResourceProjectAuth {

    @Autowired
    private ProjectService projectService;

    public boolean check(String resourceId) {
        boolean exist = projectService.checkOwnerInProject(resourceId, UserContext.getUser().getOwnerId());
        if (!exist) {
            ProjectDO project = projectService.openProject(resourceId);
            if (ProjectStatusEnum.ARCHIVED.getCode().equals(project.getStatus())) {
                return projectService.checkNodeArchive(resourceId);
            }
        }
        return exist;
    }
}
