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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JpaQueryHelper;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.*;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectResultRepository;
import org.secretflow.secretpad.service.DataService;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.NodeService;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.data.DataSourceVO;
import org.secretflow.secretpad.service.model.datatable.TableColumnVO;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.project.ProjectJobVO;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Node service implementation class
 *
 * @author : xiaonan.fhn
 * @date 2023/5/23
 */
@Service
public class NodeServiceImpl implements NodeService {

    private final static Logger LOGGER = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private AbstractNodeManager nodeManager;

    @Autowired
    private AbstractDatatableManager datatableManager;

    @Autowired
    private ProjectResultRepository resultRepository;

    /**
     * Todo: part of the projectService logic should be brought into use in projectManager
     */
    @Autowired
    private ProjectService projectService;

    /**
     * Todo: part of the graphService logic should be brought into use in graphManager
     */
    @Autowired
    private GraphService graphService;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    public DataService dataService;

    @Override
    public List<NodeVO> listNodes() {
        List<NodeDTO> nodeDTOList = nodeManager.listNode();
        return nodeDTOList.stream()
                .map(it -> NodeVO.from(it,
                        datatableManager.findByNodeId(it.getNodeId(), AbstractDatatableManager.DATA_VENDOR_MANUAL),
                        nodeManager.findBySrcNodeId(it.getNodeId()), resultRepository.countByNodeId(it.getNodeId())))
                .collect(Collectors.toList());
    }

    @Override
    public String createNode(CreateNodeRequest request) {
        return nodeManager.createNode(CreateNodeParam.builder().name(request.getName()).build());
    }

    @Override
    public void deleteNode(String nodeId) {
        nodeManager.deleteNode(nodeId);
    }

    @Override
    public NodeVO getNode(String nodeId) {
        NodeDTO it = nodeManager.getNode(nodeId);
        return NodeVO.from(it, null, null, null);
    }

    @Override
    public void updateNode(UpdateNodeRequest request) {
        NodeDO nodeDO = nodeRepository.findByNodeId(request.getNodeId());
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("update node error, node is not exist {}", request.getNodeId());
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        nodeDO.setNetAddress(request.getNetAddress());
        nodeRepository.save(nodeDO);
    }

    @Override
    public SecretPadPageResponse<NodeVO> queryPage(PageNodeRequest request, Pageable pageable) {
        Page<NodeDO> page = nodeRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> JpaQueryHelper.getPredicate(root, request, criteriaBuilder),
                pageable);
        if (ObjectUtils.isEmpty(page)) {
            return SecretPadPageResponse.toPage(null, 0);
        }
        List<NodeVO> data = page.stream()
                .map(info -> NodeVO.from(nodeManager.getNode(info.getNodeId()), null, null, null))
                .collect(Collectors.toList());
        return SecretPadPageResponse.toPage(data, page.getTotalElements());
    }

    @Override
    public NodeResultsListVO listResults(ListNodeResultRequest request) {
        NodeResultListDTO nodeResultDTOList = nodeManager.listResult(ListResultParam.builder()
                .nodeId(request.getNodeId()).pageSize(request.getPageSize()).pageNumber(request.getPageNumber())
                .kindFilters(request.getKindFilters()).dataVendorFilter(request.getDataVendorFilter())
                .nameFilter(request.getNameFilter()).timeSortingRule(request.getTimeSortingRule()).build());
        return NodeResultsListVO.fromDTO(nodeResultDTOList);
    }

    @Override
    public NodeVO refreshNode(String nodeId) {
        return NodeVO.fromDto(nodeManager.refreshNode(nodeId));
    }

    @Override
    public NodeTokenVO getNodeToken(String nodeId, boolean refresh) {
        return NodeTokenVO.fromDto(nodeManager.getNodeToken(nodeId, refresh));
    }

    @Override
    public NodeResultDetailVO getNodeResultDetail(GetNodeResultDetailRequest request) {
        LOGGER.info(
                "get node result detail with nodeId = {}, domain data id = {},dataType filter = {}, data vender filter = {}",
                request.getNodeId(), request.getDomainDataId(), request.getDataType(), request.getDataVendor());
        NodeResultDTO nodeResult = nodeManager.getNodeResult(request.getNodeId(), request.getDomainDataId());
        ProjectJobVO projectJob = projectService.getProjectJob(nodeResult.getSourceProjectId(), nodeResult.getJobId());
        Optional<DatatableDTO> datatableDTO = datatableManager
                .findById(DatatableDTO.NodeDatatableId.from(request.getNodeId(), request.getDomainDataId()));
        if (datatableDTO.isEmpty()) {
            LOGGER.error("Cannot find datatable when get node result detail.");
            throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
        }
        List<DataSourceVO> list = dataService.queryDataSources();
        String datasource = "";
        for (DataSourceVO d : list) {
            if (datatableDTO.get().getDatasourceId().equals(d.getName())) {
                datasource = d.getPath();
                break;
            }
        }
        return NodeResultDetailVO.builder().nodeResultsVO(NodeResultsVO.fromNodeResultDTO(nodeResult))
                .graphDetailVO(projectJob.getGraph())
                .tableColumnVOList(
                        datatableDTO.get().getSchema().stream().map(TableColumnVO::from).collect(Collectors.toList()))
                .output(graphService.getResultOutputVO(request.getNodeId(), request.getDomainDataId()))
                .datasource(datasource)
                .build();
    }
}
