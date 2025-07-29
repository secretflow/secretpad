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

package org.secretflow.secretpad.service.handler.datatable;

import org.secretflow.secretpad.common.constant.DomainDataConstants;
import org.secretflow.secretpad.common.dto.KusciaResponse;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.AsyncTaskExecutionUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectDatatableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.TeeNodeDatatableManagementRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.graph.converter.KusciaTeeDataManagerConverter;
import org.secretflow.secretpad.service.model.datatable.CreateDatatableRequest;
import org.secretflow.secretpad.service.model.datatable.CreateDatatableVO;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author lufeng
 * @date 2024/8/19
 */
@Component
@Slf4j
public class LocalKusciaControlDatatableHandler extends AbstractDatatableHandler {
    private final EnvService envService;

    protected LocalKusciaControlDatatableHandler(ProjectRepository projectRepository, ProjectDatatableRepository projectDatatableRepository,
                                                 KusciaTeeDataManagerConverter teeJobConverter, TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository,
                                                 KusciaGrpcClientAdapter kusciaGrpcClientAdapter, AbstractJobManager jobManager, AbstractDatatableManager datatableManager, NodeRepository nodeRepository, EnvService envService) {
        super(projectRepository, projectDatatableRepository, teeJobConverter, teeNodeDatatableManagementRepository, kusciaGrpcClientAdapter, jobManager, datatableManager, nodeRepository);
        this.envService = envService;
    }


    @Override
    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.LOCAL);
    }

    @Override
    public CreateDatatableVO createDatatable(CreateDatatableRequest createDatatableRequest) {
        validateNodeIds(createDatatableRequest.getNodeIds());
        Map<String, String> failedDatatable = new HashMap<>();
        List<CreateDatatableVO.DataTableNodeInfo> dataTableNodeInfos = new ArrayList<>();
        List<CompletableFuture<KusciaResponse<Domaindata.CreateDomainDataResponse>>> completableFutures = createDatatableRequest.getNodeIds().stream()
                .map(nodeId -> createAndExecuteFuture(nodeId, createDatatableRequest, failedDatatable))
                .collect(Collectors.toList());

        fetchResult(failedDatatable, completableFutures, dataTableNodeInfos);

        logFailedDatatable(failedDatatable, createDatatableRequest.getNodeIds().size());
        return new CreateDatatableVO(dataTableNodeInfos, failedDatatable);
    }

    private void validateNodeIds(List<String> nodeIds) {
        if (!envService.isCurrentNodeEnvironment(nodeIds.get(0)) && envService.isAutonomy()) {
            log.error("The nodeId is not the platform node id.");
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
    }

    private CompletableFuture<KusciaResponse<Domaindata.CreateDomainDataResponse>> createAndExecuteFuture(String nodeId, CreateDatatableRequest request, Map<String, String> failedDatatable) {
        Domaindata.CreateDomainDataRequest createDomainDataRequest = buildCreateDomainDataRequest(nodeId, request);
        return AsyncTaskExecutionUtils.executeDecoratedOperation(createDomainDataRequest, this::createDomainData, nodeId, failedDatatable);
    }

    private Domaindata.CreateDomainDataRequest buildCreateDomainDataRequest(String nodeId, CreateDatatableRequest request) {
        String domainDataId = genDomainDataId();
        List<Common.DataColumn> columns = request.getColumns().stream()
                .map(column -> Common.DataColumn.newBuilder()
                        .setName(column.getColName())
                        .setType(column.getColType())
                        .setComment(StringUtils.isNotEmpty(column.getColComment()) ? column.getColComment() : "")
                        .build())
                .collect(Collectors.toList());

        return Domaindata.CreateDomainDataRequest.newBuilder()
                .setDomaindataId(domainDataId)
                .setDomainId(nodeId)
                .setName(request.getDatatableName())
                .setType("table")
                .setFileFormat(Common.FileFormat.CSV)
                .putAttributes("DatasourceType", request.getDatasourceType())
                .putAttributes("DatasourceName", request.getDatasourceName())
                .putAttributes(DomainDataConstants.NULL_STRS, CollectionUtils.isEmpty(request.getNullStrs()) ? JsonUtils.toJSONString(new ArrayList<>()) : JsonUtils.toJSONString(request.getNullStrs()))
                .setDatasourceId(request.getDatasourceId())
                .setRelativeUri(request.getRelativeUri())
                .putAttributes("description", request.getDesc() == null ? "" : request.getDesc())
                .addAllColumns(columns)
                .build();
    }

    private KusciaResponse<Domaindata.CreateDomainDataResponse> createDomainData(Domaindata.CreateDomainDataRequest request) {
        String domainId = envService.isCenter() ? envService.getPlatformNodeId() : request.getDomainId();
        Domaindata.CreateDomainDataResponse domainData = kusciaGrpcClientAdapter.createDomainData(request, domainId);
        return KusciaResponse.of(domainData, request.getDomainId());
    }

}
