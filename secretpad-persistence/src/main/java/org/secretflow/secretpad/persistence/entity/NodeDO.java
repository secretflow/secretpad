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

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Node data object
 *
 * @author xiaonan
 * @date 2023/5/25
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "node")
@ToString
@Getter
@Setter
@SQLDelete(sql = "update node set is_deleted = 1 where node_id = ?")
@Where(clause = "is_deleted = 0")
@EntityListeners(AuditingEntityListener.class)
public class NodeDO extends BaseAggregationRoot<NodeDO> {

    /**
     * Node id
     */
    @Id
    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    /**
     * Node name
     */
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    /**
     * Node authorization
     */
    @Column(name = "auth", columnDefinition = "text")
    private String auth;

    /**
     * Node description
     */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    private String controlNodeId;
    private String netAddress;
    private String token;
    private String type;
    @CreatedBy
    private String createBy;
    @LastModifiedBy
    private String updateBy;

}
