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

package org.secretflow.secretpad.service.model.datasource;

import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.util.DesensitizationUtils;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Getter
@Setter
public class DatasourceDetailUnAggregateDTO {

    private String nodeId;

    private String datasourceId;

    private String name;

    private String type;

    private String status = Constants.STATUS_AVAILABLE;

    private DataSourceInfo info;

    public static DatasourceDetailUnAggregateDTO from(Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse) {
        DatasourceDetailUnAggregateDTO datasourceDetailUnAggregateDTO = new DatasourceDetailUnAggregateDTO();
        Domaindatasource.DomainDataSource data = queryDomainDataSourceResponse.getData();
        datasourceDetailUnAggregateDTO.setDatasourceId(data.getDatasourceId());
        datasourceDetailUnAggregateDTO.setType(StringUtils.toRootUpperCase(data.getType()));
        if (StringUtils.isNotBlank(data.getStatus())) {
            datasourceDetailUnAggregateDTO.setStatus(data.getStatus());
        }
        datasourceDetailUnAggregateDTO.setName(data.getName());
        datasourceDetailUnAggregateDTO.setNodeId(data.getDomainId());

        Domaindatasource.DataSourceInfo info = data.getInfo();
        switch (StringUtils.toRootUpperCase(data.getType())) {
            case DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE:
                Domaindatasource.OssDataSourceInfo oss = info.getOss();
                datasourceDetailUnAggregateDTO.setInfo(OssDatasourceInfo.create(oss.getEndpoint(), oss.getBucket(), oss.getPrefix(), DesensitizationUtils.akSkDesensitize(oss.getAccessKeyId()), DesensitizationUtils.akSkDesensitize(oss.getAccessKeySecret()), oss.getStorageType(), oss.getVirtualhost()));
                break;
            case DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE:
                Domaindatasource.OdpsDataSourceInfo odps = info.getOdps();
                datasourceDetailUnAggregateDTO.setInfo(OdpsDatasourceInfo.builder().accessId(DesensitizationUtils.akSkDesensitize(odps.getAccessKeyId())).accessKey(DesensitizationUtils.akSkDesensitize(odps.getAccessKeySecret())).endpoint(odps.getEndpoint()).project(odps.getProject()).build());
                break;
            default:
                break;
        }
        return datasourceDetailUnAggregateDTO;
    }
}
