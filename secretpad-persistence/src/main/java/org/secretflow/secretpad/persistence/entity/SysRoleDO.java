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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "sys_role")
@SQLDelete(sql = "update sys_role set is_deleted = 1 where id = ?")
@Where(clause = "is_deleted = 0")
public class SysRoleDO extends BaseAggregationRoot<SysRoleDO>{
    @Id
    /**
     * Node id
     */
    @Unique
    @Column(name = "role_code", nullable = false, length = 64)
    private String ruleCode;

    /**
     * Node name
     */
    @Column(name = "resource_name", length = 64)
    private String ruleName;

}
