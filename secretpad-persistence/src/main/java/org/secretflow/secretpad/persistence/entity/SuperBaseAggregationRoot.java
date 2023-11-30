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
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;

/**
 * @author beiwei
 * @date 2023/9/13
 */
@Getter
@Setter
@MappedSuperclass
public class SuperBaseAggregationRoot<A extends AbstractAggregateRoot<A>> extends AbstractAggregateRoot<A> {
    /**
     * The id of the database is automatically added
     */
//    @Id
    @Column(name = "id", unique = true, insertable = false, updatable = false)
    Long id;

    /**
     * Start time
     */
    @CreatedDate
    @Column(name = "gmt_create", nullable = false, insertable = false, updatable = false)
    LocalDateTime gmtCreate;

    /**
     * Update time
     */
    @LastModifiedDate
    @Column(name = "gmt_modified", nullable = false, insertable = false, updatable = false)
    LocalDateTime gmtModified;
}
