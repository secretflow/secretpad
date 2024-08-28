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

package org.secretflow.secretpad.manager.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.common.Common;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author yutu
 * @date 2024/07/25
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OdpsPartitionParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String type;

    private List<Field> fields;

    public static OdpsPartitionParam fromPartition(Common.Partition partition) {
        return OdpsPartitionParam.builder()
                .type(partition.getType())
                .fields(ObjectUtils.isEmpty(partition.getFieldsList()) ? null : partition.getFieldsList().stream().map(Field::from).toList())
                .build();
    }

    @Data
    public static class Field {
        private String name;
        private String type;
        private String comment;

        public static Field from(Common.DataColumn column) {
            Field field = new Field();
            field.setName(column.getName());
            field.setType(column.getType());
            field.setComment(column.getComment());
            return field;
        }
    }
}