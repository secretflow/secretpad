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

package org.secretflow.secretpad.service.graph;

import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;

import com.google.protobuf.Any;
import com.secretflow.spec.v1.DistData;
import com.secretflow.spec.v1.IndividualTable;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Distributed data view object
 *
 * @author yansi
 * @date 2023/6/6
 */
public class DistDataVO {
    /**
     * Build distributed data from project datatable data object and datatable data transfer object
     *
     * @param projectDatatableDO project datatable data object
     * @param datatableDTO       datatable data transfer object
     * @return distributed data
     */
    public static DistData fromDatatable(ProjectDatatableDO projectDatatableDO, DatatableDTO datatableDTO) {
        DistData.DataRef dataRef = DistData.DataRef.newBuilder()
                .setUri(datatableDTO.getRelativeUri())
                .setParty(datatableDTO.getNodeId())
                .setFormat("csv")
                .build();

        DistData.Builder distDataBuilder = DistData.newBuilder()
                .setType("sf.table.individual")
                .addDataRefs(0, dataRef);

        List<ProjectDatatableDO.TableColumnConfig> tableConfig = projectDatatableDO.getTableConfig();
        if (!CollectionUtils.isEmpty(tableConfig)) {
            IndividualTable vTable = IndividualTable.newBuilder()
                    .setLineCount(-1)
                    .build();
            distDataBuilder.setMeta(Any.pack(vTable));
        }

        return distDataBuilder.build();
    }
}
