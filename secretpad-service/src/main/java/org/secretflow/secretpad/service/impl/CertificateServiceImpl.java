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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.service.CertificateService;

import jakarta.annotation.Resource;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.springframework.stereotype.Service;

/**
 * @author cml
 * @date 2023/11/15
 */
@Service
public class CertificateServiceImpl implements CertificateService {


    @Resource
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Override
    public Certificate.GenerateKeyCertsResponse generateCertByNodeID(String nodeID) {
        Certificate.GenerateKeyCertsResponse generateKeyCertsResponse =
                kusciaGrpcClientAdapter.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().setCommonName("vote").setKeyType("PKCS#8").build(), nodeID);
        if (generateKeyCertsResponse.getStatus().getCode() != 0) {
            throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, generateKeyCertsResponse.getStatus().getMessage());
        }
        return generateKeyCertsResponse;
    }

}
