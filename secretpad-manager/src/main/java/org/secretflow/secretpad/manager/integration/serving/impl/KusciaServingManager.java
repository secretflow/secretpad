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

package org.secretflow.secretpad.manager.integration.serving.impl;

import org.secretflow.secretpad.common.constant.ServingConstants;
import org.secretflow.secretpad.manager.integration.serving.AbstractKusciaServingManager;

import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.Serving;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * kuscia serving manager
 *
 * @author yutu
 * @date 2024/01/29
 */
@Slf4j
@Service
public class KusciaServingManager extends AbstractKusciaServingManager {


    @EventListener
    public void onApplicationEvent(KusciaServingEvent event) {
        String action = event.getAction();
        log.info("kuscia serving event {}", action);
        switch (action) {
            case ServingConstants.CREATE, ServingConstants.UPDATE, ServingConstants.DELETE -> {
                Serving.QueryServingResponse response =
                        query(Serving.QueryServingRequest.newBuilder().setServingId(event.getServingId()).build());
            }
            default -> throw new IllegalStateException("Unexpected value: " + action);
        }
    }
}