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

import org.secretflow.secretpad.manager.kuscia.grpc.KusciaCertificateRpc;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.secretflow.v1alpha1.kusciaapi.CertificateServiceGrpc;
import org.springframework.stereotype.Service;

/**
 * KusciaCertificateRpcImpl.
 *
 * @author cml
 * @date 2023/10/08
 */
@Service
@Slf4j
public class KusciaCertificateRpcImpl implements KusciaCertificateRpc {


    @Resource(name = "certificateServiceBlockingStub")
    private CertificateServiceGrpc.CertificateServiceBlockingStub certificateServiceBlockingStub;


    @Override
    public Certificate.GenerateKeyCertsResponse generateKeyCerts(Certificate.GenerateKeyCertsRequest generateKeyCertsRequest) {
        log.info("CertificateServiceGrpc generateKeyCerts request{}", generateKeyCertsRequest);
        Certificate.GenerateKeyCertsResponse generateKeyCertsResponse = certificateServiceBlockingStub.generateKeyCerts(generateKeyCertsRequest);
        log.info("CertificateServiceGrpc generateKeyCerts response{}", generateKeyCertsResponse);
        checkResponse(generateKeyCertsResponse.getStatus());
        return generateKeyCertsResponse;
    }


}
