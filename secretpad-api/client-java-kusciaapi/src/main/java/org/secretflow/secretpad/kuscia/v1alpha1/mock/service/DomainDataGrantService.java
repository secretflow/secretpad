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
import org.secretflow.v1alpha1.kusciaapi.DomainDataGrantServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindatagrant;

/**
 * @author yutu
 * @date 2024/06/19
 */
public class DomainDataGrantService extends DomainDataGrantServiceGrpc.DomainDataGrantServiceImplBase implements CommonService {

    @Override
    public void queryDomainDataGrant(Domaindatagrant.QueryDomainDataGrantRequest request, StreamObserver<Domaindatagrant.QueryDomainDataGrantResponse> responseObserver) {
        Domaindatagrant.QueryDomainDataGrantResponse resp = Domaindatagrant.QueryDomainDataGrantResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void createDomainDataGrant(Domaindatagrant.CreateDomainDataGrantRequest request, StreamObserver<Domaindatagrant.CreateDomainDataGrantResponse> responseObserver) {
        Domaindatagrant.CreateDomainDataGrantResponse resp = Domaindatagrant.CreateDomainDataGrantResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void updateDomainDataGrant(Domaindatagrant.UpdateDomainDataGrantRequest request, StreamObserver<Domaindatagrant.UpdateDomainDataGrantResponse> responseObserver) {
        Domaindatagrant.UpdateDomainDataGrantResponse resp = Domaindatagrant.UpdateDomainDataGrantResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteDomainDataGrant(Domaindatagrant.DeleteDomainDataGrantRequest request, StreamObserver<Domaindatagrant.DeleteDomainDataGrantResponse> responseObserver) {
        Domaindatagrant.DeleteDomainDataGrantResponse resp = Domaindatagrant.DeleteDomainDataGrantResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void batchQueryDomainDataGrant(Domaindatagrant.BatchQueryDomainDataGrantRequest request, StreamObserver<Domaindatagrant.BatchQueryDomainDataGrantResponse> responseObserver) {
        Domaindatagrant.BatchQueryDomainDataGrantResponse resp = Domaindatagrant.BatchQueryDomainDataGrantResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}