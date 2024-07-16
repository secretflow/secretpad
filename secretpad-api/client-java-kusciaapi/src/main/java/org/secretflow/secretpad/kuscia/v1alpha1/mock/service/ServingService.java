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
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.secretflow.v1alpha1.kusciaapi.ServingServiceGrpc;

/**
 * @author yutu
 * @date 2024/06/19
 */
public class ServingService extends ServingServiceGrpc.ServingServiceImplBase implements CommonService {

    @Override
    public void queryServing(Serving.QueryServingRequest request, StreamObserver<Serving.QueryServingResponse> responseObserver) {
        Serving.QueryServingResponse resp = Serving.QueryServingResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void createServing(Serving.CreateServingRequest request, StreamObserver<Serving.CreateServingResponse> responseObserver) {
        Serving.CreateServingResponse resp = Serving.CreateServingResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteServing(Serving.DeleteServingRequest request, StreamObserver<Serving.DeleteServingResponse> responseObserver) {
        Serving.DeleteServingResponse resp = Serving.DeleteServingResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void updateServing(Serving.UpdateServingRequest request, StreamObserver<Serving.UpdateServingResponse> responseObserver) {
        Serving.UpdateServingResponse resp = Serving.UpdateServingResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void batchQueryServingStatus(Serving.BatchQueryServingStatusRequest request, StreamObserver<Serving.BatchQueryServingStatusResponse> responseObserver) {
        Serving.BatchQueryServingStatusResponse resp = Serving.BatchQueryServingStatusResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}