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

import org.secretflow.secretpad.persistence.converter.BaseObjectListJsonConverter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Project federal table data object
 *
 * @author jiezi
 * @date 2023/5/25
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "project_fed_table")
@SQLDelete(sql = "update project_fed_table set is_deleted = 1 where fed_table_id = ? and project_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectFedTableDO implements Serializable {
    @Serial
    private static final long serialVersionUID = 291568296509217011L;

    /**
     * Project federal table unique primary key
     */
    @EmbeddedId
    private UPK upk;

    /**
     * Project federal table joins, store nodeId and datatableId
     */
    @Column(name = "joins", nullable = false, columnDefinition = "text")
    @Convert(converter = JoinsConverter.class)
    private List<JoinItem> joins = new ArrayList<>();

    /**
     * Project federal table unique primary key
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
         * Federal table id
         */
        @Column(name = "fed_table_id", nullable = false, length = 64)
        private String fedTableId;
    }

    /**
     * Project federal table join item
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JoinItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;

        /**
         * Node id
         */
        private String nodeId;
        /**
         * Datatable id
         */
        private String datatableId;
    }

    @Converter
    public static class JoinsConverter extends BaseObjectListJsonConverter<JoinItem> {
        public JoinsConverter() {
            super(JoinItem.class);
        }
    }
}
