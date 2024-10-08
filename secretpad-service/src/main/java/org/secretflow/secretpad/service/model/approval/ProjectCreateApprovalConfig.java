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


import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author cml
 * @date 2023/11/27
 */
@Getter
@Setter
public class ProjectCreateApprovalConfig extends AbstractVoteConfig {

    @NotBlank
    private String projectId;

    @NotEmpty
    private List<String> participants;

    private List<ParticipantNodeInstVO> participantNodeInstVOS;


}
