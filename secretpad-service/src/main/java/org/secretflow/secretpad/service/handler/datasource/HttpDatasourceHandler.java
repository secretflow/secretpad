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
import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.InstService;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailRequest;
import org.secretflow.secretpad.service.model.datasource.DatasourceDetailUnAggregateDTO;
import org.secretflow.secretpad.service.model.datasource.DatasourceListInfoUnAggregate;
import org.secretflow.secretpad.service.model.node.NodeVO;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenmingliang
 * @date 2024/05/27
 */
@Component
@Slf4j
@AllArgsConstructor
public class HttpDatasourceHandler extends AbstractDatasourceHandler {


    @Resource
    private InstService instService;

    @Resource
    private EnvService envService;

    @Override

    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.HTTP);
    }

    @Override
    public List<DatasourceListInfoUnAggregate> listDatasource(String nodeId) {
        log.info("list http datasource");
        DatasourceListInfoUnAggregate info = new DatasourceListInfoUnAggregate();
        info.setNodeId(nodeId);
        info.setDatasourceId(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);
        info.setName(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_NAME);
        info.setType(DataSourceTypeEnum.HTTP.name());
        if (envService.isAutonomy()){
            Map<String, String> nodeVOMap = instService.listNode().stream().collect(Collectors.toMap(NodeVO::getNodeId, NodeVO::getNodeStatus));
            info.setStatus(nodeVOMap.get(nodeId) == null ? Constants.STATUS_UNAVAILABLE : parses(nodeVOMap.get(nodeId)));
        }else{
            info.setStatus(Constants.STATUS_AVAILABLE);
        }
        return Lists.newArrayList(info);
    }

    private String parses(String nodeStatus) {
        String status;
        switch (nodeStatus) {
            case DomainConstants.NODE_READY -> status = Constants.STATUS_AVAILABLE;
            default -> status = Constants.STATUS_UNAVAILABLE;
        }
        return status;
    }

    @Override
    public DatasourceDetailUnAggregateDTO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        DatasourceDetailUnAggregateDTO datasourceDetailUnAggregateDTO = new DatasourceDetailUnAggregateDTO();
        datasourceDetailUnAggregateDTO.setNodeId(datasourceDetailRequest.getOwnerId());
        datasourceDetailUnAggregateDTO.setType(DataSourceTypeEnum.HTTP.name());
        datasourceDetailUnAggregateDTO.setName(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_NAME);
        datasourceDetailUnAggregateDTO.setDatasourceId(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID);
        datasourceDetailUnAggregateDTO.setStatus(Constants.STATUS_AVAILABLE);
        return datasourceDetailUnAggregateDTO;
    }
}
