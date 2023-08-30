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

import org.secretflow.secretpad.common.errorcode.DataErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.manager.integration.model.DatatableSchema;

import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager data operation
 *
 * @author xiaonan
 * @date 2023/5/23
 */
public class DataManager extends AbstractDataManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    /**
     * Domain data service blocking stub
     */
    private final DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub;

    public DataManager(DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub) {
        this.dataStub = dataStub;
    }

    @Override
    public String createData(String domainId, String name, String realName, String tableName, String description,
                             List<DatatableSchema> datatableSchemaList) {
        if (realName == null) {
            realName = name;
        }
        String domainDataId = genDomainDataId();
        LOGGER.info("start create domainData, description = {}", description);
        description = description == null ? "" : description;
        Domaindata.CreateDomainDataRequest createDomainDataRequest =
                Domaindata.CreateDomainDataRequest.newBuilder()
                        .setDomaindataId(domainDataId)
                        .setDomainId(domainId).setName(tableName).setType("table")
                        .setRelativeUri(realName).putAttributes("description", description)
                        .addAllColumns(datatableSchemaList
                                .stream().map(it -> Common.DataColumn.newBuilder().setName(it.getFeatureName())
                                        .setType(it.getFeatureType()).setComment(it.getFeatureDescription()).build())
                                .collect(Collectors.toList()))
                        .build();
        Domaindata.CreateDomainDataResponse domainData = this.dataStub.createDomainData(createDomainDataRequest);
        LOGGER.info("finish create domainData, description = {}",
                createDomainDataRequest.getAttributesMap().get("description"));
        return domainData.getData().getDomaindataId();
    }

    @Override
    public String createDataByDataSource(String domainId, String name, String tablePath, String datasourceId,
                                         String description, List<DatatableSchema> datatableSchemaList) {
        String domainDataId = genDomainDataId();
        LOGGER.info("starter create domainData, description = {}", description);
        description = description == null ? "" : description;
        Domaindata.CreateDomainDataRequest createDomainDataRequest =
                Domaindata.CreateDomainDataRequest.newBuilder()
                        .setDomaindataId(domainDataId)
                        .setDomainId(domainId).setName(name).setType("table")
                        .setDatasourceId(datasourceId).setRelativeUri(tablePath).putAttributes("description", description)
                        .addAllColumns(datatableSchemaList
                                .stream().map(it -> Common.DataColumn.newBuilder().setName(it.getFeatureName())
                                        .setType(it.getFeatureType()).setComment(it.getFeatureDescription()).build())
                                .collect(Collectors.toList()))
                        .build();
        Domaindata.CreateDomainDataResponse domainData = this.dataStub.createDomainData(createDomainDataRequest);
        if (domainData.getStatus().getCode() != 0) {
            LOGGER.error("createDataByDataSource error {}", domainData.getStatus().getMessage());
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR);
        }
        LOGGER.info("finish create domainData, description = {}",
                createDomainDataRequest.getAttributesMap().get("description"));
        return domainData.getData().getDomaindataId();
    }

    @Override
    public String download(String uri) {
        return null;
    }

    private String genDomainDataId() {
        return UUIDUtils.random(8);
    }

}
