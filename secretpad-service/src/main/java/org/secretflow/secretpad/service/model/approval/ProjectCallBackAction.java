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

package org.secretflow.secretpad.service.model.approval;

import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectInstDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodeDO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2023/12/11
 */
@Getter
@Setter
public class ProjectCallBackAction {

    private ProjectDO projectDO;

    private List<ProjectInstDO> projectInstDOS;
    private List<ProjectNodeDO> projectNodeDOS;

}
