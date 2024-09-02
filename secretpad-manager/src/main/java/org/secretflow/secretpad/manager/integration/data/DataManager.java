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

import org.secretflow.secretpad.common.constant.DomainDataConstants;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.DataErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.model.DatatableSchema;

import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
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
    private final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    @Value("${secretpad.platform-type}")
    private String plaformType;

    public DataManager(KusciaGrpcClientAdapter kusciaGrpcClientAdapter) {
        this.kusciaGrpcClientAdapter = kusciaGrpcClientAdapter;
    }

    @Override
    public String createData(String domainId, String name, String realName, String tableName, String description,
                             String datasourceType, String datasourceName, List<DatatableSchema> datatableSchemaList, List<String> nullStrs) {
        if (realName == null) {
            realName = name;
        }
        String domainDataId = genDomainDataId();
        LOGGER.info("start create domainData, description = {}", description);
        description = description == null ? "" : description;
        String nullstrJson = CollectionUtils.isEmpty(nullStrs) ? JsonUtils.toJSONString(new ArrayList<>()) : JsonUtils.toJSONString(nullStrs);
        Domaindata.CreateDomainDataRequest createDomainDataRequest =
                Domaindata.CreateDomainDataRequest.newBuilder()
                        .setDomaindataId(domainDataId)
                        .putAttributes("DatasourceType", datasourceType)
                        .putAttributes("DatasourceName", datasourceName)
                        .putAttributes(DomainDataConstants.NULL_STRS, nullstrJson)
                        .setDomainId(domainId).setName(tableName).setType("table")
                        .setRelativeUri(realName).putAttributes("description", description)
                        .addAllColumns(datatableSchemaList
                                .stream().map(it -> Common.DataColumn.newBuilder().setName(it.getFeatureName())
                                        .setType(it.getFeatureType()).setComment(it.getFeatureDescription()).build())
                                .collect(Collectors.toList()))
                        .build();
        Domaindata.CreateDomainDataResponse domainData = this.kusciaGrpcClientAdapter.createDomainData(createDomainDataRequest);
        LOGGER.info("createData finish create domainData, description = {}",
                createDomainDataRequest.getAttributesMap().get("description"));
        return domainData.getData().getDomaindataId();
    }

    @Override
    public String createDataByDataSource(String domainId, String name, String tablePath, String datasourceId,
                                         String description, String datasourceType, String datasourceName, List<String> nullStrs, List<DatatableSchema> datatableSchemaList) {
        String domainDataId = genDomainDataId();
        LOGGER.info("starter create domainData, description = {}", description);
        description = description == null ? "" : description;
        String nullstrJson = CollectionUtils.isEmpty(nullStrs) ? JsonUtils.toJSONString(new ArrayList<>()) : JsonUtils.toJSONString(nullStrs);
        Domaindata.CreateDomainDataRequest createDomainDataRequest =
                Domaindata.CreateDomainDataRequest.newBuilder()
                        .setDomaindataId(domainDataId)
                        .setDomainId(domainId).setName(name).setType("table")
                        .putAttributes("DatasourceType", datasourceType)
                        .putAttributes("DatasourceName", datasourceName)
                        .putAttributes(DomainDataConstants.NULL_STRS, nullstrJson)
                        .setDatasourceId(datasourceId).setRelativeUri(tablePath).putAttributes("description", description)
                        .addAllColumns(datatableSchemaList
                                .stream().map(it -> Common.DataColumn.newBuilder().setName(it.getFeatureName())
                                        .setType(it.getFeatureType()).setComment(it.getFeatureDescription()).build())
                                .collect(Collectors.toList()))
                        .build();
        Domaindata.CreateDomainDataResponse domainData = this.kusciaGrpcClientAdapter.createDomainData(createDomainDataRequest);
        if (domainData.getStatus().getCode() != 0) {
            LOGGER.error("createDataByDataSource error {}", domainData.getStatus().getMessage());
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR);
        }
        LOGGER.info("finish create domainData, description = {}",
                createDomainDataRequest.getAttributesMap().get("description"));
        return domainData.getData().getDomaindataId();
    }

    @Override
    public String createDatatable(String domainDataId, String domainId, String name, String tablePath, String datasourceId,
                                  String description, String datasourceType, String datasourceName, List<String> nullStrs, List<DatatableSchema> datatableSchemaList
            , Common.Partition partition
    ) {
        LOGGER.info("starter create domainData, description = {}", description);
        description = description == null ? "" : description;
        String nullstrJson = CollectionUtils.isEmpty(nullStrs) ? JsonUtils.toJSONString(new ArrayList<>()) : JsonUtils.toJSONString(nullStrs);

        Domaindata.CreateDomainDataRequest createDomainDataRequest =
                Domaindata.CreateDomainDataRequest.newBuilder()
                        .setDomaindataId(domainDataId)
                        .setDomainId(domainId).setName(name).setType("table")
                        .setFileFormat(Common.FileFormat.CSV)
                        .putAttributes("DatasourceType", datasourceType)
                        .putAttributes("DatasourceName", datasourceName)
                        .putAttributes(DomainDataConstants.NULL_STRS, nullstrJson)
                        .setDatasourceId(datasourceId).setRelativeUri(tablePath).putAttributes("description", description)
                        .addAllColumns(datatableSchemaList
                                .stream().map(it -> Common.DataColumn.newBuilder().setName(it.getFeatureName())
                                        .setType(it.getFeatureType()).setComment(it.getFeatureDescription()).build())
                                .collect(Collectors.toList()))
                        .build();
        if (partition != null) {
            createDomainDataRequest = createDomainDataRequest.toBuilder().setPartition(partition).build();
        }
        Domaindata.CreateDomainDataResponse domainData;
        if (PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(plaformType))) {
            domainData = this.kusciaGrpcClientAdapter.createDomainData(createDomainDataRequest, domainId);
        } else {
            domainData = this.kusciaGrpcClientAdapter.createDomainData(createDomainDataRequest);
        }

        if (domainData.getStatus().getCode() != 0) {
            LOGGER.error("createDataByDataSource error {}", domainData.getStatus().getMessage());
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR);
        }
        LOGGER.info("finish create domainData, description = {}",
                createDomainDataRequest.getAttributesMap().get("description"));
        return domainData.getData().getDomaindataId();
    }

    @Override
    public Domaindata.DomainData queryDomainData(String domainId, String domainDataId, String targetNode) {
        Domaindata.QueryDomainDataResponse queryDomainDataResponse;
        if (PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(plaformType))) {
            queryDomainDataResponse = kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest
                    .newBuilder().setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                            .setDomainId(targetNode)
                            .setDomaindataId(domainDataId))
                    .build(), targetNode);
        } else {
            queryDomainDataResponse = kusciaGrpcClientAdapter.queryDomainData(Domaindata.QueryDomainDataRequest
                    .newBuilder()
                    .setData(Domaindata.QueryDomainDataRequestData.newBuilder()
                            .setDomainId(domainId)
                            .setDomaindataId(domainDataId))
                    .build());
        }
        if (queryDomainDataResponse.getStatus().getCode() != 0) {
            LOGGER.error("queryDomainData error {}", queryDomainDataResponse.getStatus().getMessage());
            throw SecretpadException.of(DataErrorCode.ILLEGAL_PARAMS_ERROR);
        }
        Domaindata.DomainData data = queryDomainDataResponse.getData();
        LOGGER.info("finish queryDomainData {}", data);
        return data;
    }

    @Override
    public String download(String uri) {
        return null;
    }

    private String genDomainDataId() {
        return UUIDUtils.random(8);
    }

}
