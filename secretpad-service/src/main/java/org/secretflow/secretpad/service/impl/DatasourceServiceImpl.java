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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.service.DatasourceService;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.handler.datasource.DatasourceHandler;
import org.secretflow.secretpad.service.model.datasource.*;
import org.secretflow.secretpad.service.model.datatable.DatatableVO;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.secretflow.secretpad.service.util.RateLimitUtil.verifyRate;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Service
@Slf4j
public class DatasourceServiceImpl implements DatasourceService {

    private final Map<DataSourceTypeEnum, DatasourceHandler> datasourceHandlerMap;


    private final DatatableService datatableService;

    public DatasourceServiceImpl(Map<DataSourceTypeEnum, DatasourceHandler> datasourceHandlerMap,
                                 DatatableService datatableService) {
        this.datasourceHandlerMap = datasourceHandlerMap;
        this.datatableService = datatableService;
    }

    @Override
    public CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        verifyRate();
        return datasourceHandlerMap.get(DataSourceTypeEnum.valueOf(createDatasourceRequest.getType())).createDatasource(createDatasourceRequest);
    }


    @Override
    public void deleteDatasource(DeleteDatasourceRequest deleteDatasourceRequest) {
        //only oss support delete at current
        datasourceHandlerMap.get(DataSourceTypeEnum.valueOf(deleteDatasourceRequest.getType())).deleteDatasource(deleteDatasourceRequest);
    }

    @Override
    public DatasourceDetailVO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        return datasourceHandlerMap.get(DataSourceTypeEnum.valueOf(datasourceDetailRequest.getType())).datasourceDetail(datasourceDetailRequest);
    }

    @Override
    public DatasourceListVO listDatasource(DatasourceListRequest datasourceListRequest) {
        List<DatasourceListInfo> datasourceListInfos = new ArrayList<>();
        List<String> types = datasourceListRequest.getTypes();
        List<DataSourceTypeEnum> expectTypes = filterKusciaType(types);
        for (DataSourceTypeEnum searchType : expectTypes) {
            List<DatasourceListInfo> datasourceList = datasourceHandlerMap.get(searchType).listDatasource(datasourceListRequest);
            datasourceListInfos.addAll(datasourceList);
        }
        datasourceListInfos = filterByName(datasourceListRequest.getName(), datasourceListInfos);
        datasourceListInfos = filterByType(types, datasourceListInfos);
        datasourceListInfos = filterByStatus(datasourceListRequest.getStatus(), datasourceListInfos);
        datasourceListInfos = getRelatedDatasourceDatatables(datasourceListInfos);
        int startIndex = datasourceListRequest.getSize() * (datasourceListRequest.getPage() - 1);
        if (startIndex > datasourceListInfos.size()) {
            log.error("When list datasource  by node id, the page start index {} > datasourceListInfos len {}", startIndex, datasourceListInfos.size());
            throw SecretpadException.of(SystemErrorCode.OUT_OF_RANGE_ERROR, "page start index > datasource  list length.");
        }
        int endIndex = Math.min((startIndex + datasourceListRequest.getSize()), datasourceListInfos.size());
        log.info("After page, we show from {} to {}", startIndex, endIndex);
        return DatasourceListVO.newInstance(datasourceListInfos.subList(startIndex, endIndex), datasourceListRequest.getPage(), datasourceListRequest.getSize(), Long.valueOf(datasourceListInfos.size()));
    }

    @NotNull
    private static List<DataSourceTypeEnum> filterKusciaType(List<String> searchTypes) {
        if (CollectionUtils.isEmpty(searchTypes)) {
            return Lists.newArrayList(DataSourceTypeEnum.OSS, DataSourceTypeEnum.HTTP);
        }
        return searchTypes.stream()
                .map(DataSourceTypeEnum::valueOf)
                .collect(Collectors.partitioningBy(DataSourceTypeEnum::isKusciaControl))
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    boolean isKusciaControl = entry.getKey();
                    List<DataSourceTypeEnum> enums = entry.getValue();
                    if (isKusciaControl) {
                        return enums.stream().limit(1);
                    } else {
                        return enums.stream();
                    }
                }).collect(Collectors.toList());
    }

    private List<DatasourceListInfo> getRelatedDatasourceDatatables(List<DatasourceListInfo> datasourceListInfos) {
        if (CollectionUtils.isEmpty(datasourceListInfos)) {
            return Collections.EMPTY_LIST;
        }
        List<DatatableVO> datatableVOS = datatableService.findDatatableByNodeId(datasourceListInfos.get(0).getNodeId());
        if (!CollectionUtils.isEmpty(datatableVOS)) {
            Map<String, List<String>> result = datatableVOS.stream()
                    .collect(Collectors.groupingBy(
                            DatatableVO::getDatasourceId,
                            Collectors.mapping(
                                    DatatableVO::getDatatableName,
                                    Collectors.toList()
                            )
                    ));
            datasourceListInfos.forEach(it -> {
                if (result.containsKey(it.getDatasourceId())) {
                    it.setRelatedDatas(result.get(it.getDatasourceId()));
                }
            });
        }
        return datasourceListInfos;
    }

    private List<DatasourceListInfo> filterByStatus(String status, List<DatasourceListInfo> datasourceListInfos) {
        if (StringUtils.isBlank(status)) {
            return datasourceListInfos;
        }
        return datasourceListInfos.stream().filter(
                it -> status.equalsIgnoreCase(it.getStatus())
        ).collect(Collectors.toList());
    }

    private List<DatasourceListInfo> filterByType(List<String> types, List<DatasourceListInfo> datasourceListInfos) {
        if (CollectionUtils.isEmpty(types)) {
            return datasourceListInfos;
        }
        return datasourceListInfos.stream().filter(
                it -> types.contains(StringUtils.toRootUpperCase(it.getType()))
        ).collect(Collectors.toList());
    }

    private List<DatasourceListInfo> filterByName(String name, List<DatasourceListInfo> datasourceListInfos) {
        if (StringUtils.isBlank(name)) {
            return datasourceListInfos;
        }
        return datasourceListInfos.stream().filter(
                it -> it.getName().contains(name)
        ).collect(Collectors.toList());
    }


}
