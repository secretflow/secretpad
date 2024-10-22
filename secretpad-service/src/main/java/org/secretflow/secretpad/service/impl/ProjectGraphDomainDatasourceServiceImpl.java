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

import org.secretflow.secretpad.common.constant.KusciaDataSourceConstants;
import org.secretflow.secretpad.common.constant.ProjectConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.ProjectGraphDomainDatasourceService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.graph.CreateGraphRequest;
import org.secretflow.secretpad.service.model.graph.FullUpdateGraphRequest;
import org.secretflow.secretpad.service.model.graph.GetGraphRequest;
import org.secretflow.secretpad.service.model.graph.GraphDetailVO;
import org.secretflow.secretpad.service.model.project.ProjectGraphDomainDataSourceVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.ObjectUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import static org.secretflow.secretpad.common.enums.DataSourceTypeEnum.LOCAL;
import static org.secretflow.secretpad.common.enums.DataSourceTypeEnum.OSS;

/**
 * @author yutu
 * @date 2024/05/24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectGraphDomainDatasourceServiceImpl implements ProjectGraphDomainDatasourceService {

    private final ProjectGraphDomainDatasourceRepository repository;
    private final NodeRepository nodeRepository;
    private final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;
    private final EnvService envService;
    private final ProjectNodeRepository projectNodeRepository;
    private final ProjectRepository projectRepository;

    private final ProjectApprovalConfigRepository projectApprovalConfigRepository;

    @Override
    public ProjectGraphDomainDatasourceDO getById(String projectId, String graphId, String domainId) {
        return repository.findById(ProjectGraphDomainDatasourceDO.UPK
                .builder()
                .domainId(domainId)
                .graphId(graphId)
                .projectId(projectId)
                .build()
        ).orElse(null);
    }

    @Override
    public ProjectGraphDomainDatasourceDO save(ProjectGraphDomainDatasourceDO projectGraphDomainDatasource) {
        return repository.save(projectGraphDomainDatasource);
    }

    @Override
    public void deleteById(String projectId, String graphId, String domainId) {
        repository.deleteById(ProjectGraphDomainDatasourceDO.UPK.builder()
                .domainId(domainId)
                .graphId(graphId)
                .projectId(projectId)
                .build());
    }

    @Override
    public ProjectGraphDomainDatasourceDO update(ProjectGraphDomainDatasourceDO projectGraphDomainDatasource) {
        checkEditEnable(projectGraphDomainDatasource);
        return repository.save(projectGraphDomainDatasource);
    }

    @Override
    public List<ProjectGraphDomainDatasourceDO> getAll() {
        return repository.findAll();
    }

    public Domaindatasource.ListDomainDataSourceResponse findDomainDataSourceByNodeId(String nodeId) {
        if (envService.isAutonomy()) {
            return kusciaGrpcClientAdapter.listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest.newBuilder().setDomainId(nodeId).build(), nodeId);
        }
        return kusciaGrpcClientAdapter.listDomainDataSource(Domaindatasource.ListDomainDataSourceRequest.newBuilder().setDomainId(nodeId).build());
    }

    public NodeDO findDataSourceByNodeId(String nodeId) {
        Optional<NodeDO> nodeDO = nodeRepository.findById(nodeId);
        if (nodeDO.isPresent()) {
            return nodeDO.get();
        }
        throw new RuntimeException("not found node with id:" + nodeId);
    }


    public boolean checkEditEnable(ProjectGraphDomainDatasourceDO projectGraphDomainDatasource) {
        if (projectGraphDomainDatasource == null || projectGraphDomainDatasource.getUpk() == null) {
            throw new IllegalArgumentException("Can't update by a null data object.");
        }
        ProjectGraphDomainDatasourceDO byId = getById(
                projectGraphDomainDatasource.getUpk().getProjectId(),
                projectGraphDomainDatasource.getUpk().getGraphId(),
                projectGraphDomainDatasource.getUpk().getDomainId());
        if (ObjectUtils.isEmpty(byId)) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "current node dataSources is empty" + byId);
        }
        if (!byId.getEditEnable()) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "saved node dataSources are not editable" + byId.getDataSourceId());
        }
        return true;
    }

    @Override
    public Set<ProjectGraphDomainDataSourceVO.DataSource> getDomainDataSources(String domainId) {
        Set<ProjectGraphDomainDataSourceVO.DataSource> result;
        Domaindatasource.ListDomainDataSourceResponse domainDataSourceByNodeId = findDomainDataSourceByNodeId(domainId);
        if (ObjectUtils.isNotEmpty(domainDataSourceByNodeId) && domainDataSourceByNodeId.getStatus().getCode() == 0) {
            result = domainDataSourceByNodeId.getData().getDatasourceListList().stream().map(dataSource -> ProjectGraphDomainDataSourceVO.DataSource.builder()
                    .dataSourceName(dataSource.getName())
                    .type(DataSourceTypeEnum.kuscia2platform(dataSource.getType()))
                    .dataSourceId(dataSource.getDatasourceId())
                    .nodeId(domainId)
                    .build()).collect(Collectors.toSet());
        } else {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "failed to query domain data sources " + domainDataSourceByNodeId.getStatus());
        }
        return result;
    }

    public Set<ProjectGraphDomainDataSourceVO.DataSource> getLOCALFSDomainDataSources(String domainId) {
        return getDomainDataSources(domainId).stream().filter(ds -> ds.getType().equals(LOCAL.name())).collect(Collectors.toSet());
    }

    public Set<ProjectGraphDomainDataSourceVO.DataSource> getOSSDomainDataSources(String domainId) {
        return getDomainDataSources(domainId).stream().filter(ds -> ds.getType().equals(OSS.name())).collect(Collectors.toSet());
    }


    public Set<String> getControlNodeIds(String projectId) {
        UserOwnerTypeEnum ownerType = UserContext.getUser().getOwnerType();
        String ownerId = UserContext.getUser().getOwnerId();
        Set<String> controlNodeIds = Sets.newHashSet(ownerId);
        Set<String> projectNodes = new HashSet<>();
        Optional<ProjectDO> projectDOOptional = projectRepository.findById(projectId);
        boolean isArchived = false;
        if (projectDOOptional.isPresent()) {
            ProjectDO projectDO = projectDOOptional.get();
            if (ProjectStatusEnum.ARCHIVED.getCode().equals(projectDO.getStatus())) {
                isArchived = true;
                Optional<ProjectApprovalConfigDO> projectApprovalConfigDOOptional = projectApprovalConfigRepository.findByProjectIdAndType(projectId, VoteTypeEnum.PROJECT_CREATE.name());
                if (projectApprovalConfigDOOptional.isPresent()) {
                    ProjectApprovalConfigDO projectApprovalConfigDO = projectApprovalConfigDOOptional.get();
                    List<ParticipantNodeInstVO> participantNodeInfo = projectApprovalConfigDO.getParticipantNodeInfo();
                    participantNodeInfo.forEach(pn -> {
                        projectNodes.add(pn.getInitiatorNodeId());
                        projectNodes.addAll(pn.getInvitees().stream().map(e -> e.getInviteeId()).collect(Collectors.toSet()));
                    });
                }

            }
        }
        List<ProjectNodeDO> projectNodeDOList = projectNodeRepository.findByProjectId(projectId);
        if (CollectionUtils.isEmpty(projectNodeDOList) && !isArchived) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "can't find any nodes in project:" + projectId);
        }
        if (!isArchived) {
            projectNodeDOList.forEach(n -> {
                projectNodes.add(n.getUpk().getNodeId());
            });
        }

        if (CollectionUtils.isEmpty(projectNodes)) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "can't find any nodes in project:" + projectId);
        }
        switch (ownerType) {
            case CENTER -> {
                if (envService.isCenter()) {
                    controlNodeIds.remove(ownerId);
                    controlNodeIds.addAll(projectNodes);
                }
            }
            case P2P -> {
                controlNodeIds.clear();
                for (String n : projectNodes) {
                    if (P2pDataSyncProducerTemplate.nodeIds.contains(n)) {
                        controlNodeIds.add(n);
                    }
                }
            }
            case EDGE -> {
                if (envService.isCenter()) {
                    controlNodeIds.clear();
                    controlNodeIds.addAll(projectNodes);
                } else {
                    throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "unsupported user roles" + ownerType);
                }
            }
            default ->
                    throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "unsupported user roles" + ownerType);
        }
        if (CollectionUtils.isEmpty(controlNodeIds)) {
            throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "can't find any control nodes in project:" + projectId);
        }
        log.info("projectId {} controlNodeIds:{}", projectId, controlNodeIds);
        return controlNodeIds;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateProjectGraphDomainDatasourceDOByFullUpdateGraphRequest(FullUpdateGraphRequest request) {
        Set<String> controlNodeIds = getControlNodeIds(request.getProjectId());
        List<FullUpdateGraphRequest.GraphDataSourceConfig> dataSourceConfig = request.getDataSourceConfig();
        if (!CollectionUtils.isEmpty(dataSourceConfig)) {
            dataSourceConfig.forEach(config -> {
                if (!controlNodeIds.contains(config.getNodeId())) {
                    throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "unauthorized access to node" + config.getNodeId() + ",by project " + request.getProjectId());
                }
                Set<ProjectGraphDomainDataSourceVO.DataSource> domainDataSourcesByProjectAndComputeMode = getDomainDataSourcesByProjectAndComputeMode(request.getProjectId(), config.getNodeId());
                if (CollectionUtils.isEmpty(domainDataSourcesByProjectAndComputeMode)) {
                    throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "no available data sources for compute mode" + request.getProjectId());
                }
                Set<String> domainDataSourcesByProjectAndComputeModeConfigs = domainDataSourcesByProjectAndComputeMode.stream()
                        .map(ProjectGraphDomainDataSourceVO.DataSource::getDataSourceId)
                        .collect(Collectors.toSet());
                ProjectGraphDomainDataSourceVO.DataSource ds = domainDataSourcesByProjectAndComputeMode.stream()
                        .filter(d -> d.getDataSourceId().equals(config.getDataSourceId())).findFirst()
                        .orElseThrow(() -> SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "can't find data source " + config.getDataSourceId()));
                if (domainDataSourcesByProjectAndComputeModeConfigs.contains(config.getDataSourceId())) {
                    update(ProjectGraphDomainDatasourceDO.builder()
                            .upk(ProjectGraphDomainDatasourceDO.UPK.builder()
                                    .projectId(request.getProjectId())
                                    .graphId(request.getGraphId())
                                    .domainId(config.getNodeId())
                                    .build())
                            .dataSourceId(config.getDataSourceId())
                            .dataSourceName(ds.getDataSourceName())
                            .editEnable(Boolean.FALSE)
                            .build());
                } else {
                    throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "wrong data source " + config.getDataSourceId());
                }
            });
        }
    }

    public List<GraphDetailVO.DataSourceConfig> convertToGraphDetailVODataSourceConfig(GetGraphRequest request) {
        if (request != null) {
            Set<String> controlNodeIds = getControlNodeIds(request.getProjectId());
            List<ProjectGraphDomainDatasourceDO> all = repository.findByProjectIdAndGraphId(request.getProjectId(), request.getGraphId());
            if (!CollectionUtils.isEmpty(all)) {
                List<GraphDetailVO.DataSourceConfig> collect = all.stream().filter(d -> controlNodeIds.contains(d.getUpk().getDomainId())).map(d -> GraphDetailVO.DataSourceConfig.builder()
                        .nodeId(d.getUpk().getDomainId())
                        .dataSourceId(d.getDataSourceId())
                        .editEnable(d.getEditEnable())
                        .dataSourceName(d.getDataSourceName())
                        .nodeName(findDataSourceByNodeId(d.getUpk().getDomainId()).getName())
                        .build()).collect(Collectors.toList());
                log.info("convertToGraphDetailVODataSourceConfig end request {} collect {}", request, collect);
                return collect;
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createGraphAndInitDefaultDataSource(CreateGraphRequest request, String graphId) {
        log.info("createGraphAndInitDefaultDataSource start request {} graphId {}", request, graphId);
        if (request != null) {
            Set<String> controlNodeIds = getControlNodeIds(request.getProjectId());
            if (!CollectionUtils.isEmpty(controlNodeIds)) {
                controlNodeIds.forEach(nodeId -> {
                    save(ProjectGraphDomainDatasourceDO.builder()
                            .upk(ProjectGraphDomainDatasourceDO.UPK.builder()
                                    .projectId(request.getProjectId())
                                    .graphId(graphId)
                                    .domainId(nodeId)
                                    .build())
                            .dataSourceId(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                            .dataSourceName(KusciaDataSourceConstants.DEFAULT_DATA_SOURCE)
                            .editEnable(Boolean.TRUE)
                            .build());
                });
            }
        }
    }

    public String getProjectComputeMode(String projectId) {
        Optional<ProjectDO> projectDO = projectRepository.findById(projectId);
        String computeMode;
        if (projectDO.isPresent()) {
            ProjectDO project = projectDO.get();
            computeMode = project.getComputeMode();
        } else {
            computeMode = ProjectConstants.ComputeModeEnum.MPC.name();
        }
        return computeMode;
    }

    public Set<ProjectGraphDomainDataSourceVO.DataSource> getDomainDataSourcesByProjectAndComputeMode(String projectId) {
        String computeMode = getProjectComputeMode(projectId);
        Set<String> controlNodeIds = getControlNodeIds(projectId);
        if (ProjectConstants.ComputeModeEnum.TEE.name().equalsIgnoreCase(computeMode)) {
            return getLOCALFSDomainDataSources(controlNodeIds.iterator().next());
        } else {
            return new HashSet<>(getDomainDataSources(controlNodeIds.iterator().next()));
        }
    }

    public Set<ProjectGraphDomainDataSourceVO.DataSource> getDomainDataSourcesByProjectAndComputeMode(String projectId, String domainId) {
        String computeMode = getProjectComputeMode(projectId);
        if (ProjectConstants.ComputeModeEnum.TEE.name().equalsIgnoreCase(computeMode)) {
            return getLOCALFSDomainDataSources(domainId);
        } else {
            return new HashSet<>(getDomainDataSources(domainId));
        }
    }

}
