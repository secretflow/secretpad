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

import org.secretflow.secretpad.common.constant.role.RoleCodeConstants;
import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.ConcurrentErrorCode;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.NodeRouteErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.manager.integration.datasource.AbstractDatasourceManager;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.*;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.*;
import org.secretflow.secretpad.service.model.auth.NodeUserCreateRequest;
import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.data.DataSourceVO;
import org.secretflow.secretpad.service.model.datatable.TableColumnVO;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.service.model.node.p2p.P2pCreateNodeRequest;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.project.ProjectJobVO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
    public DataService dataService;
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
    @Resource
    private InstService instService;
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
    private EnvService envService;

    @Resource
    private NodeRouterServiceImpl nodeRouterServiceimpl;

    @Autowired
    private TeeNodeDatatableManagementRepository teeNodeDatatableManagementRepository;

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;

    @Resource
    private SysUserPermissionRelRepository permissionRelRepository;

    @Value("${tee.domain-id:tee}")
    private String teeNodeId;

    @Value("${secretpad.node-id}")
    private String masterNodeId;
    @Qualifier("kusciaApiFutureTaskThreadPool")
    @Autowired
    private Executor kusciaApiFutureThreadPool;

    @Resource
    private AbstractDatasourceManager datasourceManager;

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
        LOGGER.info("request = {}", JsonUtils.toJSONString(request));
        // check address equals
        nodeManager.checkSrcAddressAndDstAddressEquals(request.getSrcNetAddress(), request.getDstNetAddress());
        CreateNodeParam param = CreateNodeParam.builder()
                .dstNodeId(request.getDstNodeId())
                .srcNodeId(request.getSrcNodeId())
                .name(request.getName())
                .mode(request.getMode())
                .netAddress(request.getDstNetAddress())
                .certText(request.getCertText())
                .masterNodeId(StringUtils.isBlank(request.getMasterNodeId()) ? masterNodeId : request.getMasterNodeId())
                .instId(request.getDstInstId())
                .instName(request.getDstInstName())
                .build();
        // check node cert
        nodeManager.checkNodeCert(UserContext.getUser().getPlatformNodeId(), param);
        // create node
        String nodeId = nodeManager.createP2pNode(param);
        // create node route
        nodeRouterService.createNodeRouter(CreateNodeRouterRequest.builder()
                .srcNodeId(request.getSrcNodeId())
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
    public void deleteP2pNodeRoute(String routerId) {

        //verify node route exist or can be delete,if ture,return the result
        NodeRouteDO nodeRouteDO = verifyAndFind(routerId);

        //check If There Are Tasks Running

        nodeRouterServiceimpl.validateNoRunningJobs(nodeRouteDO);

        //find channel node id by source and destination node id
        String channelNodeId = findChannelNodeId(nodeRouteDO);

        //delete node route full duplex in kuscia
        deleteP2PNodeRouteFullDuplexInKuscia(nodeRouteDO, channelNodeId);

        //delete node in kuscia
        deleteP2pNodeInKuscia(nodeRouteDO, channelNodeId);

        //delete node route in db
        deleteP2pNodeRouteInDB(routerId);

        //delete node route relationship in db
        deleteNodeRouteRelationInDb(nodeRouteDO, channelNodeId);
    }

    /**
     * check there are other node route relevance to the srcNode
     * ex: inst contains [a1,a2],a1 and a2 both has node route to bob(another inst node)
     * while we delete node route a1<->bob,if we want to delete node bob in db,we should make sure a2<->bob not exist
     *
     * @param nodeRouteDO
     * @param channelNodeId
     */
    private void deleteNodeRouteRelationInDb(NodeRouteDO nodeRouteDO, String channelNodeId) {
        List<NodeRouteDO> nodeRouteDOS = nodeRouteRepository.findBySrcNodeId(nodeRouteDO.getSrcNodeId());
        LOGGER.info("deleteNodeRouteRelationInDb sourceNodeId = {}", nodeRouteDO.getSrcNodeId());
        if (CollectionUtils.isEmpty(nodeRouteDOS)) {
            nodeManager.checkNodeExists(nodeRouteDO.getSrcNodeId(), channelNodeId);
            nodeRepository.deleteById(nodeRouteDO.getSrcNodeId());
            SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
            upk.setUserKey(nodeRouteDO.getSrcNodeId());
            upk.setTargetCode(RoleCodeConstants.P2P_NODE);
            permissionRelRepository.deleteById(upk);
            nodeRepository.flush();
            LOGGER.info("deleteNodeRouteRelationInDb relations success");
        } else {
            LOGGER.info("deleteNodeRouteRelationInDb,node route has related!,related node nodeRouteDOS id is {}", JsonUtils.toJSONString(nodeRouteDOS));
        }
    }

    private void deleteP2pNodeInKuscia(NodeRouteDO nodeRouteDO, String channelNodeId) {
        try {
            nodeRouteManager.deleteNodeInKuscia(nodeRouteDO.getSrcNodeId(), channelNodeId);
        } catch (Exception e) {
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, e);
        }
    }

    private void deleteP2pNodeRouteInDB(String routerId) {
        nodeRouteRepository.deleteById(routerId);
    }

    private void deleteP2PNodeRouteFullDuplexInKuscia(NodeRouteDO nodeRouteDO, String channelNodeId) {
        String dstNodeId = nodeRouteDO.getDstNodeId();
        String srcNodeId = nodeRouteDO.getSrcNodeId();

        if (nodeRouteManager.checkDomainRouterExistsInKuscia(srcNodeId, dstNodeId, channelNodeId)) {
            nodeRouteManager.deleteNodeRouteInKuscia(srcNodeId, dstNodeId, channelNodeId);
        }
        if (nodeRouteManager.checkDomainRouterExistsInKuscia(dstNodeId, srcNodeId, channelNodeId)) {
            nodeRouteManager.deleteNodeRouteInKuscia(dstNodeId, srcNodeId, channelNodeId);
        }
    }

    private NodeRouteDO verifyAndFind(String routerId) {
        LOGGER.info("routerId = {}", routerId);
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId(routerId);
        if (ObjectUtils.isEmpty(nodeRouteDO)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_NOT_EXIST_ERROR,
                    "route not exist " + routerId);
        }

        List<ProjectDO> projectList = projectRepository.findByStatus(ProjectStatusEnum.REVIEWING.getCode());
        if (!CollectionUtils.isEmpty(projectList)) {
            List<ProjectApprovalConfigDO> projectApprovalConfigList = projectApprovalConfigRepository
                    .findByProjectIdsAndType(projectList.stream().map(ProjectDO::getProjectId).toList(), "PROJECT_CREATE");
            if (!CollectionUtils.isEmpty(projectApprovalConfigList) && projectApprovalConfigList.stream().anyMatch(t ->
                    t.getParties().contains(nodeRouteDO.getSrcNodeId()))) {
                throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node have project reviewing");
            }
        }
        return nodeRouteDO;
    }

    private String findChannelNodeId(NodeRouteDO nodeRouteDO) {
        Set<String> instNodeIds = instService.listNodeIds();
        String channelNodeId = null;
        if (instNodeIds.contains(nodeRouteDO.getSrcNodeId())) {
            channelNodeId = nodeRouteDO.getSrcNodeId();
        } else if (instNodeIds.contains(nodeRouteDO.getDstNodeId())) {
            channelNodeId = nodeRouteDO.getDstNodeId();
        }
        if (StringUtils.isNotBlank(channelNodeId)) {
            LOGGER.info("channelNodeId is {}", channelNodeId);
            if (!StringUtils.equals(channelNodeId, nodeRouteDO.getDstNodeId())) {
                LOGGER.info("channelNodeId does not match destNodeId,destNodeId is {}", nodeRouteDO.getDstNodeId());
            }
            return channelNodeId;
        }
        String errMsg = String.format("Both nodes--> sourceNode %s and destNode %s are not within the inst", nodeRouteDO.getSrcNodeId(), nodeRouteDO.getDstNodeId());
        throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR, errMsg);
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
    public AllNodeResultsListVO listAllNodeResults(ListNodeResultRequest request) {

        List<String> nodeIds = new ArrayList<>();
        if (envService.isAutonomy()) {
            nodeIds.addAll(nodeManager.listReadyNodeByIds(InstServiceImpl.INST_ID, request.getNodeNamesFilter()));
        } else {
            nodeIds.add(request.getOwnerId());
        }

        List<NodeAllResultsVO> nodeAllResultsVOList = new CopyOnWriteArrayList<>();
        AtomicInteger totalNodeResultNum = new AtomicInteger();
        ConcurrentMap<String, String> errorMap = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = nodeIds.stream()
                .map(nodeId -> CompletableFuture.supplyAsync(() -> {
                    ListNodeResultRequest nodeResultRequest = createNodeRequest(request, nodeId);
                    return listResults(nodeResultRequest);
                }, kusciaApiFutureThreadPool).handle((nodeResultsListVO, ex) -> {
                    if (ex != null) {
                        // Handle the exception: log it and put it in the error map.
                        LOGGER.error("Error processing nodeId {}: {}", nodeId, ex.getMessage(), ex);
                        errorMap.put(nodeId, ex.getMessage());
                        return null;
                    } else {
                        return nodeResultsListVO;
                    }
                }).thenAccept(nodeResultsListVO -> {
                    if (nodeResultsListVO != null) {
                        NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
                        if (ObjectUtils.isEmpty(nodeDO)) {
                            String errorMsg = "Cannot find node by nodeId " + nodeId;
                            LOGGER.error(errorMsg);
                            errorMap.put(nodeId, errorMsg);
                            return;
                        }
                        totalNodeResultNum.addAndGet(nodeResultsListVO.getTotalResultNums());
                        nodeResultsListVO.getNodeResultsVOList().forEach(nodeResultsVO -> {
                            nodeAllResultsVOList.add(NodeAllResultsVO.builder()
                                    .nodeResultsVO(nodeResultsVO)
                                    .nodeId(nodeId)
                                    .nodeName(nodeDO.getName())
                                    .build());
                        });
                    }
                })).toList();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException detected", e);
            Thread.currentThread().interrupt();
            throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
        } catch (TimeoutException e) {
            LOGGER.error("TimeoutException detected", e);
            throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
        } catch (ExecutionException e) {
            LOGGER.error("ExecutionException detected", e);
            throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
        }

        List<NodeAllResultsVO> rangeVOList = PageUtils.rangeList(nodeAllResultsVOList, request.getPageSize(), request.getPageNumber());

        //fill datatable info
        List<Domaindata.DomainData> domainDataList = datatableManager.findByIdGroup(rangeVOList.stream().map(it -> DatatableDTO.NodeDatatableId.from(it.getNodeId(), it.getNodeResultsVO().getDomainDataId())).toList(), (currentNodeId, extra) -> currentNodeId);
        Map<String, Domaindata.DomainData> dataIdMap = domainDataList.stream().collect(Collectors.toMap(it -> it.getDomaindataId(), Function.identity()));

        //multi thread datasource
        Set<DatasourceDTO.NodeDatasourceId> datasourceIds = domainDataList.stream().map(data -> DatasourceDTO.NodeDatasourceId.from(data.getDomainId(), data.getDatasourceId())).collect(Collectors.toSet());
        Map<String, String> datasourceIdTypeMap = datasourceIds.stream()
                .map(sourceId -> {
                    Optional<DatasourceDTO> result = datasourceManager.findById(sourceId);
                    return result.isPresent() ? result.get() : null;
                }).filter(Objects::nonNull)
                .collect(Collectors.toMap(dto -> dto.getDatasourceId(), dto -> dto.getType(), (a, b) -> a));

        rangeVOList.stream().forEach(vo -> {
            String domainDataId = vo.getNodeResultsVO().getDomainDataId();
            Domaindata.DomainData domainData = dataIdMap.get(domainDataId);
            if(domainData!=null){
                vo.getNodeResultsVO().setRelativeUri(domainData.getRelativeUri());
                vo.getNodeResultsVO().setDatasourceId(domainData.getDatasourceId());
                vo.getNodeResultsVO().setDatatableType(domainData.getType());
                vo.getNodeResultsVO().setDatasourceType(DataSourceTypeEnum.kuscia2platform(datasourceIdTypeMap.get(domainData.getDatasourceId())));
            }
        });


        return AllNodeResultsListVO.builder()
                .nodeAllResultsVOList(rangeVOList)
                .totalNodeResultNums(totalNodeResultNum.get())
                .build();
    }

    @Override
    public NodeResultsListVO listResults(ListNodeResultRequest request) {
        NodeResultListDTO nodeResultDTOList = nodeManager.listResult(ListResultParam.builder()
                .nodeId(request.getOwnerId()).pageSize(request.getPageSize()).pageNumber(request.getPageNumber())
                .kindFilters(request.getKindFilters()).dataVendorFilter(request.getDataVendorFilter())
                .nameFilter(request.getNameFilter()).timeSortingRule(request.getTimeSortingRule()).build());
        // teeNodeId maybe blank
        String teeDomainId = StringUtils.isBlank(request.getTeeNodeId()) ? teeNodeId : request.getTeeNodeId();
        // query push to tee map
        Map<String, List<TeeNodeDatatableManagementDO>> pullFromTeeInfoMap = getPullFromTeeInfos(request.getOwnerId(), teeDomainId,
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
    public void initialNode(String instName) {
        nodeManager.initialNode(envService.getPlatformNodeId(), instName);
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

    private ListNodeResultRequest createNodeRequest(ListNodeResultRequest request, String nodeId) {
        return ListNodeResultRequest.builder()
                .ownerId(nodeId)
                .teeNodeId(request.getTeeNodeId())
                .pageSize(PageUtils.DEFAULT_PAGE_SIZE)
                .pageNumber(PageUtils.DEFAULT_PAGE_NUM)
                .kindFilters(request.getKindFilters())
                .dataVendorFilter(request.getDataVendorFilter())
                .nameFilter(request.getNameFilter())
                .timeSortingRule(request.getTimeSortingRule())
                .build();
    }

    @Override
    public void updateNodeMasterNodeId(String instId) {
        nodeRepository.updateMasterNodeIdByInstid(instId, masterNodeId);
    }

    @Override
    public List<String> findInstIdsForNodes(List<String> nodeIds) {
        return nodeRepository.findInstIdsByNodeIds(nodeIds);
    }
}


