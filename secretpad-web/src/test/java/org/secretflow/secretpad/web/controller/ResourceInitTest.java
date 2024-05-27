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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.manager.kuscia.grpc.impl.KusciaDomainRpcImpl;
import org.secretflow.secretpad.service.DatatableService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * @author yutu
 * @date 2024/02/27
 */
@ActiveProfiles({SystemConstants.DEV})
@TestPropertySource(properties = {
        "secretpad.deploy-mode=ALL-IN-ONE",
        "secretpad.platform-type=CENTER",
        "secretpad.node-id=test",
        "secretpad.tee=true"
})
public class ResourceInitTest extends ControllerTest {
    @MockBean
    private KusciaDomainRpcImpl kusciaDomainRpc;
    @MockBean
    private DatatableService datatableService;

    @Test
    void init() {
        DomainOuterClass.QueryDomainResponse response = DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(1).build()).build();
        DomainOuterClass.CreateDomainResponse createDomainResponse = DomainOuterClass.CreateDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build();
        Mockito.doNothing().when(datatableService).pushDatatableToTeeNode(Mockito.any());
        Mockito.when(kusciaDomainRpc.queryDomain(Mockito.any())).thenReturn(response);
        Mockito.when(kusciaDomainRpc.queryDomainNoCheck(Mockito.any())).thenReturn(DomainOuterClass.QueryDomainResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build());
        Mockito.when(kusciaDomainRpc.createDomain(Mockito.any())).thenReturn(createDomainResponse);
    }
}