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
public class DatasourceDetailVO {

    private String nodeId;

    private String datasourceId;

    private String name;

    private String type;

    private String status = Constants.STATUS_AVAILABLE;

    private OssDatasourceInfo info;

    public static DatasourceDetailVO from(Domaindatasource.QueryDomainDataSourceResponse queryDomainDataSourceResponse) {
        DatasourceDetailVO datasourceDetailVO = new DatasourceDetailVO();
        Domaindatasource.DomainDataSource data = queryDomainDataSourceResponse.getData();
        datasourceDetailVO.setDatasourceId(data.getDatasourceId());
        datasourceDetailVO.setType(StringUtils.toRootUpperCase(data.getType()));
        if (StringUtils.isNotBlank(data.getStatus())) {
            datasourceDetailVO.setStatus(data.getStatus());
        }
        datasourceDetailVO.setName(data.getName());
        datasourceDetailVO.setNodeId(data.getDomainId());

        Domaindatasource.DataSourceInfo info = data.getInfo();
        switch (StringUtils.toRootUpperCase(data.getType())) {
            case DomainDatasourceConstants.DEFAULT_OSS_DATASOURCE_TYPE:
                Domaindatasource.OssDataSourceInfo oss = info.getOss();
                datasourceDetailVO.setInfo(OssDatasourceInfo.create(oss.getEndpoint(), oss.getBucket(), oss.getPrefix(), oss.getAccessKeyId(), oss.getAccessKeyId(), oss.getStorageType(), oss.getVirtualhost()));
                break;
            default:
                break;
        }
        return datasourceDetailVO;
    }
}
