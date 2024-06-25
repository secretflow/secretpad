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

package org.secretflow.secretpad.manager.kuscia.grpc;

import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
public interface KusciaDomainDatasourceRpc extends KusciaRpc {

    Domaindatasource.CreateDomainDataSourceResponse createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request);

    Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest request);

    Domaindatasource.DeleteDomainDataSourceResponse deleteDomainDataSource(Domaindatasource.DeleteDomainDataSourceRequest request);

    Domaindatasource.UpdateDomainDataSourceResponse updateDomainDataSource(Domaindatasource.UpdateDomainDataSourceRequest request);

    Domaindatasource.ListDomainDataSourceResponse listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest request);

    Domaindatasource.BatchQueryDomainDataSourceResponse batchQueryDomainDataSource(Domaindatasource.BatchQueryDomainDataSourceRequest request);
}
