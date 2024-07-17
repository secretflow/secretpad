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

import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author yutu
 * @date 2024/06/17
 */
public interface DomainDataSourceService {

    Domaindatasource.CreateDomainDataSourceResponse createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request);

    Domaindatasource.UpdateDomainDataSourceResponse updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request);

    Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request);

    Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request);

    Domaindatasource.BatchQueryDomainDataSourceResponse batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request);

    Domaindatasource.ListDomainDataSourceResponse listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request);

    Domaindatasource.CreateDomainDataSourceResponse createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request, String domainId);

    Domaindatasource.UpdateDomainDataSourceResponse updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request, String domainId);

    Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request, String domainId);

    Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request, String domainId);

    Domaindatasource.BatchQueryDomainDataSourceResponse batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request, String domainId);

    Domaindatasource.ListDomainDataSourceResponse listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request, String domainId);


}