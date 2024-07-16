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
 * Project read_data data object
 *
 * @author yutu
 * @date 2023/12/1
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
@Getter
@Table(name = "project_read_data")
@SQLDelete(sql = "update project_read_data set is_deleted = 1 where project_id = ? and report_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectReadDataDO extends BaseAggregationRoot {
    /**
     * Project read_data unique primary key
     */
    @EmbeddedId
    private UPK upk;
    /**
     * Project read_data content
     */
    @Column(name = "content")
    private String content;

    @Column(name = "raw")
    private String raw;

    @Column(name = "output_id")
    private String outputId;

    @Column(name = "hash")
    private String hash;

    @Column(name = "task")
    private String task;

    @Column(name = "grap_node_id")
    private String grapNodeId;

    /**
     * Project read_data unique primary key
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
