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

import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.serving.AbstractKusciaServingManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author yutu
 * @date 2024/02/26
 */
@ExtendWith(MockitoExtension.class)
public class AbstractKusciaServingManagerTest {


    @Mock
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void testCreate() {
        AbstractKusciaServingManager servingManager = new AbstractKusciaServingManager() {
            @Override
            public Serving.CreateServingResponse create(Serving.CreateServingRequest request) {
                return super.create(request);
            }
        };
        servingManager.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        servingManager.setApplicationEventPublisher(applicationEventPublisher);
        Serving.CreateServingRequest request = Serving.CreateServingRequest.newBuilder()
                .setServingId("serving1")
                .build();
        Serving.CreateServingResponse response = Serving.CreateServingResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0))
                .build();
        when(kusciaGrpcClientAdapter.createServing(request)).thenReturn(response);
        Serving.CreateServingResponse result = servingManager.create(request);
        assertEquals(response, result);
        verify(applicationEventPublisher).publishEvent(any(AbstractKusciaServingManager.KusciaServingEvent.class));
    }

    @Test
    void testDelete_WithValidRequest_ReturnsResponse() {
        AbstractKusciaServingManager servingManager = new AbstractKusciaServingManager() {
            @Override
            public Serving.CreateServingResponse create(Serving.CreateServingRequest request) {
                return super.create(request);
            }
        };
        servingManager.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        servingManager.setApplicationEventPublisher(applicationEventPublisher);
        Serving.DeleteServingRequest request = Serving.DeleteServingRequest.newBuilder()
                .setServingId("serving123")
                .build();
        Serving.DeleteServingResponse response = Serving.DeleteServingResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0))
                .build();
        when(kusciaGrpcClientAdapter.deleteServing(request)).thenReturn(response);
        Serving.DeleteServingResponse result = servingManager.delete(request);
        assertEquals(response, result);
        verify(kusciaGrpcClientAdapter).deleteServing(request);
    }

    @Test
    void testDelete_WithInvalidRequest_ReturnsResponse() {
        AbstractKusciaServingManager servingManager = new AbstractKusciaServingManager() {
            @Override
            public Serving.CreateServingResponse create(Serving.CreateServingRequest request) {
                return super.create(request);
            }
        };
        servingManager.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        servingManager.setApplicationEventPublisher(applicationEventPublisher);
        Serving.DeleteServingRequest request = Serving.DeleteServingRequest.newBuilder()
                .setServingId("")
                .build();
        Serving.DeleteServingResponse response = Serving.DeleteServingResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(1))
                .build();
        when(kusciaGrpcClientAdapter.deleteServing(request)).thenReturn(response);
        Serving.DeleteServingResponse result = servingManager.delete(request);
        assertEquals(response, result);
        verify(kusciaGrpcClientAdapter).deleteServing(request);
    }

    @Test
    public void testUpdate() {
        AbstractKusciaServingManager servingManager = new AbstractKusciaServingManager() {
            @Override
            public Serving.CreateServingResponse create(Serving.CreateServingRequest request) {
                return super.create(request);
            }
        };
        servingManager.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        servingManager.setApplicationEventPublisher(applicationEventPublisher);
        Serving.UpdateServingRequest request = Serving.UpdateServingRequest.newBuilder().build();
        Serving.UpdateServingResponse expectedResponse = Serving.UpdateServingResponse.newBuilder().build();
        when(kusciaGrpcClientAdapter.updateServing(request)).thenReturn(expectedResponse);
        Serving.UpdateServingResponse actualResponse = servingManager.update(request);
        assertEquals(expectedResponse, actualResponse);
        verify(applicationEventPublisher, times(1)).publishEvent(any(AbstractKusciaServingManager.KusciaServingEvent.class));
    }

    @Test
    public void testQuery() {
        AbstractKusciaServingManager servingManager = new AbstractKusciaServingManager() {
            @Override
            public Serving.CreateServingResponse create(Serving.CreateServingRequest request) {
                return super.create(request);
            }
        };
        servingManager.setKusciaGrpcClientAdapter(kusciaGrpcClientAdapter);
        servingManager.setApplicationEventPublisher(applicationEventPublisher);
        Serving.QueryServingRequest request = Serving.QueryServingRequest.newBuilder()
                .setServingId("serving123")
                .build();
        Serving.QueryServingResponse response = Serving.QueryServingResponse.newBuilder()
                .setStatus(Common.Status.newBuilder().setCode(0))
                .build();
        when(kusciaGrpcClientAdapter.queryServing(request)).thenReturn(response);
        response = servingManager.query(request);
        Assertions.assertEquals(0, response.getStatus().getCode());
    }


}