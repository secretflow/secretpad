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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.datatable.*;

import java.util.List;

/**
 * Datatable service interface
 *
 * @author xiaonan
 * @date 2023/6/7
 */
public interface DatatableService {
    /**
     * List all datatable list by ownerId
     *
     * @param request list datatable list request
     * @return all node datatable list view object
     */
    AllDatatableListVO listDatatablesByOwnerId(ListDatatableRequest request);

    /**
     * List datatable list by nodeId
     *
     * @param request list datatable list request
     * @return datatable list view object
     */
    DatatableListVO listDatatablesByNodeId(ListDatatableRequest request);

    /**
     * Query datatable by request
     *
     * @param request query datatable request
     * @return datatable view object
     */
    DatatableNodeVO getDatatable(GetDatatableRequest request);

    /**
     * Delete datatable by request
     *
     * @param request delete datatable request
     * @return
     */
    void deleteDatatable(DeleteDatatableRequest request);

    /**
     * Push datatable to tee node by request
     *
     * @param request push datable to tee node request
     * @return
     */
    void pushDatatableToTeeNode(PushDatatableToTeeRequest request);

    /**
     * Pull result from tee node by request
     *
     * @param nodeId          target edge node id
     * @param datatableId     target datatableId in tee node
     * @param targetTeeNodeId tee node id
     * @param datasourceId    target datasourceId
     * @param relativeUri     target relativeUri
     * @param voteResult      target vote result string
     * @param projectId       target project id
     * @param projectJobId    target project job id
     * @param projectJobId    target project job task id
     * @param resultType      target result type
     */
    void pullResultFromTeeNode(String nodeId, String datatableId, String targetTeeNodeId, String datasourceId, String relativeUri, String voteResult,
                               String projectId, String projectJobId, String projectJobTaskId, String resultType);

    /**
     * create data table
     *
     * @param createDatatableRequest
     * @return
     */
    CreateDatatableVO createDataTable(CreateDatatableRequest createDatatableRequest);


    List<DatatableVO> findDatatableByNodeId(String nodeId);
}
