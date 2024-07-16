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
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;

/**
 * @author yutu
 * @date 2024/06/19
 */

public class DomainService extends DomainServiceGrpc.DomainServiceImplBase implements CommonService {
    @Override
    public void queryDomain(DomainOuterClass.QueryDomainRequest request, StreamObserver<DomainOuterClass.QueryDomainResponse> responseObserver) {
        DomainOuterClass.QueryDomainResponse resp = DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void createDomain(DomainOuterClass.CreateDomainRequest request, StreamObserver<DomainOuterClass.CreateDomainResponse> responseObserver) {
        DomainOuterClass.CreateDomainResponse resp = DomainOuterClass.CreateDomainResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void updateDomain(DomainOuterClass.UpdateDomainRequest request, StreamObserver<DomainOuterClass.UpdateDomainResponse> responseObserver) {
        DomainOuterClass.UpdateDomainResponse resp = DomainOuterClass.UpdateDomainResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteDomain(DomainOuterClass.DeleteDomainRequest request, StreamObserver<DomainOuterClass.DeleteDomainResponse> responseObserver) {
        DomainOuterClass.DeleteDomainResponse resp = DomainOuterClass.DeleteDomainResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void batchQueryDomain(DomainOuterClass.BatchQueryDomainRequest request, StreamObserver<DomainOuterClass.BatchQueryDomainResponse> responseObserver) {
        DomainOuterClass.BatchQueryDomainResponse resp = DomainOuterClass.BatchQueryDomainResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}