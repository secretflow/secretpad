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

package org.secretflow.secretpad.manager.kuscia.grpc.impl;

import org.secretflow.secretpad.manager.kuscia.grpc.KusciaServingRpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.secretflow.v1alpha1.kusciaapi.ServingServiceGrpc;
import org.springframework.stereotype.Service;

/**
 * @author chenmingliang
 * @date 2024/01/22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KusciaServingRpcImpl implements KusciaServingRpc {

    private final ServingServiceGrpc.ServingServiceBlockingStub servingServiceBlockingStub;

    @Override
    public Serving.CreateServingResponse createServing(Serving.CreateServingRequest createServingRequest) {
        log.info("ServingServiceGrpc createServing request{}", createServingRequest);
        Serving.CreateServingResponse createServingResponse = servingServiceBlockingStub.createServing(createServingRequest);
        log.info("ServingServiceGrpc createServing createServingResponse{}", createServingResponse);
        checkResponse(createServingResponse.getStatus());
        return createServingResponse;
    }

    @Override
    public Serving.DeleteServingResponse deleteServing(Serving.DeleteServingRequest deleteServingRequest) {
        log.info("ServingServiceGrpc deleteServingRequest request{}", deleteServingRequest);
        Serving.DeleteServingResponse deleteServingResponse = servingServiceBlockingStub.deleteServing(deleteServingRequest);
        log.info("ServingServiceGrpc deleteServingRequest deleteServingResponse{}", deleteServingResponse);
        checkResponse(deleteServingResponse.getStatus());
        return deleteServingResponse;
    }

    @Override
    public Serving.QueryServingResponse queryServing(Serving.QueryServingRequest queryServingRequest) {
        log.info("ServingServiceGrpc queryServingRequest request{}", queryServingRequest);
        Serving.QueryServingResponse queryServingResponse = servingServiceBlockingStub.queryServing(queryServingRequest);
        log.info("ServingServiceGrpc queryServing queryServingResponse{}", queryServingResponse);
        checkResponse(queryServingResponse.getStatus());
        return queryServingResponse;
    }
}
