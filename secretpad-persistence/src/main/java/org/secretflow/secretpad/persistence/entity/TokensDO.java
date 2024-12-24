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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * User token data object
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_tokens")
@ToString
@Getter
@Setter
@SQLDelete(sql = "update user_tokens set is_deleted = 1 where token = ?")
@SQLRestriction("is_deleted = 0")
public class TokensDO extends BaseAggregationRoot<TokensDO> {

    /**
     * User token value
     */
    @Id
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    /**
     * User token name
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * The time when user uses token
     */
    @Column(name = "gmt_token")
    private LocalDateTime gmtToken;


    /**
     * session data
     */
    @Column(name = "session_data")
    private String sessionData;

}
