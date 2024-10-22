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

import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.enums.DataTableTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.FeatureTableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectFeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.service.model.datatable.*;
import org.secretflow.secretpad.service.util.HttpUtils;
import org.secretflow.secretpad.service.util.IpFilterUtil;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lufeng
 * @date 2024/8/19
 */
@Component
@Slf4j
public class HttpDatatableHandler implements DatatableHandler {


    @Override
    public List<DataSourceTypeEnum> supports() {
        return Lists.newArrayList(DataSourceTypeEnum.HTTP);
    }

    @Resource
    private FeatureTableRepository featureTableRepository;
    @Resource
    private ProjectFeatureTableRepository projectFeatureTableRepository;
    @Resource
    private NodeRepository nodeRepository;
    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private IpFilterUtil ipFilterUtil;

    @Override
    public CreateDatatableVO createDatatable(CreateDatatableRequest createDatatableRequest) {
        if (ipFilterUtil.urlIsIpInRange(createDatatableRequest.getRelativeUri())) {
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_IP_FILTER, createDatatableRequest.getRelativeUri());
        }
        // set DefaultStatus
        String status = Constants.STATUS_UNAVAILABLE;
        List<FeatureTableDO> featureTableDOList = createFeatureTableDOList(createDatatableRequest, status);
        // performABatchSaveOperation
        saveFeatureTables(featureTableDOList);
        // Return the result. It is assumed that returning null is intentional and can be adjusted according to the actual situation.
        return new CreateDatatableVO();
    }

    @Override
    public DatatableNodeVO queryDatatable(GetDatatableRequest request) {
        Optional<FeatureTableDO> featureTableDOOptional = featureTableRepository.findById(new FeatureTableDO.UPK(request.getDatatableId(), request.getNodeId(), DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
        if (featureTableDOOptional.isEmpty()) {
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_NOT_EXIST);
        }
        FeatureTableDO featureTableDO = featureTableDOOptional.get();
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> datatableAuthPairs = getHttpFeatureAuthProjectPairs(request.getNodeId(), Lists.newArrayList(featureTableDO.getUpk().getFeatureTableId()));
        boolean success = HttpUtils.detection(featureTableDO.getUrl());
        String status = success ? Constants.STATUS_AVAILABLE : Constants.STATUS_UNAVAILABLE;
        featureTableDO.setStatus(status);
        featureTableRepository.save(featureTableDO);
        DatatableDTO datatableDTO = DatatableDTO.builder().datatableId(featureTableDO.getUpk().getFeatureTableId()).datatableName(featureTableDO.getFeatureTableName()).nodeId(featureTableDO.getNodeId()).relativeUri(featureTableDO.getUrl()).datasourceId(DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID).status(status).datasourceType(DataSourceTypeEnum.HTTP.name()).type(DataTableTypeEnum.HTTP.name()).schema(featureTableDO.getColumns().stream().map(it -> new DatatableDTO.TableColumnDTO(it.getColName(), it.getColType(), it.getColComment())).collect(Collectors.toList())).build();
        DatatableVO datatableVO = DatatableVO.from(datatableDTO, datatableAuthPairs.containsKey(datatableDTO.getDatatableId()) ? AuthProjectVO.fromPairs(datatableAuthPairs.get(datatableDTO.getDatatableId())) : null, null);
        return DatatableNodeVO.builder().datatableVO(datatableVO).nodeId(request.getNodeId()).nodeName(nodeRepository.findByNodeId(request.getNodeId()).getName()).build();
    }

    @Override
    public void deleteDatatable(DeleteDatatableRequest request) {
        Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> featureAuthProjectPairs = getHttpFeatureAuthProjectPairs(request.getNodeId(), Collections.singletonList(request.getDatatableId()));
        if (!CollectionUtils.isEmpty(featureAuthProjectPairs)) {
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_DUPLICATED_AUTHORIZED);
        }
        featureTableRepository.deleteById(new FeatureTableDO.UPK(request.getDatatableId(), request.getNodeId(), DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID));
    }

    private List<FeatureTableDO> createFeatureTableDOList(CreateDatatableRequest request, String status) {
        return request.getNodeIds().stream()
                .map(nodeId -> buildFeatureTableDO(nodeId, request, status))
                .collect(Collectors.toList());
    }

    private FeatureTableDO buildFeatureTableDO(String nodeId, CreateDatatableRequest request, String status) {
        return FeatureTableDO.builder()
                .upk(new FeatureTableDO.UPK(UUIDUtils.random(8), nodeId, StringUtils.isBlank(request.getDatasourceId()) ? DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID : request.getDatasourceId()))
                .featureTableName(request.getDatatableName())
                .type(request.getDatasourceType())
                .desc(request.getDesc())
                .url(request.getRelativeUri())
                .columns(request.getColumns().stream().map(e -> FeatureTableDO.TableColumn.builder().colName(e.getColName()).colType(e.getColType()).colComment(e.getColComment()).build()).collect(Collectors.toList()))
                .status(status)
                .build();
    }

    private void saveFeatureTables(List<FeatureTableDO> featureTableDOList) {
        try {
            featureTableRepository.saveAll(featureTableDOList);
        } catch (Exception e) {
            log.error("Failed to save feature tables", e);
            throw new RuntimeException("Failed to save feature tables", e);
        }
    }

    private Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getHttpFeatureAuthProjectPairs(String nodeId, List<String> featureTableIds) {
        List<ProjectFeatureTableDO> featureTableDOS = projectFeatureTableRepository.findByNodeIdAndFeatureTableIds(nodeId, featureTableIds);
        List<ProjectDatatableDO> authProjectDatatables = featureTableDOS.stream().map(e -> ProjectDatatableDO.builder().tableConfig(e.getTableConfig()).source(e.getSource()).upk(new ProjectDatatableDO.UPK(e.getUpk().getProjectId(), e.getUpk().getNodeId(), e.getUpk().getFeatureTableId())).build()).collect(Collectors.toList());
        return getStringListMap(authProjectDatatables);
    }

    public Map<String, List<Pair<ProjectDatatableDO, ProjectDO>>> getStringListMap(List<ProjectDatatableDO> authProjectDatatables) {
        List<String> projectIds = authProjectDatatables.stream().map(it -> it.getUpk().getProjectId()).collect(Collectors.toList());
        Map<String, ProjectDO> projectMap = projectRepository.findAllById(projectIds).stream().collect(Collectors.toMap(ProjectDO::getProjectId, Function.identity()));
        return authProjectDatatables.stream().map(
                        // List<Pair>
                        it -> new Pair<>(it, projectMap.getOrDefault(it.getUpk().getProjectId(), null))).filter(it -> it.getValue1() != null)
                // Map<datatable, List<Pair>>
                .collect(Collectors.groupingBy(it -> it.getValue0().getUpk().getDatatableId()));
    }


}
