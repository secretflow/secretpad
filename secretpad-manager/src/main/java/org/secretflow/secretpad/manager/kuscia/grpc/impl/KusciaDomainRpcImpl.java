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

import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainRpc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.springframework.stereotype.Service;

/**
 * @author yutu
 * @date 2023/08/09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KusciaDomainRpcImpl implements KusciaDomainRpc {

    private final DomainServiceGrpc.DomainServiceBlockingStub domainServiceBlockingStub;

    @Override
    public Domain.CreateDomainResponse createDomain(Domain.CreateDomainRequest request) {
        log.info("DomainServiceGrpc createDomain request{}", request);
        Domain.CreateDomainResponse response = domainServiceBlockingStub.createDomain(request);
        log.info("DomainServiceGrpc createDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domain.QueryDomainResponse queryDomain(Domain.QueryDomainRequest request) {
        log.info("DomainServiceGrpc queryDomain request{}", request);
        Domain.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        log.info("DomainServiceGrpc queryDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domain.QueryDomainResponse queryDomainNoCheck(Domain.QueryDomainRequest request) {
        log.info("DomainServiceGrpc queryDomain request{}", request);
        Domain.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        log.info("DomainServiceGrpc queryDomain response{}", response);
        return response;
    }

    @Override
    public Domain.UpdateDomainResponse updateDomain(Domain.UpdateDomainRequest request) {
        log.info("DomainServiceGrpc updateDomain request{}", request);
        Domain.UpdateDomainResponse response = domainServiceBlockingStub.updateDomain(request);
        log.info("DomainServiceGrpc updateDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domain.DeleteDomainResponse deleteDomain(Domain.DeleteDomainRequest request) {
        log.info("DomainServiceGrpc deleteDomain request{}", request);
        Domain.DeleteDomainResponse response = domainServiceBlockingStub.deleteDomain(request);
        log.info("DomainServiceGrpc deleteDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domain.BatchQueryDomainStatusResponse batchQueryDomainStatus(Domain.BatchQueryDomainStatusRequest request) {
        log.info("DomainServiceGrpc batchQueryDomainStatus request{}", request);
        Domain.BatchQueryDomainStatusResponse response = domainServiceBlockingStub.batchQueryDomainStatus(request);
        log.info("DomainServiceGrpc batchQueryDomainStatus response{}", response);
        checkResponse(response.getStatus());
        return response;
    }
}