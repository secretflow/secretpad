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

package org.secretflow.secretpad.service.model.datatable;

import org.secretflow.secretpad.common.annotation.OneOfType;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.manager.integration.model.OdpsPartitionParam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
public class OdpsPartitionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @OneOfType(types = {DomainDatasourceConstants.ODPS_DATASOURCE_PARTITION_TYPE_ODPS, DomainDatasourceConstants.ODPS_DATASOURCE_PARTITION_TYPE_PATH})
    private String type;

    private List<Field> fields;

    public Common.Partition toPartition() {
        Common.Partition.Builder builder = Common.Partition.newBuilder();
        if (StringUtils.isNotEmpty(type)) {
            builder.setType(type);
        }
        if (!ObjectUtils.isEmpty(fields)) {
            builder.addAllFields(
                    fields.stream().map(field -> Common.DataColumn.newBuilder()
                            .setName(field.getName())
                            .setType(field.getType())
                            .setComment(field.getComment())
                            .build()).toList());
        }
        return builder.build();
    }

    public OdpsPartitionRequest fromOdpsPartitionParam(OdpsPartitionParam odpsPartitionParam) {
        return ObjectUtils.isEmpty(odpsPartitionParam) ? null :
                OdpsPartitionRequest.builder()
                        .type(odpsPartitionParam.getType())
                        .fields(odpsPartitionParam.getFields().stream().map(Field::from).toList())
                        .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Field {
        private String name;
        private String type;
        private String comment;

        public static Field from(Common.DataColumn column) {
            return ObjectUtils.isEmpty(column) ? null : new Field(column.getName(), column.getType(), column.getComment());
        }

        public static Field from(OdpsPartitionParam.Field fieldColumn) {
            return ObjectUtils.isEmpty(fieldColumn) ? null : new Field(fieldColumn.getName(), fieldColumn.getType(), fieldColumn.getComment());
        }
    }
}