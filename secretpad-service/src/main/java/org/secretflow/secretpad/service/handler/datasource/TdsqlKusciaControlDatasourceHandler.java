package org.secretflow.secretpad.service.handler.datasource;

import com.aliyun.core.utils.StringUtils;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import org.secretflow.secretpad.manager.integration.datasource.tdsql.TdsqlConfig;
import org.secretflow.secretpad.manager.integration.datasource.tdsql.TdsqlManager;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.model.datasource.*;
import org.secretflow.secretpad.service.model.datatable.DatatableVO;
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

@Slf4j
@Service
public class TdsqlKusciaControlDatasourceHandler extends AbstractDatasourceHandler {

    @Resource
    @Setter
    private TdsqlManager tdsqlManager;
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
        return Lists.newArrayList(DataSourceTypeEnum.TDSQL);
    }

    @Override
    public List<DatasourceListInfoUnAggregate> listDatasource(String nodeId) {
        List<Domaindatasource.DomainDataSource> datasourceListInKuscia = new ArrayList<>();
        Domaindatasource.ListDomainDataSourceRequest listDomainDataSourceRequest = Domaindatasource.ListDomainDataSourceRequest.newBuilder().setDomainId(nodeId).build();
        Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = kusciaGrpcClientAdapter.listDomainDataSource(listDomainDataSourceRequest, nodeId);
        Domaindatasource.DomainDataSourceList data = listDomainDataSourceResponse.getData();
        List<Domaindatasource.DomainDataSource> datasourceListList = data.getDatasourceListList();
        datasourceListInKuscia.addAll(datasourceListList);
        return datasourceListInKuscia.stream().map(DatasourceListInfoUnAggregate::from).collect(Collectors.toList());
    }

    @Override
    public DatasourceDetailUnAggregateDTO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        return findDatasourceInKuscia(datasourceDetailRequest);
    }

    @Override
    public CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        TdsqlDatasourceInfo info = (TdsqlDatasourceInfo) createDatasourceRequest.getDataSourceInfo();
        TdsqlConfig tdsqlConfig = info.toTdsqlConfig();
        tdsqlManager.testConnection(tdsqlConfig);
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


    private CreateDatasourceVO createDatasourceInKuscia(CreateDatasourceRequest createDatasourceRequest, TdsqlDatasourceInfo info) {

        Domaindatasource.TdsqlDataSourceInfo.Builder builder = Domaindatasource.TdsqlDataSourceInfo.newBuilder()
                .setEndpoint(info.getEndpoint())
                .setUser(info.getUser())
                .setPassword(info.getPassword())
                .setDatabase(info.getDatabase());
        //record the failed execution node
        Map<String, String> failedDatasource = new ConcurrentHashMap<>(16);
        String datasourceId = DomainDatasourceConstants.DATASOURCE_TDSQL_ID_PREFIX.concat(UUIDUtils.random(16));
        List<CompletableFuture<KusciaResponse<Domaindatasource.CreateDomainDataSourceResponse>>> completableFutures = createDatasourceRequest.getNodeIds().stream().map(nodeId -> {
            Domaindatasource.CreateDomainDataSourceRequest createDomainDataSourceRequest = Domaindatasource.CreateDomainDataSourceRequest.newBuilder()
                    .setDomainId(nodeId)
                    .setDatasourceId(datasourceId)
                    .setType(DataSourceTypeEnum.TDSQL.name().toLowerCase(Locale.ROOT))
                    .setName(createDatasourceRequest.getName())
                    .setAccessDirectly(Boolean.FALSE)
                    .setInfo(Domaindatasource.DataSourceInfo.newBuilder()
                            .setTdsql(builder.build())).build();

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
