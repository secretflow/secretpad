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

package org.secretflow.secretpad.kuscia.v1alpha1.service;

import org.secretflow.v1alpha1.kusciaapi.DomainRoute;

/**
 * @author yutu
 * @date 2024/06/17
 */
public interface DomainRouteService {

    DomainRoute.CreateDomainRouteResponse createDomainRoute(DomainRoute.CreateDomainRouteRequest request);

    DomainRoute.DeleteDomainRouteResponse deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest request);

    DomainRoute.QueryDomainRouteResponse queryDomainRoute(DomainRoute.QueryDomainRouteRequest request);

    DomainRoute.BatchQueryDomainRouteStatusResponse batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest request);

    DomainRoute.CreateDomainRouteResponse createDomainRoute(DomainRoute.CreateDomainRouteRequest request, String domainId);

    DomainRoute.DeleteDomainRouteResponse deleteDomainRoute(DomainRoute.DeleteDomainRouteRequest request, String domainId);

    DomainRoute.QueryDomainRouteResponse queryDomainRoute(DomainRoute.QueryDomainRouteRequest request, String domainId);

    DomainRoute.BatchQueryDomainRouteStatusResponse batchQueryDomainRouteStatus(DomainRoute.BatchQueryDomainRouteStatusRequest request, String domainId);

}
