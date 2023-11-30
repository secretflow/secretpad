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
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
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
    public DomainOuterClass.CreateDomainResponse createDomain(DomainOuterClass.CreateDomainRequest request) {
        log.info("DomainServiceGrpc createDomain request{}", request);
        DomainOuterClass.CreateDomainResponse response = domainServiceBlockingStub.createDomain(request);
        log.info("DomainServiceGrpc createDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainOuterClass.QueryDomainResponse queryDomain(DomainOuterClass.QueryDomainRequest request) {
        log.info("DomainServiceGrpc queryDomain request{}", request);
        DomainOuterClass.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        log.info("DomainServiceGrpc queryDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainOuterClass.QueryDomainResponse queryDomainNoCheck(DomainOuterClass.QueryDomainRequest request) {
        log.info("DomainServiceGrpc queryDomain request{}", request);
        DomainOuterClass.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        log.info("DomainServiceGrpc queryDomain response{}", response);
        return response;
    }

    @Override
    public DomainOuterClass.UpdateDomainResponse updateDomain(DomainOuterClass.UpdateDomainRequest request) {
        log.info("DomainServiceGrpc updateDomain request{}", request);
        DomainOuterClass.UpdateDomainResponse response = domainServiceBlockingStub.updateDomain(request);
        log.info("DomainServiceGrpc updateDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainOuterClass.DeleteDomainResponse deleteDomain(DomainOuterClass.DeleteDomainRequest request) {
        log.info("DomainServiceGrpc deleteDomain request{}", request);
        DomainOuterClass.DeleteDomainResponse response = domainServiceBlockingStub.deleteDomain(request);
        log.info("DomainServiceGrpc deleteDomain response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainOuterClass.BatchQueryDomainResponse batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest request) {
        log.info("DomainServiceGrpc batchQueryDomainStatus request{}", request);
        DomainOuterClass.BatchQueryDomainResponse response = domainServiceBlockingStub.batchQueryDomain(request);
        log.info("DomainServiceGrpc batchQueryDomainStatus response{}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public DomainOuterClass.BatchQueryDomainResponse batchQueryDomainNoCheck(DomainOuterClass.BatchQueryDomainRequest request) {
        log.info("DomainServiceGrpc batchQueryDomainStatus request{}", request);
        DomainOuterClass.BatchQueryDomainResponse response = domainServiceBlockingStub.batchQueryDomain(request);
        log.info("DomainServiceGrpc batchQueryDomainStatus response{}", response);
        return response;
    }
}