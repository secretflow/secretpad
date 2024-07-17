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
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Project model data object
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
@Table(name = "project_model")
@SQLDelete(sql = "update project_model set is_deleted = 1 where model_id = ? and project_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectModelDO extends BaseAggregationRoot {
    /**
     * Project model unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Project model unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 6840537796122754350L;
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        private String projectId;
        /**
         * Model id
         */
        @Column(name = "model_id", nullable = false, length = 64)
        private String modelId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UPK upk = (UPK) o;
            return Objects.equals(projectId, upk.projectId) && Objects.equals(modelId, upk.modelId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectId, modelId);
        }
    }
}
