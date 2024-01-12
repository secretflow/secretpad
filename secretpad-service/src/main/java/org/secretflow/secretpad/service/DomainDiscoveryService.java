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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.service.model.node.NodeRouteVO;

import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * DomainDiscoveryService
 *
 * @author yutu
 * @date 2023/12/05
 */
public interface DomainDiscoveryService extends Ordered {

    AtomicReference<Collection<NodeRouteVO>> localRegionApps = new AtomicReference<>();

    /**
     * Default order
     */
    int DEFAULT_ORDER = 0;

    /**
     * @return All known service IDs.
     */
    List<DomainRoute> getServices();

    default void probe() {
        getServices();
    }

    @Override
    default int getOrder() {
        return DEFAULT_ORDER;
    }
}