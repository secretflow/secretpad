package org.secretflow.secretpad.service.handler.datatable;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDataConstants;
import org.secretflow.secretpad.common.dto.KusciaResponse;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.util.AsyncTaskExecutionUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datasource.tdsql.TdsqlConfig;
import org.secretflow.secretpad.manager.integration.datasource.tdsql.TdsqlManager;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.model.DatasourceDTO;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectDatatableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.TeeNodeDatatableManagementRepository;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.graph.converter.KusciaTeeDataManagerConverter;
import org.secretflow.secretpad.service.model.datatable.CreateDatatableRequest;
import org.secretflow.secretpad.service.model.datatable.CreateDatatableVO;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TdsqlKusciaControlDatatableHandler extends AbstractDatatableHandler {

    private final TdsqlManager tdsqlManager;

    protected TdsqlKusciaControlDatatableHandler(ProjectRepository projectRepository, ProjectDatatableRepository projectDatatableRepository,
                                                 KusciaTeeDataManagerConverter teeJobConverter, TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository,
                                                 KusciaGrpcClientAdapter kusciaGrpcClientAdapter, AbstractJobManager jobManager, AbstractDatatableManager datatableManager,
                                                 NodeRepository nodeRepository, TdsqlManager tdsqlManager) {
        super(projectRepository, projectDatatableRepository, teeJobConverter, teeNodeDatatableManagementRepository, kusciaGrpcClientAdapter, jobManager, datatableManager, nodeRepository);
        this.tdsqlManager = tdsqlManager;
    }

    @Resource
    @Setter
    private EnvService envService;

    @Override
    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.TDSQL);
    }

    @Override
    public CreateDatatableVO createDatatable(CreateDatatableRequest createDatatableRequest) {
        Map<String, String> failedDatatable = new HashMap<>();
        List<CreateDatatableVO.DataTableNodeInfo> dataTableNodeInfos = new ArrayList<>();
        List<CompletableFuture<KusciaResponse<Domaindata.CreateDomainDataResponse>>> completableFutures = createDatatableRequest.getNodeIds().stream()
                .map(nodeId -> createAndExecuteFuture(nodeId, createDatatableRequest, failedDatatable))
                .collect(Collectors.toList());

        fetchResult(failedDatatable, completableFutures, dataTableNodeInfos);

        logFailedDatatable(failedDatatable);

        return new CreateDatatableVO(dataTableNodeInfos, failedDatatable);
    }

    @Override
    protected void queryDatatable(DatatableDTO datatableDTO) {
        Domaindatasource.QueryDomainDataSourceResponse response = kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder().setDomainId(datatableDTO.getNodeId()).setDatasourceId(datatableDTO.getDatasourceId()).build(), datatableDTO.getNodeId());
        DatasourceDTO datasourceDTO = DatasourceDTO.fromDatabaseDomainDatasource(response.getData());
        DatasourceDTO.DatabaseDataSourceInfo databaseDataSourceInfo = datasourceDTO.getDatabaseDataSourceInfo();
        if (!checkTableExists(databaseDataSourceInfo, datatableDTO.getRelativeUri())) {
            datatableDTO.setStatus(Constants.STATUS_UNAVAILABLE);
        }
    }

    protected boolean checkTableExists(DatasourceDTO.DatabaseDataSourceInfo databaseDataSourceInfo, String tableName) {
        TdsqlConfig tdsqlConfig = TdsqlConfig.builder()
                .endpoint(databaseDataSourceInfo.getEndpoint())
                .database(databaseDataSourceInfo.getDatabase())
                .user(databaseDataSourceInfo.getUser())
                .password(databaseDataSourceInfo.getPassword())
                .build();
        return tdsqlManager.testTableExists(tdsqlConfig, databaseDataSourceInfo.getDatabase(), tableName);
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
