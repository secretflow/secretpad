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

package org.secretflow.secretpad.manager.integration.datatable;

import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO.NodeDatatableId;
import org.secretflow.secretpad.manager.integration.model.DatatableListDTO;
import org.secretflow.secretpad.manager.integration.node.SearchTargetNodeManager;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author yansi
 * @date 2023/5/23
 */
public abstract class AbstractDatatableManager {

    public static final String DATA_TYPE_TABLE = "table";

    public static final String DATA_VENDOR_MANUAL = "manual";

    public static final String DATA_VENDOR_GRANT = "grant";

    /**
     * Find Optional of DatatableDTO by nodeDatatableId
     *
     * @param nodeDatatableId datatable id
     * @return Optional of DatatableDTO
     */
    public abstract Optional<DatatableDTO> findById(NodeDatatableId nodeDatatableId);


    /**
     * Find Map of NodeDatatableId and DatatableDTO by nodeDatatableIds
     *
     * @param nodeDatatableIds datatable id list
     * @return Map of NodeDatatableId and DatatableDTO
     */
    public abstract Map<NodeDatatableId, DatatableDTO> findByIds(List<NodeDatatableId> nodeDatatableIds , SearchTargetNodeManager searchTargetNodeManager);


    public abstract List<Domaindata.DomainData> findByIdsBase(List<NodeDatatableId> nodeDatatableIds , SearchTargetNodeManager searchTargetNodeManager);

    /** group targetNode*/
    public abstract List<Domaindata.DomainData> findByIdGroup(List<NodeDatatableId> nodeDatatableIds , SearchTargetNodeManager searchTargetNodeManager);


    public abstract Map<NodeDatatableId, DatatableDTO> findByIdsFromProjectConfig(List<NodeDatatableId> nodeDatatableIds , SearchTargetNodeManager searchTargetNodeManager);


    /**
     * Find DatatableDTO list and number by params
     *
     * @param nodeId              nodeId
     * @param pageSize
     * @param pageNumberfindByIds
     * @param statusFilter
     * @param datatableNameFilter
     * @param types
     * @return DatatableListDTO
     */
    public abstract DatatableListDTO findByNodeId(String nodeId, Integer pageSize, Integer pageNumber, String statusFilter, String datatableNameFilter, List<String> types);

    /**
     * Find DatatableDTO list by nodeId and vendor
     *
     * @param nodeId
     * @param vendor
     * @return DatatableDTO list
     */
    public abstract List<DatatableDTO> findByNodeId(String nodeId, String vendor);

    /**
     * Delete datatable
     *
     * @param nodeDatatableId
     * @return
     */
    public abstract void deleteDataTable(NodeDatatableId nodeDatatableId);

    public abstract List<DatatableDTO> findAllDatatableByNodeId(String nodeId);

}
