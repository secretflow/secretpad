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

import org.secretflow.secretpad.common.enums.ResourceTypeEnum;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sys_resource")
@SQLDelete(sql = "update sys_resource set is_deleted = 1 where id = ?")
@Where(clause = "is_deleted = 0")
public class SysResourceDO extends BaseAggregationRoot<SysResourceDO> {
    // empty @Id for ignore jpa error.
    @Id
    /**
     * Node id
     */
    @Unique
    @Column(name = "resource_code", nullable = false, length = 64)
    private String resourceCode;

    /**
     * Node name
     */
    @Column(name = "resource_name", length = 64)
    private String resourceName;

    @Column(name = "resource_type", length = 16)
    @Enumerated(EnumType.STRING)
    private ResourceTypeEnum resourceType;

}
