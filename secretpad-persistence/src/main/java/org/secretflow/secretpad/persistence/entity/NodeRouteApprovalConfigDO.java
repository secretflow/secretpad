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

import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * NodeRouteApprovalConfigDO.
 *
 * @author cml
 * @date 2023/09/20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "node_route_approval_config")
public class NodeRouteApprovalConfigDO extends BaseAggregationRoot<NodeRouteApprovalConfigDO> {

    @Id
    @Column(name = "vote_id", nullable = false, length = 64)
    private String voteID;
    @Column(name = "is_single", nullable = false, length = 1)
    private Boolean isSingle;

    @Column(name = "src_node_id", nullable = false, length = 64)
    private String srcNodeID;

    @Column(name = "src_node_addr", nullable = false, length = 64)
    private String srcNodeAddr;

    @Column(name = "all_participants")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> allParticipants;

    @Column(name = "des_node_id", nullable = false, length = 64)
    private String desNodeID;

    @Column(name = "des_node_addr", nullable = false, length = 64)
    private String desNodeAddr;

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        return allParticipants;
    }
}
