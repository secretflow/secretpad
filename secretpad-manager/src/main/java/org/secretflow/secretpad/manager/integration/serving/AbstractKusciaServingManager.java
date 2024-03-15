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

package org.secretflow.secretpad.manager.integration.serving;

import org.secretflow.secretpad.common.constant.ServingConstants;

import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.secretflow.v1alpha1.kusciaapi.ServingServiceGrpc;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * abstract kuscia serving manager.
 *
 * @author yutu
 * @date 2024/01/29
 */
@Slf4j
@Setter
public abstract class AbstractKusciaServingManager {
    @Resource
    private ServingServiceGrpc.ServingServiceBlockingStub servingServiceBlockingStub;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    public Serving.CreateServingResponse create(Serving.CreateServingRequest request) {
        log.debug("kuscia serving create request: {}", request);
        Serving.CreateServingResponse response = servingServiceBlockingStub.createServing(request);
        if (response.getStatus().getCode() == 0) {
            publishEvent(request.getServingId(), ServingConstants.CREATE);
        }
        log.debug("kuscia serving create response: {}", response);
        return response;
    }

    public Serving.DeleteServingResponse delete(Serving.DeleteServingRequest request) {
        log.debug("kuscia serving delete request: {}", request);
        Serving.DeleteServingResponse response = servingServiceBlockingStub.deleteServing(request);
        if (response.getStatus().getCode() == 0) {
            publishEvent(request.getServingId(), ServingConstants.DELETE);
        }
        log.debug("kuscia serving delete response: {}", response);
        return response;
    }

    public Serving.UpdateServingResponse update(Serving.UpdateServingRequest request) {
        log.debug("kuscia serving update request: {}", request);
        Serving.UpdateServingResponse response = servingServiceBlockingStub.updateServing(request);
        if (response.getStatus().getCode() == 0) {
            publishEvent(request.getServingId(), ServingConstants.UPDATE);
        }
        log.debug("kuscia serving update response: {}", response);
        return response;
    }

    public Serving.QueryServingResponse query(Serving.QueryServingRequest request) {
        log.debug("kuscia serving query request: {}", request);
        Serving.QueryServingResponse response = servingServiceBlockingStub.queryServing(request);
        log.debug("kuscia serving query response: {}", response);
        return response;
    }

    public Serving.BatchQueryServingStatusResponse batchQueryServingStatus(Serving.BatchQueryServingStatusRequest request) {
        log.debug("kuscia serving batchQueryServingStatus request: {}", request);
        Serving.BatchQueryServingStatusResponse response = servingServiceBlockingStub.batchQueryServingStatus(request);
        log.debug("kuscia serving batchQueryServingStatus response: {}", response);
        return response;
    }

    private void publishEvent(String servingId, String action) {
        applicationEventPublisher.publishEvent(new KusciaServingEvent(servingId, action));
    }

    @ToString
    @Setter
    @Getter
    public static class KusciaServingEvent extends ApplicationEvent {
        private String servingId;
        private String action;

        public KusciaServingEvent(String servingId, String action) {
            super(new Object());
            this.servingId = servingId;
            this.action = action;
        }
    }
}