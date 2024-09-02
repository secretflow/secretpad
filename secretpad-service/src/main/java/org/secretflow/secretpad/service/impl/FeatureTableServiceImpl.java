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

import org.secretflow.secretpad.common.constant.Constants;
import org.secretflow.secretpad.common.constant.DomainDatasourceConstants;
import org.secretflow.secretpad.common.errorcode.FeatureTableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.FeatureTableDO;
import org.secretflow.secretpad.persistence.entity.ProjectFeatureTableDO;
import org.secretflow.secretpad.persistence.repository.FeatureTableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectFeatureTableRepository;
import org.secretflow.secretpad.service.FeatureTableService;
import org.secretflow.secretpad.service.model.datasource.feature.CreateFeatureDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.feature.FeatureDataSourceVO;
import org.secretflow.secretpad.service.model.datatable.TableColumnVO;
import org.secretflow.secretpad.service.util.IpFilterUtil;

import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static org.secretflow.secretpad.service.util.RateLimitUtil.verifyRate;

/**
 * @author chenmingliang
 * @date 2024/01/24
 */
@Service
public class FeatureTableServiceImpl implements FeatureTableService {


    @Resource
    private FeatureTableRepository featureTableRepository;

    @Resource
    private ProjectFeatureTableRepository projectFeatureTableRepository;

    @Resource
    private IpFilterUtil ipFilterUtil;

    @Override
    public void createFeatureTable(CreateFeatureDatasourceRequest createFeatureDatasourceRequest) {
        if (ipFilterUtil.urlIsIpInRange(createFeatureDatasourceRequest.getUrl())) {
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_IP_FILTER, createFeatureDatasourceRequest.getUrl());
        }
        verifyRate();
        String status = Constants.STATUS_UNAVAILABLE;
        createFeatureDatasourceRequest.getNodeIds().forEach(nodeId -> {
            FeatureTableDO featureTableDO = FeatureTableDO.builder()
                    .upk(new FeatureTableDO.UPK(UUIDUtils.random(8), nodeId, StringUtils.isBlank(createFeatureDatasourceRequest.getDatasourceId()) ? DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_ID : createFeatureDatasourceRequest.getDatasourceId()))
                    .featureTableName(createFeatureDatasourceRequest.getFeatureTableName())
                    .type(createFeatureDatasourceRequest.getType())
                    .desc(createFeatureDatasourceRequest.getDesc())
                    .url(createFeatureDatasourceRequest.getUrl())
                    .columns(createFeatureDatasourceRequest.getColumns().stream().map(e -> FeatureTableDO.TableColumn.builder().colName(e.getColName()).colType(e.getColType()).colComment(e.getColComment()).build()).collect(Collectors.toList()))
                    .status(status)
                    .build();
            featureTableRepository.save(featureTableDO);
        });
    }


    @Override
    public List<FeatureDataSourceVO> featureDatasourceList(String nodeId) {
        List<FeatureTableDO> featureTableDOS = featureTableRepository.findByNodeId(nodeId);
        return featureTableDOS.stream().map(e ->
                FeatureDataSourceVO.builder()
                        .featureTableId(e.getUpk().getFeatureTableId())
                        .featureTableName(e.getFeatureTableName())
                        .nodeId(e.getUpk().getNodeId())
                        .columns(e.getColumns().stream().map(c -> TableColumnVO.builder()
                                .colName(c.getColName())
                                .colType(c.getColType())
                                .colComment(c.getColComment())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());
    }

    @Override
    public List<FeatureDataSourceVO> projectFeatureTableList(String nodeId, String projectId) {
        List<ProjectFeatureTableDO> projectFeatureTableDOList = projectFeatureTableRepository.findByNodeIdAndProjectId(nodeId, projectId);
        if (CollectionUtils.isEmpty(projectFeatureTableDOList)) {
            return Collections.EMPTY_LIST;
        }
        List<String> featureTableIds = projectFeatureTableDOList.stream().map(e -> e.getUpk().getFeatureTableId()).collect(Collectors.toList());
        List<FeatureTableDO> featureTableDOS = featureTableRepository.findByFeatureTableIdIn(featureTableIds);
        Map<FeatureTableDO.UPK, FeatureTableDO> featureTableDOMap = featureTableDOS.stream().collect(Collectors.toMap(e -> e.getUpk(), Function.identity()));

        return projectFeatureTableDOList.stream().map(e ->
                FeatureDataSourceVO.builder()
                        .featureTableId(e.getUpk().getFeatureTableId())
                        .featureTableName(featureTableDOMap.getOrDefault(new FeatureTableDO.UPK(e.getUpk().getFeatureTableId(), e.getNodeId(), e.getUpk().getDatasourceId()), new FeatureTableDO()).getFeatureTableName())
                        .nodeId(e.getUpk().getNodeId())
                        .columns(featureTableDOMap.getOrDefault(new FeatureTableDO.UPK(e.getUpk().getFeatureTableId(), e.getNodeId(), e.getUpk().getDatasourceId()), new FeatureTableDO()).getColumns().stream().map(
                                c -> TableColumnVO.builder()
                                        .colName(c.getColName())
                                        .colType(c.getColType())
                                        .colComment(c.getColComment())
                                        .build()
                        ).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());
    }


}
