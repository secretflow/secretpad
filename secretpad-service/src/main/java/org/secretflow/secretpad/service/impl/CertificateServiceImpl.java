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

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.kuscia.grpc.KusciaCertificateRpc;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.embedded.EmbeddedChannelService;

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
    private KusciaCertificateRpc kusciaCertificateRpc;

    @Resource
    private EmbeddedChannelService embeddedChannelService;

    @Resource
    private EnvService envService;


    @Override
    public Certificate.GenerateKeyCertsResponse generateCertByNodeID(String nodeID) {
        Certificate.GenerateKeyCertsResponse generateKeyCertsResponse;
        if (!PlatformTypeEnum.AUTONOMY.equals(envService.getPlatformType()) && envService.isEmbeddedNode(nodeID)) {
            generateKeyCertsResponse = embeddedChannelService.getCertificateServiceBlockingStub(nodeID).generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().setCommonName("vote").setKeyType("PKCS#8").build());
            if (generateKeyCertsResponse.getStatus().getCode() != 0) {
                throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, generateKeyCertsResponse.getStatus().getMessage());
            }
        } else {
            generateKeyCertsResponse = kusciaCertificateRpc.generateKeyCerts(Certificate.GenerateKeyCertsRequest.newBuilder().setCommonName("vote").setKeyType("PKCS#8").build());
        }
        return generateKeyCertsResponse;
    }

}
