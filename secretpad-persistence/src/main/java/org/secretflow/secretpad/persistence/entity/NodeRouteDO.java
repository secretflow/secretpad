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

import org.secretflow.secretpad.persistence.converter.Boolean2IntConverter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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
// @SQLDelete(sql = "update node_route set is_deleted = 1 where id = ?")
@Where(clause = "is_deleted = 0")
@EntityListeners(AuditingEntityListener.class)
public class NodeRouteDO {

    @Id
    @Column(name = "id", unique = true, insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "is_deleted", nullable = false, length = 1)
    @Convert(converter = Boolean2IntConverter.class)
    Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "gmt_create", nullable = false, insertable = false, updatable = false)
    LocalDateTime gmtCreate;

    @LastModifiedDate
    @Column(name = "gmt_modified", nullable = false, insertable = false, updatable = false)
    LocalDateTime gmtModified;

    @Column(name = "src_node_id", nullable = false, length = 64)
    private String srcNodeId;
    @Column(name = "dst_node_id", nullable = false, length = 64)
    private String dstNodeId;

    private String srcNetAddress;
    private String dstNetAddress;

    @CreatedBy
    private String createBy;
    @LastModifiedBy
    private String updateBy;
}
