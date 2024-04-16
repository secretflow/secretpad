/*
 * Copyright 2024 Ant Group Co., Ltd.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author chenmingliang
 * @date 2024/01/24
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "feature_table")
public class FeatureTableDO extends BaseAggregationRoot<FeatureTableDO> {

    @EmbeddedId
    private UPK upk;

    @Column(name = "feature_table_name", nullable = false, length = 32)
    private String featureTableName;


    @Column(name = "type", nullable = false, length = 8)
    private String type;

    @Column(name = "description")
    private String desc;

    @Column(name = "url", nullable = false, length = 64)
    private String url;

    @Column(name = "columns", nullable = false, columnDefinition = "text")
    @Convert(converter = TableConfigConverter.class)
    private List<TableColumn> columns;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Converter
    public static class TableConfigConverter extends BaseObjectListJsonConverter<FeatureTableDO.TableColumn> {
        public TableConfigConverter() {
            super(FeatureTableDO.TableColumn.class);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UPK implements Serializable {

        @Serial
        private static final long serialVersionUID = -6653511353225143749L;
        @Column(name = "feature_table_id", nullable = false, length = 8)
        private String featureTableId;

        @Column(name = "node_id", nullable = false, length = 64)
        private String nodeId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UPK upk = (UPK) o;
            return Objects.equals(featureTableId, upk.featureTableId) && Objects.equals(nodeId, upk.nodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(featureTableId, nodeId);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TableColumn implements Serializable {
        @Serial
        private static final long serialVersionUID = -8027065560250042170L;
        /**
         * Column name
         */
        private String colName;
        /**
         * Column type
         */
        private String colType;
        /**
         * Column comment
         */
        private String colComment;
    }

    @Override
    @JsonIgnore
    public List<String> getNodeIds() {
        return Lists.newArrayList(this.getUpk().getNodeId());
    }
}
