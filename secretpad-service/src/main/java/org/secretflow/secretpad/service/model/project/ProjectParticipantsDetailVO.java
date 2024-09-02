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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.service.model.message.PartyVoteStatus;
import lombok.*;

import java.util.List;

/**
 * @author cml
 * @date 2023/12/08
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectParticipantsDetailVO {

    /**
     * initiator id
     */
    private String initiatorId;

    /**
     * initiator name
     */
    private String initiatorName;

    /**
     * project name
     */
    private String projectName;

    /**
     * party vote status
     */
    private List<? extends PartyVoteStatus> partyVoteStatuses;

    /**
     * compute mode
     */
    private String computeMode;

    /**
     * compute func
     */
    private String computeFunc;

    /**
     * project description
     */
    private String projectDesc;

    /**
     * gmt created
     */
    private String gmtCreated;

    /**
     * participant node and inst
     */
    private List<ParticipantNodeInstVO> participantNodeInstVOS;

    /**
     * status
     */
    private String status;
}
