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

package org.secretflow.secretpad.persistence.entity;

import org.secretflow.secretpad.persistence.converter.BaseObjectListJsonConverter;
import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cml
 * @date 2023/11/24
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "project_approval_config")
@ToString
public class ProjectApprovalConfigDO extends BaseAggregationRoot<ProjectApprovalConfigDO> {

    @Id
    @Column(name = "vote_id", nullable = false, length = 64)
    private String voteID;

    /**
     * vote initiator
     */
    @Column(name = "initiator", nullable = false, length = 64)
    private String initiator;

    /**
     * project vote type
     * PROJECT_CREATE,
     * PROJECT_ARCHIVE,
     * PROJECT_NODE_ADD,
     */
    @Column(name = "type", length = 16)
    private String type;

    @Column(name = "parties")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> parties;

    @Column(name = "participant_node_info")
    @Convert(converter = ParticipantNodeInstConverter.class)
    private List<ParticipantNodeInstVO> participantNodeInfo;

    @Column(name = "project_id", length = 64)
    private String projectId;

    /**
     * invite a new party of project,this is new party nodeId
     */
    @Column(name = "invite_node_id", length = 64)
    private String inviteNodeId;
    @Converter
    public static class ParticipantNodeInstConverter extends BaseObjectListJsonConverter<ParticipantNodeInstVO> {
        public ParticipantNodeInstConverter() {
            super(ParticipantNodeInstVO.class);
        }
    }

    @Override
    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(parties)) {
            nodeIds.addAll(parties);
        }
        if (StringUtils.isNotBlank(inviteNodeId)) {
            nodeIds.add(inviteNodeId);
        }
        return nodeIds;
    }
}
