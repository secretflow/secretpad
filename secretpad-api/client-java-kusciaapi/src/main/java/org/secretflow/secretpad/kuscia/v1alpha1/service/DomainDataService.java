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

import org.secretflow.v1alpha1.kusciaapi.Domaindata;

/**
 * @author yutu
 * @date 2024/06/17
 */
public interface DomainDataService {

    Domaindata.CreateDomainDataResponse createDomainData(Domaindata.CreateDomainDataRequest request);

    Domaindata.UpdateDomainDataResponse updateDomainData(Domaindata.UpdateDomainDataRequest request);

    Domaindata.DeleteDomainDataResponse deleteDomainData(Domaindata.DeleteDomainDataRequest request);

    Domaindata.QueryDomainDataResponse queryDomainData(Domaindata.QueryDomainDataRequest request);

    Domaindata.BatchQueryDomainDataResponse batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest request);

    Domaindata.ListDomainDataResponse listDomainData(Domaindata.ListDomainDataRequest request);


    Domaindata.CreateDomainDataResponse createDomainData(Domaindata.CreateDomainDataRequest request, String domainId);

    Domaindata.UpdateDomainDataResponse updateDomainData(Domaindata.UpdateDomainDataRequest request, String domainId);

    Domaindata.DeleteDomainDataResponse deleteDomainData(Domaindata.DeleteDomainDataRequest request, String domainId);

    Domaindata.QueryDomainDataResponse queryDomainData(Domaindata.QueryDomainDataRequest request, String domainId);

    Domaindata.BatchQueryDomainDataResponse batchQueryDomainData(Domaindata.BatchQueryDomainDataRequest request, String domainId);

    Domaindata.ListDomainDataResponse listDomainData(Domaindata.ListDomainDataRequest request, String domainId);

}
