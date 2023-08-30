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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * User account data object
 *
 * @author : xiaonan.fhn
 * @date 2023/05/25
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_accounts")
@ToString
@Getter
@Setter
@SQLDelete(sql = "update user_accounts set is_deleted = 1 where inst_id = ?")
@Where(clause = "is_deleted = 0")
public class AccountsDO extends BaseAggregationRoot<AccountsDO> {
    /**
     * User name
     */
    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * User password
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

}
