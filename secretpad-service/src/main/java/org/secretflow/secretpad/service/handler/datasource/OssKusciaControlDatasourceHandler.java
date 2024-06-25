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

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.errorcode.KusciaGrpcErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainDatasourceRpc;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.OssService;
import org.secretflow.secretpad.service.decorator.awsoss.AwsOssConfig;
import org.secretflow.secretpad.service.embedded.EmbeddedChannelService;
import org.secretflow.secretpad.service.model.datasource.*;
import org.secretflow.secretpad.service.model.datatable.DatatableVO;
import org.secretflow.secretpad.service.util.HttpUtils;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Component
@Slf4j
@AllArgsConstructor
public class OssKusciaControlDatasourceHandler extends AbstractDatasourceHandler {

    private final KusciaDomainDatasourceRpc kusciaDomainDatasourceRpc;

    private final EmbeddedChannelService embeddedChannelService;

    private final EnvService envService;

    private final DatatableService datatableService;

    private final OssService ossService;


    @Override
    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.OSS);
    }

    @Override
    public List<DatasourceListInfo> listDatasource(DatasourceListRequest datasourceListRequest) {
        List<Domaindatasource.DomainDataSource> datasourceListInKuscia = new ArrayList<>();
        Domaindatasource.ListDomainDataSourceRequest listDomainDataSourceRequest = Domaindatasource.ListDomainDataSourceRequest.newBuilder().setDomainId(datasourceListRequest.getNodeId()).build();
        if (envService.isCenter() && envService.isEmbeddedNode(datasourceListRequest.getNodeId())) {
            Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = embeddedChannelService.getDatasourceServiceBlockingStub(datasourceListRequest.getNodeId()).listDomainDataSource(listDomainDataSourceRequest);
            Domaindatasource.DomainDataSourceList data = listDomainDataSourceResponse.getData();
            List<Domaindatasource.DomainDataSource> datasourceListList = data.getDatasourceListList();
            datasourceListInKuscia.addAll(datasourceListList);
        } else {
            Domaindatasource.ListDomainDataSourceResponse listDomainDataSourceResponse = kusciaDomainDatasourceRpc.listDomainDataSource(listDomainDataSourceRequest);
            Domaindatasource.DomainDataSourceList data = listDomainDataSourceResponse.getData();
            List<Domaindatasource.DomainDataSource> datasourceListList = data.getDatasourceListList();
            datasourceListInKuscia.addAll(datasourceListList);
        }
        return datasourceListInKuscia.stream().map(DatasourceListInfo::from).collect(Collectors.toList());
    }

    @Override
    public CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        OssDatasourceInfo info = (OssDatasourceInfo) createDatasourceRequest.getDataSourceInfo();
        serviceCheck(info.getEndpoint());
        AwsOssConfig awsOssConfig = AwsOssConfig.builder()
                .accessKeyId(info.getAk())
                .secretAccessKey(info.getSk())
                .endpoint(info.getEndpoint())
                .build();
        ossService.checkBucketExists(awsOssConfig, info.getBucket());
        return new CreateDatasourceVO(createDatasourceInKuscia(createDatasourceRequest, info));

    }

    @Override
    public DatasourceDetailVO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        return findDatasourceInKuscia(datasourceDetailRequest);
    }

    @Override
    public void deleteDatasource(DeleteDatasourceRequest deleteDatasourceRequest) {
        List<DatatableVO> datatableVOS = datatableService.findDatatableByNodeId(deleteDatasourceRequest.getNodeId());
        if (!CollectionUtils.isEmpty(datatableVOS) &&
                !datatableVOS.stream().filter(x -> StringUtils.equals(x.getDatasourceId(),
                        deleteDatasourceRequest.getDatasourceId())).findAny().isEmpty()) {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_DELETE_FAIL, "has related data table");
        }
        Domaindatasource.DeleteDomainDataSourceRequest deleteDomainDataSourceRequest = Domaindatasource.DeleteDomainDataSourceRequest.newBuilder()
                .setDatasourceId(deleteDatasourceRequest.getDatasourceId())
                .setDomainId(deleteDatasourceRequest.getNodeId())
                .build();
        if (envService.isCenter() && envService.isEmbeddedNode(deleteDatasourceRequest.getNodeId())) {
            embeddedChannelService.getDatasourceServiceBlockingStub(deleteDatasourceRequest.getNodeId())
                    .deleteDomainDataSource(deleteDomainDataSourceRequest);
        } else {
            kusciaDomainDatasourceRpc.deleteDomainDataSource(deleteDomainDataSourceRequest);
        }
    }

    private void serviceCheck(String endpoint) {
        if (!HttpUtils.detection(endpoint)) {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_ENDPOINT_CONNECT_FAIL);
        }
    }

    private String createDatasourceInKuscia(CreateDatasourceRequest createDatasourceRequest, OssDatasourceInfo info) {
        log.info("start create datasource in kuscia");
        Domaindatasource.OssDataSourceInfo.Builder builder = Domaindatasource.OssDataSourceInfo.newBuilder()
                .setEndpoint(info.getEndpoint())
                .setBucket(info.getBucket())
                .setAccessKeyId(info.getAk())
                .setAccessKeySecret(info.getSk());
        if (StringUtils.isNotBlank(info.getPrefix())) {
            builder.setPrefix(info.getPrefix());
        }
        if (Objects.nonNull(info.getVirtualhost())) {
            builder.setVirtualhost(info.getVirtualhost());
        }
        Domaindatasource.CreateDomainDataSourceRequest createDomainDataSourceRequest = Domaindatasource.CreateDomainDataSourceRequest.newBuilder()
                .setDomainId(createDatasourceRequest.getNodeId())
                .setType(DataSourceTypeEnum.OSS.name().toLowerCase(Locale.ROOT))
                .setName(createDatasourceRequest.getName())
                .setAccessDirectly(true)
                .setInfo(Domaindatasource.DataSourceInfo.newBuilder()
                        .setOss(builder.build())).build();
        if (envService.isCenter() && envService.isEmbeddedNode(createDatasourceRequest.getNodeId())) {
            Domaindatasource.CreateDomainDataSourceResponse domainDataSource = embeddedChannelService.getDatasourceServiceBlockingStub(createDatasourceRequest.getNodeId()).createDomainDataSource(createDomainDataSourceRequest);
            if (domainDataSource.getStatus().getCode() != 0) {
                throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, domainDataSource.getStatus().getMessage());
            }
            return domainDataSource.getData().getDatasourceId();
        }
        Domaindatasource.CreateDomainDataSourceResponse domainDataSource = kusciaDomainDatasourceRpc.createDomainDataSource(createDomainDataSourceRequest);
        return domainDataSource.getData().getDatasourceId();
    }

    private DatasourceDetailVO findDatasourceInKuscia(DatasourceDetailRequest datasourceDetailRequest) {
        Domaindatasource.QueryDomainDataSourceRequest queryDomainDataSourceRequest = Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
                .setDatasourceId(datasourceDetailRequest.getDatasourceId())
                .setDomainId(datasourceDetailRequest.getNodeId())
                .build();
        if (envService.isCenter() && envService.isEmbeddedNode(datasourceDetailRequest.getNodeId())) {
            Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = embeddedChannelService.getDatasourceServiceBlockingStub(datasourceDetailRequest.getNodeId()).queryDomainDataSource(queryDomainDataSourceRequest);
            if (queryDomainDataSourceResponse.getStatus().getCode() != 0) {
                throw SecretpadException.of(KusciaGrpcErrorCode.RPC_ERROR, queryDomainDataSourceResponse.getStatus().getMessage());
            }
            return DatasourceDetailVO.from(queryDomainDataSourceResponse);
        }
        Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse = kusciaDomainDatasourceRpc.queryDomainDataSource(queryDomainDataSourceRequest);
        return DatasourceDetailVO.from(queryDomainDataSourceResponse);
    }
}
