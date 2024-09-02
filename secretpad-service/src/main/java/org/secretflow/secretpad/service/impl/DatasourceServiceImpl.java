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
import org.secretflow.secretpad.common.errorcode.ConcurrentErrorCode;
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.errorcode.InstErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.AsyncTaskExecutionUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.service.DatasourceService;
import org.secretflow.secretpad.service.DatatableService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.InstService;
import org.secretflow.secretpad.service.handler.datasource.DatasourceHandler;
import org.secretflow.secretpad.service.model.datasource.*;
import org.secretflow.secretpad.service.model.datatable.DatatableVO;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
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

    private final NodeRepository nodeRepository;

    private final EnvService envService;

    private final InstService instService;

    public DatasourceServiceImpl(Map<DataSourceTypeEnum, DatasourceHandler> datasourceHandlerMap,
                                 DatatableService datatableService,
                                 NodeRepository nodeRepository,
                                 EnvService envService,
                                 InstService instService) {
        this.datasourceHandlerMap = datasourceHandlerMap;
        this.datatableService = datatableService;
        this.nodeRepository = nodeRepository;
        this.envService = envService;
        this.instService = instService;
    }

    @NotNull
    private static List<DataSourceTypeEnum> filterKusciaType(List<String> searchTypes) {
        if (CollectionUtils.isEmpty(searchTypes)) {
            return Lists.newArrayList(DataSourceTypeEnum.OSS, DataSourceTypeEnum.HTTP, DataSourceTypeEnum.ODPS);
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

    @Override
    public CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        verifyRate();
        verifyNodes(createDatasourceRequest);
        return datasourceHandlerMap.get(DataSourceTypeEnum.valueOf(createDatasourceRequest.getType())).createDatasource(createDatasourceRequest);
    }

    private void verifyNodes(CreateDatasourceRequest createDatasourceRequest) {
        if (!CollectionUtils.isEmpty(createDatasourceRequest.getNodeIds()) && envService.isAutonomy()) {
            List<String> distinctNodes = createDatasourceRequest.getNodeIds().stream().distinct().collect(Collectors.toList());
            if (!instService.checkNodesInInst(createDatasourceRequest.getOwnerId(), distinctNodes)) {
                throw SecretpadException.of(InstErrorCode.INST_NOT_MATCH_NODE);
            }
            createDatasourceRequest.setNodeIds(distinctNodes);
        }
    }

    @Override
    public void deleteDatasource(DeleteDatasourceRequest deleteDatasourceRequest) {
        List<String> nodeIds = findAllRelatedNodesOfTheDatasource(deleteDatasourceRequest.getOwnerId(), deleteDatasourceRequest.getDatasourceId(), deleteDatasourceRequest.getType());
        Map<String, String> failedRecord = new ConcurrentHashMap<>();
        nodeIds.forEach(e -> {
            DeleteDatasourceRequest request = new DeleteDatasourceRequest();
            request.setDatasourceId(deleteDatasourceRequest.getDatasourceId());
            request.setOwnerId(e);
            request.setType(deleteDatasourceRequest.getType());
            AsyncTaskExecutionUtils.executeUnDecoratedOperation(request, this::delete, e, failedRecord);
        });
        if (!CollectionUtils.isEmpty(failedRecord)) {
            log.info("delete datasource failed, failed record: {}", JsonUtils.toJSONString(failedRecord));
        }
    }

    @Override
    public DatasourceDetailAggregateVO datasourceDetail(DatasourceDetailRequest datasourceDetailRequest) {
        DatasourceListVO allDatasource = findAllDatasourceByOwnerId(datasourceDetailRequest.getOwnerId(), Lists.newArrayList(datasourceDetailRequest.getType()));
        List<DatasourceListInfoAggregate> infos = allDatasource.getInfos();
        log.info("allDatasource infos: {}", JsonUtils.toJSONString(infos));
        if (!CollectionUtils.isEmpty(infos)) {
            DatasourceListInfoAggregate datasourceListInfoAggregate = infos.stream().filter(e -> StringUtils.equals(e.getDatasourceId(), datasourceDetailRequest.getDatasourceId())).findAny().orElseThrow(() -> SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_NOT_FOUND));
            DatasourceDetailRequest request = new DatasourceDetailRequest();
            request.setDatasourceId(datasourceDetailRequest.getDatasourceId());
            request.setType(datasourceDetailRequest.getType());
            String nodeId = datasourceListInfoAggregate.getNodes().get(0).getNodeId();
            log.info("detail nodeId = {}", nodeId);
            request.setOwnerId(nodeId);
            DatasourceDetailUnAggregateDTO datasourceDetailUnAggregateDTO = datasourceHandlerMap.get(DataSourceTypeEnum.valueOf(datasourceDetailRequest.getType())).datasourceDetail(request);
            log.info("datasourceDetailUnAggregateDTO: {}", JsonUtils.toJSONString(datasourceDetailUnAggregateDTO));
            DatasourceDetailAggregateVO vo = new DatasourceDetailAggregateVO();
            vo.setNodes(datasourceListInfoAggregate.getNodes());
            vo.setDatasourceId(datasourceDetailRequest.getDatasourceId());
            vo.setInfo(datasourceDetailUnAggregateDTO.getInfo());
            vo.setType(datasourceDetailUnAggregateDTO.getType());
            vo.setStatus(datasourceDetailUnAggregateDTO.getStatus());
            vo.setName(datasourceDetailUnAggregateDTO.getName());
            return vo;
        } else {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_NOT_FOUND);
        }
    }

    @Override
    public DatasourceListVO listDatasource(DatasourceListRequest datasourceListRequest) {
        List<DatasourceListInfoUnAggregate> datasourceListInfoUnAggregates = new ArrayList<>();
        List<NodeDO> nodeDOS = nodeRepository.findByInstId(datasourceListRequest.getOwnerId());
        Map<String, String> nodeMap = nodeDOS.stream().collect(Collectors.toMap(NodeDO::getNodeId, NodeDO::getName));
        List<String> nodeIds = Lists.newArrayList();
        if (envService.isAutonomy()) {
            nodeIds.addAll(nodeDOS.stream().map(NodeDO::getNodeId).toList());
        } else {
            nodeIds.add(datasourceListRequest.getOwnerId());
            NodeDO nodeDO = nodeRepository.findByNodeId(datasourceListRequest.getOwnerId());
            nodeMap.put(nodeDO.getNodeId(), nodeDO.getName());
        }
        Map<String, String> failedRecords = new ConcurrentHashMap<>();
        List<CompletableFuture<List<DatasourceListInfoUnAggregate>>> completableFutureDatasourceListInfoList = nodeIds.stream().map(nodeId -> {
            DatasourceListRequest request = new DatasourceListRequest();
            request.setTypes(datasourceListRequest.getTypes());
            request.setOwnerId(nodeId);
            return AsyncTaskExecutionUtils.executeUnDecoratedOperation(request, this::getSingleNodeDatasourceListInfos, nodeId, failedRecords);
        }).collect(Collectors.toList());
        fetchResult(datasourceListInfoUnAggregates, completableFutureDatasourceListInfoList);

        if (!CollectionUtils.isEmpty(failedRecords)) {
            log.info("list datasource failed, failed record: {}", JsonUtils.toJSONString(failedRecords));
        } else {
            log.info("list datasource success, datasource size: {}", datasourceListInfoUnAggregates.size());
        }
        List<DatasourceListInfoAggregate> datasourceListInfoAggregates = datasourceListInfoUnAggregates.stream().collect(Collectors.groupingBy(DatasourceListInfoUnAggregate::getDatasourceId))
                .entrySet()
                .stream()
                .map(entry -> {
                    String datasourceId = entry.getKey();
                    List<DatasourceListInfoUnAggregate> unAggregatedItems = entry.getValue();
                    DatasourceListInfoUnAggregate firstItem = unAggregatedItems.get(0);
                    DatasourceListInfoAggregate aggregate = new DatasourceListInfoAggregate();
                    aggregate.setDatasourceId(datasourceId);
                    aggregate.setName(firstItem.getName());
                    aggregate.setType(firstItem.getType());

                    List<DataSourceRelatedNode> nodes = unAggregatedItems.stream()
                            .map(item -> DataSourceRelatedNode.builder()
                                    .nodeId(item.getNodeId())
                                    .nodeName(nodeMap.get(item.getNodeId()))
                                    .status(item.getStatus())
                                    .build())
                            .collect(Collectors.toList());
                    aggregate.setNodes(nodes);
                    return aggregate;

                }).collect(Collectors.toList());
        datasourceListInfoAggregates = filterByName(datasourceListRequest.getName(), datasourceListInfoAggregates);
        datasourceListInfoAggregates = filterByType(datasourceListRequest.getTypes(), datasourceListInfoAggregates);
        datasourceListInfoAggregates = filterByStatus(datasourceListRequest.getStatus(), datasourceListInfoAggregates);
        datasourceListInfoAggregates = getRelatedDatasourceDatatables(datasourceListInfoAggregates);
        int startIndex = datasourceListRequest.getSize() * (datasourceListRequest.getPage() - 1);
        if (startIndex > datasourceListInfoAggregates.size()) {
            log.error("When list datasource  by node id, the page start index {} > datasourceListInfos len {}", startIndex, datasourceListInfoUnAggregates.size());
            throw SecretpadException.of(SystemErrorCode.OUT_OF_RANGE_ERROR, "page start index > datasource  list length.");
        }
        int endIndex = Math.min((startIndex + datasourceListRequest.getSize()), datasourceListInfoAggregates.size());
        log.info("After page, we show from {} to {}", startIndex, endIndex);
        return DatasourceListVO.newInstance(datasourceListInfoAggregates.subList(startIndex, endIndex), datasourceListRequest.getPage(), datasourceListRequest.getSize(), (long) datasourceListInfoAggregates.size());
    }

    @Override
    public DatasourceNodesVO datasourceNodes(DatasourceNodesRequest datasourceNodesRequest) {

        List<DatasourceListInfoAggregate> datasourceListInfoAggregates = findAllDatasourceByOwnerId(datasourceNodesRequest.getOwnerId(), Lists.newArrayList(DataSourceTypeEnum.HTTP.name(), DataSourceTypeEnum.OSS.name(), DataSourceTypeEnum.ODPS.name())).getInfos();

        DatasourceListInfoAggregate datasourceListInfoAggregate = datasourceListInfoAggregates.stream().filter(e -> StringUtils.equals(e.getDatasourceId(), datasourceNodesRequest.getDatasourceId())).findAny().orElse(null);
        if (datasourceListInfoAggregate == null) {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_NOT_FOUND);
        }
        return DatasourceNodesVO.builder().nodes(datasourceListInfoAggregate.getNodes()).build();
    }

    private void fetchResult(List<DatasourceListInfoUnAggregate> datasourceListInfoUnAggregates, List<CompletableFuture<List<DatasourceListInfoUnAggregate>>> completableFutureDatasourceListInfoList) {
        try {
            CompletableFuture
                    .allOf(completableFutureDatasourceListInfoList.toArray(new CompletableFuture[0]))
                    .get(5000, TimeUnit.MILLISECONDS);
            for (CompletableFuture<List<DatasourceListInfoUnAggregate>> task : completableFutureDatasourceListInfoList) {
                List<DatasourceListInfoUnAggregate> datasourceListInfo = task.get();
                if (!CollectionUtils.isEmpty(datasourceListInfo)) {
                    datasourceListInfoUnAggregates.addAll(datasourceListInfo);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
        } catch (ExecutionException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
        } catch (TimeoutException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
        }
    }

    private List<DatasourceListInfoUnAggregate> getSingleNodeDatasourceListInfos(DatasourceListRequest request) {
        List<DatasourceListInfoUnAggregate> datasourceListInfoUnAggregates = new ArrayList<>();
        List<DataSourceTypeEnum> expectTypes = filterKusciaType(request.getTypes());
        for (DataSourceTypeEnum searchType : expectTypes) {
            List<DatasourceListInfoUnAggregate> datasourceList = datasourceHandlerMap.get(searchType).listDatasource(request.getOwnerId());
            datasourceListInfoUnAggregates.addAll(datasourceList);
        }
        return datasourceListInfoUnAggregates;
    }

    private List<DatasourceListInfoAggregate> getRelatedDatasourceDatatables(List<DatasourceListInfoAggregate> datasourceListInfoAggregates) {
        if (CollectionUtils.isEmpty(datasourceListInfoAggregates)) {
            return Collections.EMPTY_LIST;
        }

        for (DatasourceListInfoAggregate datasourceListInfoAggregate : datasourceListInfoAggregates) {
            List<DataSourceRelatedNode> nodes = datasourceListInfoAggregate.getNodes();
            Map<String, String> failedRecords = new ConcurrentHashMap<>();
            List<CompletableFuture<List<DatatableVO>>> completableFutureList = nodes.stream().map(e -> AsyncTaskExecutionUtils.executeUnDecoratedOperation(e.getNodeId(), datatableService::findDatatableByNodeId, e.getNodeId(), failedRecords)).collect(Collectors.toList());
            fetchRelatedDatas(datasourceListInfoAggregates, completableFutureList);
        }

        return datasourceListInfoAggregates;
    }

    private void fetchRelatedDatas(List<DatasourceListInfoAggregate> datasourceListInfoAggregates, List<CompletableFuture<List<DatatableVO>>> completableFutureList) {
        try {
            CompletableFuture
                    .allOf(completableFutureList.toArray(new CompletableFuture[0]))
                    .get(5000, TimeUnit.MILLISECONDS);
            for (CompletableFuture<List<DatatableVO>> listCompletableFuture : completableFutureList) {
                List<DatatableVO> datatableVOS = listCompletableFuture.get();
                if (!CollectionUtils.isEmpty(datatableVOS)) {
                    Map<String, List<String>> result = datatableVOS.stream()
                            .collect(Collectors.groupingBy(
                                    DatatableVO::getDatasourceId,
                                    Collectors.mapping(
                                            DatatableVO::getDatatableName,
                                            Collectors.toList()
                                    )
                            ));
                    datasourceListInfoAggregates.forEach(it -> {
                        if (result.containsKey(it.getDatasourceId())) {
                            it.setRelatedDatas(result.get(it.getDatasourceId()));
                        }
                    });
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
        } catch (ExecutionException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
        } catch (TimeoutException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
        }
    }

    private List<DatasourceListInfoAggregate> filterByStatus(String status, List<DatasourceListInfoAggregate> datasourceListInfoAggregates) {
        if (StringUtils.isBlank(status)) {
            return datasourceListInfoAggregates;
        }
        return datasourceListInfoAggregates.stream().filter(
                it -> it.getNodes().stream().anyMatch(in -> status.equalsIgnoreCase(in.getStatus()))
        ).collect(Collectors.toList());
    }

    private List<DatasourceListInfoAggregate> filterByType(List<String> types, List<DatasourceListInfoAggregate> datasourceListInfoAggregates) {
        if (CollectionUtils.isEmpty(types)) {
            return datasourceListInfoAggregates;
        }
        return datasourceListInfoAggregates.stream().filter(
                it -> types.contains(StringUtils.toRootUpperCase(it.getType()))
        ).collect(Collectors.toList());
    }

    private List<DatasourceListInfoAggregate> filterByName(String name, List<DatasourceListInfoAggregate> datasourceListInfoAggregates) {
        if (StringUtils.isBlank(name)) {
            return datasourceListInfoAggregates;
        }
        return datasourceListInfoAggregates.stream().filter(
                it -> it.getName().contains(name)
        ).collect(Collectors.toList());
    }


    private void delete(DeleteDatasourceRequest deleteDatasourceRequest) {
        //only oss support delete at current
        datasourceHandlerMap.get(DataSourceTypeEnum.valueOf(deleteDatasourceRequest.getType())).deleteDatasource(deleteDatasourceRequest);
    }

    private List<String> findAllRelatedNodesOfTheDatasource(String ownerId, String datasourceId, String type) {
        DatasourceListVO allDatasource = findAllDatasourceByOwnerId(ownerId, Lists.newArrayList(type));
        List<DatasourceListInfoAggregate> infos = allDatasource.getInfos();
        if (!CollectionUtils.isEmpty(infos)) {
            Optional<DatasourceListInfoAggregate> datasourceListInfoAggregateOptional = infos.stream().filter(e -> StringUtils.equals(e.getDatasourceId(), datasourceId)).findAny();
            if (datasourceListInfoAggregateOptional.isPresent()) {
                DatasourceListInfoAggregate datasourceListInfoAggregate = datasourceListInfoAggregateOptional.get();
                if (CollectionUtils.isEmpty(datasourceListInfoAggregate.getRelatedDatas())) {
                    return datasourceListInfoAggregate.getNodes().stream().map(e -> e.getNodeId()).collect(Collectors.toList());
                } else {
                    throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_DELETE_FAIL, "has related data table");
                }
            } else {
                throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_DELETE_FAIL, "not found the datasource");
            }
        } else {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_DELETE_FAIL, "not found the datasource");
        }
    }

    private DatasourceListVO findAllDatasourceByOwnerId(String ownerId, List<String> types) {
        DatasourceListRequest datasourceListRequest = new DatasourceListRequest();
        datasourceListRequest.setOwnerId(ownerId);
        datasourceListRequest.setTypes(types);
        datasourceListRequest.setSize(1000);
        return listDatasource(datasourceListRequest);
    }
}
