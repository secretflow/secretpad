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

import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainDatasourceRpc;

import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Service
@Slf4j
@AllArgsConstructor
public class KusciaDomainDatasourceRpcImpl implements KusciaDomainDatasourceRpc, InitializingBean {

    @Resource(name = "domainDataSourceServiceBlockingStub")
    private final DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub domainDataSourceServiceBlockingStub;


    @Override
    public Domaindatasource.CreateDomainDataSourceResponse createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request) {
        log.info("DomainDataSourceServiceGrpc createDomainDataSource request {}", request);
        Domaindatasource.CreateDomainDataSourceResponse response = domainDataSourceServiceBlockingStub.createDomainDataSource(request);
        log.info("DomainDataSourceServiceGrpc createDomainDataSource response {}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request) {
        log.info("DomainDataSourceServiceGrpc queryDomainDataSource request {}", request);
        Domaindatasource.QueryDomainDataSourceResponse response = domainDataSourceServiceBlockingStub.queryDomainDataSource(request);
        log.info("DomainDataSourceServiceGrpc queryDomainDataSource response {}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request) {
        log.info("DomainDataSourceServiceGrpc deleteDomainDataSource request {}", request);
        Domaindatasource.DeleteDomainDataSourceResponse response = domainDataSourceServiceBlockingStub.deleteDomainDataSource(request);
        log.info("DomainDataSourceServiceGrpc deleteDomainDataSource response {}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domaindatasource.UpdateDomainDataSourceResponse updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request) {
        log.info("DomainDataSourceServiceGrpc updateDomainDataSource request {}", request);
        Domaindatasource.UpdateDomainDataSourceResponse response = domainDataSourceServiceBlockingStub.updateDomainDataSource(request);
        log.info("DomainDataSourceServiceGrpc updateDomainDataSource response {}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domaindatasource.ListDomainDataSourceResponse listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request) {
        log.info("DomainDataSourceServiceGrpc listDomainDataSource request {}", request);
        Domaindatasource.ListDomainDataSourceResponse response = domainDataSourceServiceBlockingStub.listDomainDataSource(request);
        log.info("DomainDataSourceServiceGrpc listDomainDataSource response {}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public Domaindatasource.BatchQueryDomainDataSourceResponse batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request) {
        log.info("DomainDataSourceServiceGrpc batchQueryDomainDataSource request {}", request);
        Domaindatasource.BatchQueryDomainDataSourceResponse response = domainDataSourceServiceBlockingStub.batchQueryDomainDataSource(request);
        log.info("DomainDataSourceServiceGrpc batchQueryDomainDataSource response {}", response);
        checkResponse(response.getStatus());
        return response;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
