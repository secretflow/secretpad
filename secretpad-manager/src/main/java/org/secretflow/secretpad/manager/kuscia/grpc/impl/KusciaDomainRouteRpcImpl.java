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

package org.secretflow.secretpad.manager.kuscia.grpc.impl;

import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainRouteRpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.DomainRouteServiceGrpc;
import org.springframework.stereotype.Service;

/**
 * @author yutu
 * @date 2023/08/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KusciaDomainRouteRpcImpl implements KusciaDomainRouteRpc {
    private final DomainRouteServiceGrpc.DomainRouteServiceBlockingStub domainRouteServiceBlockingStub;

    @Override
    public DomainRoute.CreateDomainRouteResponse createDomainRoute(DomainRoute.CreateDomainRouteRequest request) {
        log.info("DomainRouteServiceGrpc createDomainRoute request{}", request);
        DomainRoute.CreateDomainRouteResponse response = domainRouteServiceBlockingStub.createDomainRoute(request);
        log.info("DomainRouteServiceGrpc createDomainRoute response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainRoute.DeleteDomainRouteResponse deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest request) {
        log.info("DomainRouteServiceGrpc deleteDomainRoute request{}", request);
        DomainRoute.DeleteDomainRouteResponse response = domainRouteServiceBlockingStub.deleteDomainRoute(request);
        log.info("DomainRouteServiceGrpc deleteDomainRoute response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainRoute.QueryDomainRouteResponse queryDomainRoute(DomainRoute.QueryDomainRouteRequest request) {
        log.info("DomainRouteServiceGrpc queryDomainRoute request{}", request);
        DomainRoute.QueryDomainRouteResponse response = domainRouteServiceBlockingStub.queryDomainRoute(request);
        log.info("DomainRouteServiceGrpc queryDomainRoute response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainRoute.BatchQueryDomainRouteStatusResponse
    batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest request) {
        log.info("DomainRouteServiceGrpc batchQueryDomainRouteStatus request{}", request);
        DomainRoute.BatchQueryDomainRouteStatusResponse response =
                domainRouteServiceBlockingStub.batchQueryDomainRouteStatus(request);
        log.info("DomainRouteServiceGrpc batchQueryDomainRouteStatus response{}", response);
        checkResponse(response.getStatus());
        return response;
    }
}