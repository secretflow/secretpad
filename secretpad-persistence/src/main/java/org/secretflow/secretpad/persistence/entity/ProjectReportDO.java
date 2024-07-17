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

/**
 * Project report data object
 *
 * @author yansi
 * @date 2023/6/12
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "project_report")
@SQLDelete(sql = "update project_report set is_deleted = 1 where project_id = ? and report_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectReportDO extends BaseAggregationRoot {
    /**
     * Project report unique primary key
     */
    @EmbeddedId
    private UPK upk;
    /**
     * Project report content
     */
    @Column(name = "content")
    private String content;

    /**
     * Project report unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
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
         * Report id
         */
        @Column(name = "report_id", nullable = false, length = 64)
        private String reportId;
    }
}
