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

package org.secretflow.secretpad.web.controller;


import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.model.datatable.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Datatable controller
 *
 * @author xiaonan
 * @date 2023/05/25
 */
@RestController
@RequestMapping(value = "/api/v1alpha1/datatable")
public class DatatableController {

    @Autowired
    private DatatableService datatableService;

    /**
     * List datatable api
     *
     * @param request list datatable request
     * @return successful SecretPadResponse with datatable list view object
     */
    @ResponseBody
    @PostMapping(value = "/list", consumes = "application/json")
    public SecretPadResponse<DatatableListVO> listDatatables(@RequestBody @Valid ListDatatableRequest request) {
        return SecretPadResponse.success(datatableService.listDatatablesByNodeId(request));
    }

    /**
     * Query datatable api
     *
     * @param request get datatable request
     * @return successful SecretPadResponse with datatable view object
     */
    @ResponseBody
    @PostMapping(value = "/get", consumes = "application/json")
    public SecretPadResponse<DatatableVO> getDatatable(@RequestBody @Valid GetDatatableRequest request) {
        return SecretPadResponse.success(datatableService.getDatatable(request));
    }

    /**
     * Delete datable api
     *
     * @param request delete datatable request
     * @return successful SecretPadResponse with null data
     */
    @PostMapping(value = "/delete", consumes = "application/json")
    public SecretPadResponse deleteDatatable(@RequestBody @Valid DeleteDatatableRequest request) {
        datatableService.deleteDatatable(request);
        return SecretPadResponse.success();
    }

}
