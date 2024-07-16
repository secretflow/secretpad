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
import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;

/**
 * @author yutu
 * @date 2024/06/19
 */
public class DomainDataService extends DomainDataServiceGrpc.DomainDataServiceImplBase implements CommonService {

    @Override
    public void queryDomainData(Domaindata.QueryDomainDataRequest request, StreamObserver<Domaindata.QueryDomainDataResponse> responseObserver) {
        Domaindata.QueryDomainDataResponse resp = Domaindata.QueryDomainDataResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void createDomainData(Domaindata.CreateDomainDataRequest request, StreamObserver<Domaindata.CreateDomainDataResponse> responseObserver) {
        Domaindata.CreateDomainDataResponse resp = Domaindata.CreateDomainDataResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void updateDomainData(Domaindata.UpdateDomainDataRequest request, StreamObserver<Domaindata.UpdateDomainDataResponse> responseObserver) {
        Domaindata.UpdateDomainDataResponse resp = Domaindata.UpdateDomainDataResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteDomainData(Domaindata.DeleteDomainDataRequest request, StreamObserver<Domaindata.DeleteDomainDataResponse> responseObserver) {
        Domaindata.DeleteDomainDataResponse resp = Domaindata.DeleteDomainDataResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest request, StreamObserver<Domaindata.BatchQueryDomainDataResponse> responseObserver) {
        Domaindata.BatchQueryDomainDataResponse resp = Domaindata.BatchQueryDomainDataResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void listDomainData(Domaindata.ListDomainDataRequest request, StreamObserver<Domaindata.ListDomainDataResponse> responseObserver) {
        Domaindata.ListDomainDataResponse resp = Domaindata.ListDomainDataResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

}