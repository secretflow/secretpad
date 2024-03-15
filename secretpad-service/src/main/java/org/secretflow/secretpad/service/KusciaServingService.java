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

import org.secretflow.v1alpha1.kusciaapi.Serving;

/**
 * kuscia serving service
 *
 * @author yutu
 * @date 2024/01/22
 */
public interface KusciaServingService {

    /**
     * create serving
     *
     * @param request request
     * @return CreateServingResponse
     */
    Serving.CreateServingResponse createServing(Serving.CreateServingRequest request);


    /**
     * update serving
     *
     * @param request request
     * @return UpdateServingResponse
     */
    Serving.UpdateServingResponse updateServing(Serving.UpdateServingRequest request);

    /**
     * delete serving
     *
     * @param request request
     * @return DeleteServingResponse
     */
    Serving.DeleteServingResponse deleteServing(Serving.DeleteServingRequest request);

    /**
     * query serving
     *
     * @param request request
     * @return QueryServingResponse
     */
    Serving.QueryServingResponse queryServing(Serving.QueryServingRequest request);

    /**
     * batch query serving status
     *
     * @param request request
     * @return BatchQueryServingStatusResponse
     */
    Serving.BatchQueryServingStatusResponse batchQueryServingStatus(Serving.BatchQueryServingStatusRequest request);
}