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
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.secretflow.v1alpha1.kusciaapi.CertificateServiceGrpc;

/**
 * @author yutu
 * @date 2024/06/19
 */
public class CertificateService extends CertificateServiceGrpc.CertificateServiceImplBase implements CommonService {

    @Override
    public void generateKeyCerts(Certificate.GenerateKeyCertsRequest request, StreamObserver<Certificate.GenerateKeyCertsResponse> responseObserver) {
        Certificate.GenerateKeyCertsResponse resp = Certificate.GenerateKeyCertsResponse.newBuilder().setStatus(getStatus()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}