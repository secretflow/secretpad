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

import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JpaQueryHelper;
import org.secretflow.secretpad.common.util.Sha256Utils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.*;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.NodeDO;
import org.secretflow.secretpad.persistence.entity.NodeRouteDO;
import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;
import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.NodeRouteRepository;
import org.secretflow.secretpad.persistence.repository.ProjectResultRepository;
import org.secretflow.secretpad.persistence.repository.TeeNodeDatatableManagementRepository;
import org.secretflow.secretpad.service.*;
import org.secretflow.secretpad.service.model.auth.NodeUserCreateRequest;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.data.DataSourceVO;
import org.secretflow.secretpad.service.model.datatable.TableColumnVO;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.node.p2p.P2pCreateNodeRequest;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.project.ProjectJobVO;

import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
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
    private AbstractNodeRouteManager nodeRouteManager;

    @Autowired
    private AbstractDatatableManager datatableManager;

    @Autowired
    private ProjectResultRepository resultRepository;

    @Autowired
    private NodeUserService nodeUserService;

    @Autowired
    private NodeRouterService nodeRouterService;

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
    private NodeRouteRepository nodeRouteRepository;
    @Autowired
    public DataService dataService;
    @Autowired
    private EnvService envService;

    @Autowired
    private TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository;

    @Value("${tee.domain-id:tee}")
    private String teeNodeId;

    @Value("${secretpad.master-node-id:master}")
    private String masterNodeId;

    @Override
    public List<NodeVO> listNodes() {
        final String userOwnerId = UserContext.getUser().getOwnerId();
        List<NodeDTO> nodeDTOList;
        if (
                envService.isCenter() && StringUtils.equals(UserContext.getUser().getOwnerType().name(), UserOwnerTypeEnum.EDGE.name())
        ) {
            nodeDTOList = nodeManager.listCooperatingNode(userOwnerId);
        } else {
            nodeDTOList = nodeManager.listNode();
        }

        return nodeDTOList.stream()
                .map(it -> {
                    if (envService.isCenter() || Objects.equals(it.getNodeId(), userOwnerId)) {
                        return NodeVO.from(it,
                                datatableManager.findByNodeId(it.getNodeId(), AbstractDatatableManager.DATA_VENDOR_MANUAL),
                                nodeManager.findBySrcNodeId(it.getNodeId()),
                                resultRepository.countByNodeId(it.getNodeId()));
                    } else {
                        return NodeVO.from(it, null, null, null);
                    }
                }).collect(Collectors.toList());
    }

    @Override
    public List<NodeBaseInfoVO> listOtherNodeBaseInfo(@NotBlank final String oneselfNodeId) {
        List<NodeDO> nodeDOList = nodeRepository.findAll();
        return nodeDOList.stream().filter(nodeDO -> !Objects.equals(nodeDO.getNodeId(), oneselfNodeId)).map(NodeBaseInfoVO::from).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createNode(CreateNodeRequest request) {
        String nodeId = nodeManager.createNode(CreateNodeParam.builder().name(request.getName()).mode(request.getMode()).build());

        NodeUserCreateRequest nodeUserCreateParam = new NodeUserCreateRequest();
        nodeUserCreateParam.setNodeId(nodeId);
        nodeUserCreateParam.setName(nodeId);
        nodeUserCreateParam.setPasswordHash(Sha256Utils.hash(nodeId + "12#$qwER"));
        nodeUserService.create(nodeUserCreateParam);

        return nodeId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createP2pNode(P2pCreateNodeRequest request) {
        // check address equals
        nodeManager.checkSrcAddressAndDstAddressEquals(request.getSrcNetAddress(), request.getDstNetAddress());
        CreateNodeParam param = CreateNodeParam.builder()
                .nodeId(request.getDstNodeId())
                .name(request.getName())
                .mode(request.getMode())
                .netAddress(request.getDstNetAddress())
                .certText(request.getCertText())
                .masterNodeId(StringUtils.isBlank(request.getMasterNodeId()) ? masterNodeId : request.getMasterNodeId())
                .build();
        // check node cert
        nodeManager.checkNodeCert(UserContext.getUser().getPlatformNodeId(), param);
        // create node
        String nodeId = nodeManager.createP2pNode(param);
        // create node route
        nodeRouterService.createNodeRouter(CreateNodeRouterRequest.builder()
                .srcNodeId(UserContext.getUser().getPlatformNodeId())
                .dstNodeId(request.getDstNodeId())
                .srcNetAddress(request.getSrcNetAddress())
                .dstNetAddress(request.getDstNetAddress())
                .build());
        LOGGER.debug("createP2pNode return {}", nodeId);
        return nodeId;
    }

    @Override
    public void deleteNode(String nodeId) {
        nodeManager.deleteNode(nodeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteP2pNode(String routerId) {
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId(routerId);
        if (ObjectUtils.isEmpty(nodeRouteDO)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist " + routerId);
        }
        // delete route first, avoid NPE
        nodeRouteManager.deleteNodeRoute(routerId);
        // delete node id is srcNodeId this version. later version will be dstNodeId
        nodeManager.deleteNode(nodeRouteDO.getSrcNodeId());
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
        // teeNodeId maybe blank
        String teeDomainId = StringUtils.isBlank(request.getTeeNodeId()) ? teeNodeId : request.getTeeNodeId();
        // query push to tee map
        Map<String, List<TeeNodeDatatableManagementDO>> pullFromTeeInfoMap = getPullFromTeeInfos(request.getNodeId(), teeDomainId,
                nodeResultDTOList.getNodeResultDTOList().stream().map(NodeResultDTO::getDomainDataId).toList());
        List<NodeResultsVO> nodeResultsVOList = nodeResultDTOList.getNodeResultDTOList().stream().map(
                it -> {
                    // query management data object
                    List<TeeNodeDatatableManagementDO> pullFromTeeInfos = pullFromTeeInfoMap.get(it.getDomainDataId());
                    TeeNodeDatatableManagementDO managementDO = CollectionUtils.isEmpty(pullFromTeeInfos) ? null : pullFromTeeInfos.stream()
                            .sorted(Comparator.comparing(TeeNodeDatatableManagementDO::getGmtCreate).reversed()).toList().get(0);
                    return NodeResultsVO.fromNodeResultDTO(it, managementDO);
                }
        ).toList();
        return NodeResultsListVO.builder()
                .totalResultNums(nodeResultDTOList.getTotalResultNums())
                .nodeResultsVOList(nodeResultsVOList)
                .build();
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
        return NodeResultDetailVO.builder().nodeResultsVO(NodeResultsVO.fromNodeResultDTO(nodeResult, null))
                .graphDetailVO(projectJob.getGraph())
                .tableColumnVOList(
                        datatableDTO.get().getSchema().stream().map(TableColumnVO::from).collect(Collectors.toList()))
                .output(graphService.getResultOutputVO(request.getNodeId(), request.getDomainDataId()))
                .datasource(datasource)
                .build();
    }

    @Override
    public List<NodeVO> listTeeNode() {
        List<NodeDTO> nodeDTOList = nodeManager.listTeeNode();
        return nodeDTOList.stream()
                .map(it -> NodeVO.from(it, null, null, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<NodeVO> listCooperatingNode(String nodeId) {
        List<NodeDTO> nodeDTOList = nodeManager.listCooperatingNode(nodeId);
        return nodeDTOList.stream()
                .map(it -> NodeVO.from(it,
                        datatableManager.findByNodeId(it.getNodeId(), AbstractDatatableManager.DATA_VENDOR_MANUAL),
                        nodeManager.findBySrcNodeId(it.getNodeId()), resultRepository.countByNodeId(it.getNodeId())))
                .collect(Collectors.toList());
    }

    @Override
    public void initialNode() {
        nodeManager.initialNode(envService.getPlatformNodeId());
    }

    /**
     * Query tee node datatable management data object list by nodeId, teeNodeId and datatableIds then collect to Map
     *
     * @param nodeId       target nodeId
     * @param teeNodeId    target teeNodeId
     * @param datatableIds target datatableId list
     * @return Map of datatableId and tee node datatable management data object list
     */
    private Map<String, List<TeeNodeDatatableManagementDO>> getPullFromTeeInfos(String nodeId, String teeNodeId, List<String> datatableIds) {
        // batch query push to tee job list by datatableIds
        List<TeeNodeDatatableManagementDO> managementList = teeNodeDatatableManagementRepository
                .findAllByNodeIdAndTeeNodeIdAndDatatableIdsAndKind(nodeId, teeNodeId, datatableIds, TeeJobKind.Pull);
        if (CollectionUtils.isEmpty(managementList)) {
            return Collections.emptyMap();
        }
        // collect by datatable id
        return managementList.stream().collect(Collectors.groupingBy(it -> it.getUpk().getDatatableId()));
    }
}


