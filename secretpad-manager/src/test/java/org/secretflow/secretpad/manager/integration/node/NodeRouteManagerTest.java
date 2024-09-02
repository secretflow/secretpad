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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.noderoute.NodeRouteManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author lufeng
 * @date 2024/8/7
 */
@ExtendWith(MockitoExtension.class)
public class NodeRouteManagerTest {
    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @InjectMocks
    private NodeRouteManager nodeRouteManager;


    /**
     * test deleteNodeRouteInKuscia
     */

    @Test
    public void testDeleteNodeRouteInKuscia_NormalCase() {
        when(kusciaGrpcClientAdapter.deleteDomainRoute(any(), any())).thenReturn(DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0)).build());

        nodeRouteManager.deleteNodeRouteInKuscia("sourceNodeId", "dstNodeId", "channelNodeId");

        verify(kusciaGrpcClientAdapter, times(1)).deleteDomainRoute(any(), any());
    }
    /**
     * test deleteNodeRouteInKuscia FailureCase
     *
     */
    @Test
    public void testDeleteNodeRouteInKuscia_FailureCase() {
        when(kusciaGrpcClientAdapter.deleteDomainRoute(any(), any())).thenReturn(DomainRoute.DeleteDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(1)).build());

        try {
            nodeRouteManager.deleteNodeRouteInKuscia("sourceNodeId", "dstNodeId", "channelNodeId");
        } catch (SecretpadException e) {
            assertEquals(NodeRouteErrorCode.NODE_ROUTE_DELETE_ERROR, e.getErrorCode());
        }

        verify(kusciaGrpcClientAdapter, times(1)).deleteDomainRoute(any(), any());

    }
}
