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

/**
 * Datatable service interface
 *
 * @author xiaonan
 * @date 2023/6/7
 */
public interface DatatableService {
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
    DatatableVO getDatatable(GetDatatableRequest request);

    /**
     * Delete datatable by request
     *
     * @param request delete datatable request
     * @return
     */
    void deleteDatatable(DeleteDatatableRequest request);
}
