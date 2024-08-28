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

package org.secretflow.secretpad.service.handler.datasource;

import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.dto.KusciaResponse;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.AsyncTaskExecutionUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.odps.OdpsConfig;
import org.secretflow.secretpad.manager.integration.odps.OdpsManager;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.model.datasource.*;
import org.secretflow.secretpad.service.model.datatable.DatatableVO;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author yutu
 * @date 2024/07/23
 */
@Slf4j
@Service
public class OdpsKusciaControlDatasourceHandler extends AbstractDatasourceHandler {
    @Resource
    @Setter
    private OdpsManager odpsManager;
    @Resource
    @Setter
    private EnvService envService;
    @Resource
    @Setter
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    @Resource
    @Setter
    private DatatableService datatableService;

    @Override
    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.ODPS);
    }

    @Override
    public List<DatasourceListInfoUnAggregate> listDatasource(String nodeId) {
        List<Domaindatasource.DomainDataSource> datasourceListInKuscia = new ArrayList<>();
        Domaindatasource.ListDomainDataSourceRequest listDomainDataSourceRequest = Domaindatasource.ListDomainDataSourceRequest.newBuilder().setDomainId(nodeId).build();
        if (envService.isCenter() && envService.isEmbeddedNode(nodeId)) {
            Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = kusciaGrpcClientAdapter.listDomainDataSource(listDomainDataSourceRequest, nodeId);
            Domaindatasource.DomainDataSourceList data = listDomainDataSourceResponse.getData();
            List<Domaindatasource.DomainDataSource> datasourceListList = data.getDatasourceListList();
            datasourceListInKuscia.addAll(datasourceListList);
        } else {
            Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = kusciaGrpcClientAdapter.listDomainDataSource(listDomainDataSourceRequest, nodeId);
            Domaindatasource.DomainDataSourceList data = listDomainDataSourceResponse.getData();
            List<Domaindatasource.DomainDataSource> datasourceListList = data.getDatasourceListList();
            datasourceListInKuscia.addAll(datasourceListList);
        }
        return datasourceListInKuscia.stream().map(DatasourceListInfoUnAggregate::from).collect(Collectors.toList());
    }

    @Override
    public DatasourceDetailUnAggregateDTO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        return findDatasourceInKuscia(datasourceDetailRequest);
    }

    @Override
    public CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        OdpsDatasourceInfo info = (OdpsDatasourceInfo) createDatasourceRequest.getDataSourceInfo();
        serviceCheck(info.getEndpoint());
        OdpsConfig odpsConfig = info.toOdpsConfig();
        odpsManager.testConnection(odpsConfig);
        return createDatasourceInKuscia(createDatasourceRequest, info);
    }

    @Override
    public void deleteDatasource(DeleteDatasourceRequest deleteDatasourceRequest) {
        List<DatatableVO> datatableVOS = datatableService.findDatatableByNodeId(deleteDatasourceRequest.getOwnerId());
        if (!CollectionUtils.isEmpty(datatableVOS) &&
                datatableVOS.stream().anyMatch(x -> StringUtils.equals(x.getDatasourceId(),
                        deleteDatasourceRequest.getDatasourceId()))) {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_DELETE_FAIL, "has related data table");
        }
        Domaindatasource.DeleteDomainDataSourceRequest deleteDomainDataSourceRequest = Domaindatasource.DeleteDomainDataSourceRequest.newBuilder()
                .setDatasourceId(deleteDatasourceRequest.getDatasourceId())
                .setDomainId(deleteDatasourceRequest.getOwnerId())
                .build();
        if (envService.isCenter() && envService.isEmbeddedNode(deleteDatasourceRequest.getOwnerId())) {
            kusciaGrpcClientAdapter
                    .deleteDomainDataSource(deleteDomainDataSourceRequest, deleteDatasourceRequest.getOwnerId());
        } else {
            kusciaGrpcClientAdapter.deleteDomainDataSource(deleteDomainDataSourceRequest, deleteDatasourceRequest.getOwnerId());
        }
    }

    private CreateDatasourceVO createDatasourceInKuscia(CreateDatasourceRequest createDatasourceRequest, OdpsDatasourceInfo info) {
        Domaindatasource.OdpsDataSourceInfo.Builder builder = Domaindatasource.OdpsDataSourceInfo.newBuilder()
                .setEndpoint(info.getEndpoint())
                .setAccessKeyId(info.getAccessId())
                .setAccessKeySecret(info.getAccessKey())
                .setProject(info.getProject());

        //record the failed execution node
        Map<String, String> failedDatasource = new ConcurrentHashMap<>(16);
        String datasourceId = DomainDatasourceConstants.DATASOURCE_ODPS_ID_PREFIX.concat(UUIDUtils.random(16));
        List<CompletableFuture<KusciaResponse<Domaindatasource.CreateDomainDataSourceResponse>>> completableFutures = createDatasourceRequest.getNodeIds().stream().map(nodeId -> {
            Domaindatasource.CreateDomainDataSourceRequest createDomainDataSourceRequest = Domaindatasource.CreateDomainDataSourceRequest.newBuilder()
                    .setDomainId(nodeId)
                    .setDatasourceId(datasourceId)
                    .setType(DataSourceTypeEnum.ODPS.name().toLowerCase(Locale.ROOT))
                    .setName(createDatasourceRequest.getName())
                    .setAccessDirectly(Boolean.FALSE)
                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder()
                            .setOdps(builder.build())).build();

            return AsyncTaskExecutionUtils.executeDecoratedOperation(createDomainDataSourceRequest, this::createDomainDataSource, nodeId, failedDatasource);

        }).collect(Collectors.toList());


        fetchResult(failedDatasource, completableFutures);
        if (!CollectionUtils.isEmpty(failedDatasource) && failedDatasource.size() == createDatasourceRequest.getNodeIds().size()) {
            log.error("all node create datasource failed {}", JsonUtils.toJSONString(failedDatasource));
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_CREATE_FAIL, "all node create datasource failed");
        }
        return new CreateDatasourceVO(datasourceId, failedDatasource);
    }


    private DatasourceDetailUnAggregateDTO findDatasourceInKuscia(DatasourceDetailRequest datasourceDetailRequest) {
        Domaindatasource.QueryDomainDataSourceRequest queryDomainDataSourceRequest = Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDatasourceId(datasourceDetailRequest.getDatasourceId())
                .setDomainId(datasourceDetailRequest.getOwnerId())
                .build();
        if (envService.isCenter() && envService.isEmbeddedNode(datasourceDetailRequest.getOwnerId())) {
            Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = kusciaGrpcClientAdapter.queryDomainDataSource(queryDomainDataSourceRequest, datasourceDetailRequest.getOwnerId());
            if (queryDomainDataSourceResponse.getStatus().getCode() != 0) {
                throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, queryDomainDataSourceResponse.getStatus().getMessage());
            }
            return DatasourceDetailUnAggregateDTO.from(queryDomainDataSourceResponse);
        }
        Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = kusciaGrpcClientAdapter.queryDomainDataSource(queryDomainDataSourceRequest, datasourceDetailRequest.getOwnerId());
        return DatasourceDetailUnAggregateDTO.from(queryDomainDataSourceResponse);
    }

    private KusciaResponse<Domaindatasource.CreateDomainDataSourceResponse> createDomainDataSource(Domaindatasource.CreateDomainDataSourceRequest request) {
        Domaindatasource.CreateDomainDataSourceResponse domainDataSource = kusciaGrpcClientAdapter.createDomainDataSource(request, request.getDomainId());
        return KusciaResponse.of(domainDataSource, request.getDomainId());
    }
}