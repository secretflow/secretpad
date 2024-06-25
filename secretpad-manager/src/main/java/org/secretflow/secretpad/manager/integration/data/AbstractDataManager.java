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

package org.secretflow.secretpad.manager.integration.data;

import org.secretflow.secretpad.manager.integration.model.DatatableSchema;

import org.secretflow.v1alpha1.kusciaapi.Domaindata;

import java.util.List;

/**
 * @author xiaonan
 * @date 2023/5/23
 */
public abstract class AbstractDataManager {

    /**
     * Create uploaded data
     *
     * @param domainId            nodeId
     * @param name                file name
     * @param realName            the name of the file that is actually stored
     * @param tableName           table name provided by the user
     * @param description         table description
     * @param datatableSchemaList table schema
     * @return domain data id
     */
    public abstract String createData(
            String domainId,
            String name,
            String realName,
            String tableName,
            String description,
            String datasourceType,
            String datasourceName,
            List<DatatableSchema> datatableSchemaList
    );

    /**
     * Create data
     *
     * @param domainId
     * @param name
     * @param tablePath
     * @param description
     * @param datasourceType
     * @param datasourceName
     * @param datasourceId
     * @param datatableSchemaList
     * @return domain data id
     */
    public abstract String createDataByDataSource(
            String domainId,
            String name,
            String tablePath,
            String datasourceId,
            String description,
            String datasourceType,
            String datasourceName,
            List<DatatableSchema> datatableSchemaList
    );

    public abstract Domaindata.DomainData queryDomainData(String domainId, String domainDataId);


    /**
     * Download from uri
     *
     * @param uri download address
     * @return download string
     */
    public abstract String download(String uri);

}
