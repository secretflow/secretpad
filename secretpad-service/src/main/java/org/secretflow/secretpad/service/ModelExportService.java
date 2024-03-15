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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.manager.integration.model.ModelExportDTO;
import org.secretflow.secretpad.service.model.model.export.*;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

/**
 * @author yutu
 * @date 2024/01/29
 */
public interface ModelExportService {

    /**
     * export model
     *
     * @param request request
     * @return response response
     */
    ModelExportPackageResponse exportModel(ModelExportPackageRequest request) throws InvalidProtocolBufferException;

    /**
     * query model status
     *
     * @param request request
     * @return response response
     */
    ModelExportDTO queryModel(ModelExportStatusRequest request);


    /**
     * find model party path
     *
     * @param request request
     * @return response response
     */
    List<ModelPartyPathResponse> modelPartyPath(ModelPartyPathRequest request);
}