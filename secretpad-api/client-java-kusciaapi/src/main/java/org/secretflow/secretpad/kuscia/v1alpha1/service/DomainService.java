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

import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass.*;

/**
 * @author yutu
 * @date 2024/06/13
 */
public interface DomainService {

    CreateDomainResponse createDomain(CreateDomainRequest request);

    UpdateDomainResponse updateDomain(UpdateDomainRequest request);

    DeleteDomainResponse deleteDomain(DeleteDomainRequest request);

    QueryDomainResponse queryDomain(QueryDomainRequest request);

    BatchQueryDomainResponse batchQueryDomain(BatchQueryDomainRequest request);


    CreateDomainResponse createDomain(CreateDomainRequest request, String domainId);

    UpdateDomainResponse updateDomain(UpdateDomainRequest request, String domainId);

    DeleteDomainResponse deleteDomain(DeleteDomainRequest request, String domainId);

    QueryDomainResponse queryDomain(QueryDomainRequest request, String domainId);

    BatchQueryDomainResponse batchQueryDomain(BatchQueryDomainRequest request, String domainId);
}
