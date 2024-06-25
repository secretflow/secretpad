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

import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailRequest;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailVO;
import org.secretflow.secretpad.service.model.datasource.DatasourceListInfo;
import org.secretflow.secretpad.service.model.datasource.DatasourceListRequest;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/05/27
 */
@Component
@Slf4j
@AllArgsConstructor
public class HttpDatasourceHandler extends AbstractDatasourceHandler {


    @Override

    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.HTTP);
    }

    @Override
    public List<DatasourceListInfo> listDatasource(DatasourceListRequest datasourceListRequest) {
        log.info("list http datasource");
        DatasourceListInfo info = new DatasourceListInfo();
        info.setNodeId(datasourceListRequest.getNodeId());
        info.setDatasourceId(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);
        info.setName(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_NAME);
        info.setType(DataSourceTypeEnum.HTTP.name());
        info.setStatus(Constants.STATUS_AVAILABLE);
        return Lists.newArrayList(info);
    }

    @Override
    public DatasourceDetailVO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        DatasourceDetailVO datasourceDetailVO = new DatasourceDetailVO();
        datasourceDetailVO.setNodeId(datasourceDetailRequest.getNodeId());
        datasourceDetailVO.setType(DataSourceTypeEnum.HTTP.name());
        datasourceDetailVO.setName(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_NAME);
        datasourceDetailVO.setDatasourceId(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);
        datasourceDetailVO.setStatus(Constants.STATUS_AVAILABLE);
        return datasourceDetailVO;
    }
}
