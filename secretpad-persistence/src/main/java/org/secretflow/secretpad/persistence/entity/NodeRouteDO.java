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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

/**
 * Node route data object
 *
 * @author xiaonan
 * @date 2023/5/25
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "node_route")
@ToString
@Getter
@Setter
@Where(clause = "is_deleted = 0")
@EqualsAndHashCode(callSuper = true)
public class NodeRouteDO extends BaseAggregationRoot<NodeRouteDO> {

    @Column(name = "src_node_id", nullable = false, length = 64)
    private String srcNodeId;
    @Column(name = "dst_node_id", nullable = false, length = 64)
    private String dstNodeId;

    @Id
    @Column(name = "route_id", unique = true)
    private String routeId = srcNodeId + "__" + dstNodeId;

    private String srcNetAddress;
    private String dstNetAddress;

    @JsonIgnore
    @Override
    public String getProjectId() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(srcNodeId);
        nodeIds.add(dstNodeId);
        return nodeIds;
    }
}

