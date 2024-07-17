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

package org.secretflow.secretpad.kuscia.v1alpha1.mock.service;

import io.grpc.stub.StreamObserver;
import org.secretflow.v1alpha1.kusciaapi.DomainDataSourceServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author yutu
 * @date 2024/06/19
 */
public class DomainDatasourceService extends DomainDataSourceServiceGrpc.DomainDataSourceServiceImplBase implements CommonService {

    @Override
    public void queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request, StreamObserver<Domaindatasource.QueryDomainDataSourceResponse> responseObserver) {
        Domaindatasource.QueryDomainDataSourceResponse resp = Domaindatasource.QueryDomainDataSourceResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request, StreamObserver<Domaindatasource.CreateDomainDataSourceResponse> responseObserver) {
        Domaindatasource.CreateDomainDataSourceResponse resp = Domaindatasource.CreateDomainDataSourceResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request, StreamObserver<Domaindatasource.UpdateDomainDataSourceResponse> responseObserver) {
        Domaindatasource.UpdateDomainDataSourceResponse resp = Domaindatasource.UpdateDomainDataSourceResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request, StreamObserver<Domaindatasource.DeleteDomainDataSourceResponse> responseObserver) {
        Domaindatasource.DeleteDomainDataSourceResponse resp = Domaindatasource.DeleteDomainDataSourceResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request, StreamObserver<Domaindatasource.BatchQueryDomainDataSourceResponse> responseObserver) {
        Domaindatasource.BatchQueryDomainDataSourceResponse resp = Domaindatasource.BatchQueryDomainDataSourceResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request, StreamObserver<Domaindatasource.ListDomainDataSourceResponse> responseObserver) {
        Domaindatasource.ListDomainDataSourceResponse resp = Domaindatasource.ListDomainDataSourceResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}