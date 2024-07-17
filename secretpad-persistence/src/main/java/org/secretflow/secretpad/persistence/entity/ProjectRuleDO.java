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

import java.io.Serial;
import java.io.Serializable;

/**
 * Project rule data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "project_rule")
@SQLDelete(sql = "update project_rule set is_deleted = 1 where project_id = ? and rule_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectRuleDO extends BaseAggregationRoot {
    /**
     * Project rule unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Project rule unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    @ToString
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId;
        /**
         * Rule id
         */
        @Column(name = "rule_id", nullable = false, length = 64)
        private String ruleId;
    }
}
