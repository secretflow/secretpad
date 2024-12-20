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

package org.secretflow.secretpad.manager.integration.node;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.role.RoleCodeConstants;
import org.secretflow.secretpad.common.enums.*;
import org.secretflow.secretpad.common.errorcode.*;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.datasource.AbstractDatasourceManager;
import org.secretflow.secretpad.manager.integration.model.*;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.persistence.repository.*;

import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Manager node operation
 *
 * @author xiaonan
 * @date 2023/05/23
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class NodeManager extends AbstractNodeManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
    private final static String ASCENDING_SORT_RULE = "ascending";
    private final static String DESCENDING_SORT_RULE = "descending";
    private final NodeRepository nodeRepository;
    private final NodeRouteRepository nodeRouteRepository;
    private final ProjectResultRepository projectResultRepository;
    private final ProjectRepository projectRepository;
    private final ProjectJobRepository projectJobRepository;
    private final ProjectNodeRepository projectNodeRepository;
    private final ProjectApprovalConfigRepository projectApprovalConfigRepository;

    private final KusciaGrpcClientAdapter kusciaGrpcClientAdapter;

    private final SysUserPermissionRelRepository permissionRelRepository;

    private final InstRepository instRepository;

    @Value("${kusciaapi.protocol:tls}")
    private String protocol;
    @Value("${secretpad.node-id}")
    private String localNodeId;

    @Value("${secretpad.platform-type}")
    private String platformType;

    @Resource
    private AbstractDatasourceManager datasourceManager;

    private void check(String nodeId) {
        List<NodeDO> byType = nodeRepository.findByType(DomainConstants.DomainTypeEnum.embedded.name());
        byType.forEach(nodeDO -> {
            if (nodeId.equals(nodeDO.getNodeId())) {
                throw SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, "default node can not delete");
            }
        });
    }

    /**
     * List all nodes
     *
     * @return NodeDTO list
     */
    @Override
    public List<NodeDTO> listNode() {
        List<NodeDTO> nodeDTOS = nodeRepository.findAll().stream().map(NodeDTO::fromDo).toList();
        return this.addNodeStatusByGrpcBatchQuery(nodeDTOS);
    }

    private NodeDTO getNodeDto(NodeDO nodeDO) {
        if (PlatformTypeEnum.AUTONOMY.name().equals(platformType)) {
            return getNodeDTOP2P(nodeDO);
        }
        return fillByGrpcDomainQuery(nodeDO);
    }

    /**
     * query inst too many times
     */
    private NodeDTO getNodeDTOP2P(NodeDO nodeDO) {
        NodeDTO nodeDTO = NodeDTO.fromDo(nodeDO);
        Optional<InstDO> optionalInstDO = instRepository.findById(nodeDO.getInstId());
        if (optionalInstDO.isEmpty()) {
            throw SecretpadException.of(InstErrorCode.INST_NOT_EXISTS);
        }
        InstDO instDO = optionalInstDO.get();
        nodeDTO.setInstId(instDO.getInstId());
        nodeDTO.setInstName(instDO.getName());
        nodeDTO.setNodeStatus(DomainConstants.DomainStatusEnum.NotReady.name());
        boolean isMainNode = StringUtils.equals(nodeDTO.getNodeId(), localNodeId);
        nodeDTO.setIsMainNode(isMainNode);
        if (isMainNode) {
            nodeDTO.setAllowDeletion(false);
        } else {
            Set<NodeRouteDO> routes = nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeDTO.getNodeId());
            nodeDTO.setAllowDeletion(CollectionUtils.isEmpty(routes));
        }
        fillByGrpcDomainQueryP2p(nodeDTO);
        return nodeDTO;
    }


    @Override
    public List<NodeDTO> listNode(String instId) {
        List<NodeDO> nodeDOs = nodeRepository.findByInstId(instId);
        if (CollectionUtils.isEmpty(nodeDOs)) {
            return new ArrayList<>();

        }
        //TODO: cpu later to thread pool
        return nodeDOs.parallelStream().map(this::getNodeDTOP2P).toList();
    }

    @Override
    public List<NodeDTO> listReadyNode(String instId) {
        return listNode(instId).stream().filter(nodeDTO -> DomainConstants.DomainStatusEnum.Ready.name().equals(nodeDTO.getNodeStatus())).toList();
    }

    @Override
    public List<NodeDTO> listReadyNodeByNames(String instId, List<String> nodeNames) {
        List<NodeDTO> nodeDTOS = listReadyNode(instId);
        if (CollectionUtils.isEmpty(nodeDTOS) || CollectionUtils.isEmpty(nodeNames)) {
            return nodeDTOS;
        }
        return nodeDTOS.stream().filter(nodeDTO -> nodeNames.contains(nodeDTO.getNodeName())).collect(Collectors.toList());
    }

    @Override
    public List<String> listReadyNodeByIds(String instId, List<String> nodeIds) {
        List<NodeDTO> nodeDTOS = listReadyNode(instId);
        if (CollectionUtils.isEmpty(nodeDTOS) || CollectionUtils.isEmpty(nodeIds)) {
            return nodeDTOS.stream().map(NodeDTO::getNodeId).toList();
        }
        return nodeDTOS.stream().map(NodeDTO::getNodeId).filter(nodeIds::contains).collect(Collectors.toList());

    }

    @Override
    public List<NodeDTO> listCooperatingNode(String nodeId) {

        List<NodeRouteDO> nodeRouteList = nodeRouteRepository.findBySrcNodeId(nodeId);

        Set<String> cooperatingNodeIdSet;
        if (CollectionUtils.isEmpty(nodeRouteList)) {
            cooperatingNodeIdSet = Collections.singleton(nodeId);
        } else {
            cooperatingNodeIdSet = nodeRouteList.stream().map(NodeRouteDO::getDstNodeId).collect(Collectors.toSet());
            // add oneself
            cooperatingNodeIdSet.add(nodeId);
        }
        return nodeRepository.findByNodeIdIn(cooperatingNodeIdSet).stream().map(this::fillByGrpcDomainQuery).collect(Collectors.toList());
    }

    /**
     * Create node
     *
     * @param param create parma
     * @return nodeId
     */
    @Override
    @Transactional
    public String createNode(CreateNodeParam param) {
        String nodeId = genDomainId();
        // the creation is successful. insert into the database
        NodeDO nodeDO = NodeDO.builder().controlNodeId(nodeId).nodeId(nodeId).netAddress(nodeId + ":1080").instId("")
                .type(DomainConstants.DomainTypeEnum.normal.name()).mode(param.getMode()).name(param.getName()).build();
        nodeRepository.save(nodeDO);

        SysUserPermissionRelDO sysUserPermission = new SysUserPermissionRelDO();
        sysUserPermission.setUserType(PermissionUserTypeEnum.NODE);
        sysUserPermission.setTargetType(PermissionTargetTypeEnum.ROLE);
        SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
        upk.setUserKey(nodeId);
        upk.setTargetCode(RoleCodeConstants.EDGE_NODE);
        sysUserPermission.setUpk(upk);
        permissionRelRepository.save(sysUserPermission);

        DomainOuterClass.CreateDomainRequest request = DomainOuterClass.CreateDomainRequest.newBuilder().setDomainId(nodeId)
                .setAuthCenter(
                        DomainOuterClass.AuthCenter.newBuilder().setAuthenticationType("Token").setTokenGenMethod("UID-RSA-GEN").build())
                .build();
        try {
            kusciaGrpcClientAdapter.createDomain(request);
        } catch (Exception e) {
            throw SecretpadException.of(NodeErrorCode.NODE_CREATE_ERROR, e);
        }
        return nodeId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createP2pNode(CreateNodeParam param) {
        String nodeId = param.getDstNodeId();
        NodeDO nodeDO;
        // query if exists deleted record, avoid insert
        Optional<NodeDO> deletednodeOptional = nodeRepository.findDeletedRecordByNodeId(nodeId);
        if (deletednodeOptional.isPresent()) {
            LOGGER.info("exists deleted record, nodeId = {}", nodeId);
            nodeDO = deletednodeOptional.get();
            nodeDO.setName(param.getName());
            nodeDO.setMode(param.getMode());
            nodeDO.setMasterNodeId(param.getMasterNodeId());
            nodeDO.setNetAddress(param.getNetAddress());
            nodeDO.setIsDeleted(Boolean.FALSE);
            nodeDO.setInstId(param.getInstId());
        } else {
            LOGGER.info("create new record, nodeId = {}", nodeId);
            nodeDO = NodeDO.builder().controlNodeId(nodeId).nodeId(nodeId).netAddress(param.getNetAddress())
                    .masterNodeId(param.getMasterNodeId()).type(DomainConstants.DomainTypeEnum.normal.name())
                    .mode(param.getMode()).name(param.getName()).instId(param.getInstId()).build();
        }
        try {
            nodeRepository.save(nodeDO);
        } catch (Exception e) {
            throw SecretpadException.of(NodeErrorCode.NODE_CREATE_ERROR, e, nodeId + " node create fail in db :" + e.getMessage());
        }

        SysUserPermissionRelDO sysUserPermission = new SysUserPermissionRelDO();
        sysUserPermission.setUserType(PermissionUserTypeEnum.NODE);
        sysUserPermission.setTargetType(PermissionTargetTypeEnum.ROLE);
        SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
        upk.setUserKey(nodeId);
        upk.setTargetCode(RoleCodeConstants.P2P_NODE);
        sysUserPermission.setUpk(upk);
        permissionRelRepository.save(sysUserPermission);

        createInst(param.getInstId(), param.getInstName());
        DomainOuterClass.CreateDomainRequest request = DomainOuterClass.CreateDomainRequest.newBuilder().setDomainId(nodeId)
                .setAuthCenter(
                        DomainOuterClass.AuthCenter.newBuilder().setAuthenticationType("Token").setTokenGenMethod("RSA-GEN").build())
                .setRole("partner")
                .setCert(param.getCertText())
                .build();
        DomainOuterClass.CreateDomainResponse domain = kusciaGrpcClientAdapter.createDomain(request, param.getSrcNodeId());
        if (domain.getStatus().getCode() != 0) {
            throw SecretpadException.of(NodeErrorCode.NODE_CREATE_ERROR, nodeId + " node create fail in kuscia :" + domain.getStatus().getMessage());
        }
        return nodeId;
    }

    @Override
    @Transactional
    public NodeDTO createP2PNodeForInst(CreateNodeParam param) {
        String nodeId = genDomainId();
        NodeDO nodeDO = NodeDO.builder()
                .instId(param.getInstId())
                .controlNodeId(nodeId)
                .nodeId(nodeId)
                .netAddress(nodeId + ":1080")
                .type(DomainConstants.DomainTypeEnum.normal.name())
                //mpc only
                .mode(param.getMode())
                .name(param.getName())
                .masterNodeId(localNodeId)
                .build();
        nodeDO.setInstToken(generateInstToken(nodeDO));
        nodeRepository.save(nodeDO);
        //remove code about SysUserPermissionRelDO
        return NodeDTO.fromDo(nodeDO);
    }

    /**
     * need  then add
     */
    private String generateInstToken(NodeDO nodeDO) {
        return TokenUtil.sign(nodeDO.getInstId(), nodeDO.getNodeId());
    }


    @Override
    public String generateInstToken(String instId, String nodeId) {

        NodeDO nodeDO = nodeRepository.findOneByInstId(instId, nodeId);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("node not find by nodeId={}", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }

        nodeDO.setInstToken(generateInstToken(nodeDO));
        nodeDO.setInstTokenState(NodeInstTokenStateEnum.UNUSED);
        nodeRepository.saveAndFlush(nodeDO);

        return nodeDO.getInstToken();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNode(String nodeId) {
        SysUserPermissionRelDO.UPK upk = new SysUserPermissionRelDO.UPK();
        upk.setUserKey(nodeId);
        // if platformType is AUTONOMY, targetCode is different
        upk.setTargetCode(UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY) ? RoleCodeConstants.P2P_NODE : RoleCodeConstants.EDGE_NODE);

        // check whether the node exists first
        check(nodeId);
        if (!checkNodeExists(nodeId)) {
            nodeRepository.deleteById(nodeId);
            permissionRelRepository.deleteById(upk);
            nodeRepository.flush();
            LOGGER.error("node {} is not exist! but delete anyway", nodeId);
            return;
        }
        List<ProjectNodeDO> projectNodeList = projectNodeRepository.findByNodeId(nodeId);
        if (!CollectionUtils.isEmpty(projectNodeList)) {
            LOGGER.error("node {} has job running!", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node have job running");
        }
        // if project vote exists and status is reviewing, can not be deleted
        if (UserContext.getUser().getPlatformType().equals(PlatformTypeEnum.AUTONOMY)) {
            List<ProjectDO> projectList = projectRepository.findByStatus(ProjectStatusEnum.REVIEWING.getCode());
            if (!CollectionUtils.isEmpty(projectList)) {
                List<ProjectApprovalConfigDO> projectApprovalConfigList = projectApprovalConfigRepository
                        .findByProjectIdsAndType(projectList.stream().map(ProjectDO::getProjectId).toList(), "PROJECT_CREATE");
                if (!CollectionUtils.isEmpty(projectApprovalConfigList) && projectApprovalConfigList.stream().anyMatch(t ->
                        t.getParties().contains(nodeId))) {
                    throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node have project reviewing");
                }
            }
        }
        // call the api interface to delete node
        DomainOuterClass.DeleteDomainRequest request = DomainOuterClass.DeleteDomainRequest.newBuilder().setDomainId(nodeId).build();
        nodeRepository.deleteById(nodeId);
        permissionRelRepository.deleteById(upk);
        nodeRouteRepository.deleteBySrcNodeId(nodeId);
        nodeRouteRepository.deleteByDstNodeId(nodeId);
        try {
            kusciaGrpcClientAdapter.deleteDomain(request);
        } catch (Exception e) {
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, e);
        }
    }

    /**
     * only p2p
     * invoke idempotent
     */
    @Override
    public void deleteNode(String inst, String nodeId) {
        LOGGER.info("delete inst={},nodeId={}", inst, nodeId);
        checkBeforeDelete(inst, nodeId);
        deleteKusciaDomain(nodeId);
        nodeRepository.deleteAuthentic(nodeId);
        LOGGER.info("delete finish inst={},nodeId={}", inst, nodeId);
    }


    private void checkBeforeDelete(String instId, String nodeId) {
        /* local node check */
        if (StringUtils.equalsIgnoreCase(localNodeId, nodeId)) {
            LOGGER.error("localNode forbid delete,node={}", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "localNode forbid delete");
        }

        NodeDO nodeDO = nodeRepository.findOneByInstId(instId, nodeId);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("node not find by instId={} ,nodeId={}", instId, nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }

        /* route check */
        Set<NodeRouteDO> routes = nodeRouteRepository.findBySrcNodeIdOrDstNodeId(nodeId);
        if (!CollectionUtils.isEmpty(routes)) {
            LOGGER.error("node route is not empty ,node={} ", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node route is not empty");
        }
        /* project check */
        List<ProjectNodeDO> projectNodeList = projectNodeRepository.findByNodeId(nodeId);
        if (!CollectionUtils.isEmpty(projectNodeList)) {
            LOGGER.error("node  has job running! node={}", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node have job running");
        }

        /* reuse approval check */
        List<ProjectDO> projectList = projectRepository.findByStatus(ProjectStatusEnum.REVIEWING.getCode());
        if (!CollectionUtils.isEmpty(projectList)) {
            List<ProjectApprovalConfigDO> approvalList = projectApprovalConfigRepository
                    .findByProjectIdsAndType(projectList.stream().map(ProjectDO::getProjectId).toList(), "PROJECT_CREATE");
            if (!CollectionUtils.isEmpty(approvalList) &&
                    approvalList.stream().anyMatch(t -> t.getParties().contains(nodeId))) {
                throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node have project reviewing");
            }
        }
    }


    /**
     * kuscia delete
     * unregister
     */
    private void deleteKusciaDomain(String nodeId) {

        boolean domainRegistered = kusciaGrpcClientAdapter.isDomainRegistered(nodeId);
        if (!domainRegistered) {
            LOGGER.info("no registered delete success, nodeId={}", nodeId);
            return;
        }

        DomainOuterClass.QueryDomainRequest query = DomainOuterClass.QueryDomainRequest.
                newBuilder().setDomainId(nodeId).build();
        DomainOuterClass.QueryDomainResponse queryDomainResponse = kusciaGrpcClientAdapter.queryDomain(query, nodeId);
        boolean isDomainExist = queryDomainResponse.getStatus().getCode() == 0;
        if (!isDomainExist) {
            LOGGER.info("domain not exist, nodeId={}", nodeId);
            return;
        }

        DomainOuterClass.DeleteDomainRequest request = DomainOuterClass.DeleteDomainRequest.newBuilder().setDomainId(nodeId).build();
        DomainOuterClass.DeleteDomainResponse deleteResponse = kusciaGrpcClientAdapter.deleteDomain(request, nodeId);
        LOGGER.warn("deleteDomain result={}", deleteResponse);
        if (deleteResponse.getStatus().getCode() != 0) {
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "kuscia delete domain failed");
        }

        // unregister
        kusciaGrpcClientAdapter.unregisterDomain(nodeId);
        LOGGER.info("unregisterDomain finish, nodeId={}", nodeId);
    }


    @Override
    public List<NodeRouteDTO> findBySrcNodeId(String nodeId) {
        return nodeRouteRepository.findBySrcNodeId(nodeId).stream().map(NodeRouteDTO::fromDo).collect(Collectors.toList());
    }

    @Override
    public NodeResultListDTO listResult(ListResultParam param) {
        // find all node results from project result table first
        LOGGER.info("Try select all project results in project_result table with node id = {}", param.getNodeId());
        List<ProjectResultDO> allProjectResults = projectResultRepository.findByNodeId(param.getNodeId());
        LOGGER.info("Filter project result with kind filter = {}", param.getKindFilters());
        allProjectResults = filterByKind(allProjectResults, param.getKindFilters());
        LOGGER.info("Querying the domain data map with every ref_id in all project results.");
        //merge db like join
        List<NodeResultDTO> nodeResultDTOS = allProjectResults.stream().map(this::mergeNodeResult).toList();

        LOGGER.info("Filter node results with name filter = {}", param.getNameFilter());
        nodeResultDTOS = filterByName(nodeResultDTOS, param.getNameFilter());
        int totalNodeResultNums = nodeResultDTOS.size();
        LOGGER.info("After filter, total node result nums = {}, now try to sort with the time sorting rule = {}", totalNodeResultNums, param.getTimeSortingRule());
        nodeResultDTOS = new ArrayList<>(nodeResultDTOS);
        sortNodeResults(nodeResultDTOS, param.getTimeSortingRule());

        return NodeResultListDTO.builder()
                .nodeResultDTOList(nodeResultDTOS)
                .totalResultNums(totalNodeResultNums)
                .build();
    }

    @Override
    public NodeResultDTO getNodeResult(String nodeId, String domainDataId) {
        LOGGER.info("Get node result with nodeId = {}, domain data id = {}", nodeId, domainDataId);
        Domaindata.QueryDomainDataRequest request = Domaindata.QueryDomainDataRequest.newBuilder()
                .setData(
                        Domaindata.QueryDomainDataRequestData.newBuilder()
                                .setDomainId(nodeId)
                                .setDomaindataId(domainDataId)
                                .build()
                )
                .build();
        Domaindata.QueryDomainDataResponse queryDomainDataResponse = PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(platformType))
                ? kusciaGrpcClientAdapter.queryDomainData(request, nodeId)
                : kusciaGrpcClientAdapter.queryDomainData(request);
        if (queryDomainDataResponse.getStatus().getCode() != 0) {
            LOGGER.error("query domain data from kusciaapi failed: code={}, message={}, nodeId={}, domainDataId={}",
                    queryDomainDataResponse.getStatus().getCode(), queryDomainDataResponse.getStatus().getMessage(), nodeId, domainDataId);
            throw SecretpadException.of(DatatableErrorCode.QUERY_DATATABLE_FAILED);
        }
        Optional<ProjectResultDO> projectResultDO = projectResultRepository.findByNodeIdAndRefId(nodeId, domainDataId);
        if (projectResultDO.isEmpty()) {
            LOGGER.error("Cannot found result in project_result table, but it can be queried with kuscia api.");
            throw SecretpadException.of(ProjectErrorCode.PROJECT_RESULT_NOT_FOUND);
        }
        return findNodeResult(queryDomainDataResponse.getData(), projectResultDO.get());
    }

    @Override
    public NodeDTO refreshNode(String nodeId) {
        return getNode(nodeId);
    }

    @Override
    public NodeTokenDTO getNodeToken(String nodeId, boolean refresh) {
        AtomicReference<NodeTokenDTO> nodeTokenDTO = new AtomicReference<>(new NodeTokenDTO());
        NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("getNodeToken Cannot find node by nodeId {}.", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        DomainOuterClass.QueryDomainRequest queryDomainRequest =
                DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId(nodeId).build();
        DomainOuterClass.QueryDomainResponse response = kusciaGrpcClientAdapter.queryDomain(queryDomainRequest);
        List<DomainOuterClass.DeployTokenStatus> deployTokenStatusesList = response.getData().getDeployTokenStatusesList();
        if (CollectionUtils.isEmpty(deployTokenStatusesList)) {
            throw SecretpadException.of(NodeErrorCode.NODE_TOKEN_IS_EMPTY_ERROR, "kuscia return empty token");
        }
        String token = nodeDO.getToken();
        boolean tokenEmpty = StringUtils.isEmpty(token);
        deployTokenStatusesList.forEach(t -> {
            if (refresh || tokenEmpty) {
                if (t.getState().equals(DomainConstants.TokenStatusEnum.unused.name())) {
                    nodeDO.setToken(t.getToken());
                    nodeRepository.save(nodeDO);
                    nodeTokenDTO.set(NodeTokenDTO.fromDeployTokenStatus(t));
                }
            } else {
                if (token.equals(t.getToken())) {
                    nodeTokenDTO.set(NodeTokenDTO.fromDeployTokenStatus(t));
                }
            }
        });
        return nodeTokenDTO.get();
    }

    @Override
    public NodeDTO getNode(String nodeId) {
        NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
        if (ObjectUtils.isEmpty(nodeDO)) {
            LOGGER.error("Cannot find node by nodeId {}.", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        return getNodeDto(nodeDO);
    }


    @Override
    public String getCert(String nodeId) {
        DomainOuterClass.BatchQueryDomainRequest queryDomainRequest =
                DomainOuterClass.BatchQueryDomainRequest.newBuilder().addAllDomainIds(Lists.newArrayList(nodeId)).build();
        DomainOuterClass.BatchQueryDomainResponse response = kusciaGrpcClientAdapter.batchQueryDomain(queryDomainRequest);
        if (response.getStatus().getCode() != 0) {
            return "";
        }
        if (ObjectUtils.isEmpty(response.getData())) {
            return "";
        }
        return response.getData().getDomainsList().get(0).getCert();
    }

    public String getCerts(String nodeId, String channelNodeId) {
        DomainOuterClass.BatchQueryDomainRequest queryDomainRequest =
                DomainOuterClass.BatchQueryDomainRequest.newBuilder().addAllDomainIds(Lists.newArrayList(nodeId)).build();
        DomainOuterClass.BatchQueryDomainResponse response = kusciaGrpcClientAdapter.batchQueryDomain(queryDomainRequest, channelNodeId);
        if (response.getStatus().getCode() != 0) {
            return "";
        }
        if (ObjectUtils.isEmpty(response.getData())) {
            return "";
        }
        return response.getData().getDomainsList().get(0).getCert();
    }

    /**
     * p2p no need token write back
     */
    private void fillByGrpcDomainQueryP2p(NodeDTO nodeDTO) {
        boolean registered = kusciaGrpcClientAdapter.isDomainRegistered(nodeDTO.getNodeId());
        DomainOuterClass.QueryDomainRequest queryDomainRequest =
                DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId(nodeDTO.getNodeId()).build();
        if (!registered) {
            LOGGER.warn("node domain not registered, nodeId={}", nodeDTO.getNodeId());
            DomainOuterClass.QueryDomainResponse response = kusciaGrpcClientAdapter.queryDomain(queryDomainRequest);
            if (response.getStatus().getCode() == 0) {
                nodeDTO.setCertText(response.getData().getCert());
            }
            return;
        }


        DomainOuterClass.QueryDomainResponse response = kusciaGrpcClientAdapter.queryDomain(queryDomainRequest, nodeDTO.getNodeId());
        fillNodeDTOByKResp(nodeDTO, response);
    }


    /**
     * compatible
     */
    private NodeDTO fillByGrpcDomainQuery(NodeDO nodeDO) {
        NodeDTO nodeDTO = NodeDTO.fromDo(nodeDO);
        fillByGrpcDomainQuery(nodeDTO);
        /* token write back */
        if (StringUtils.isEmpty(nodeDO.getToken()) && !StringUtils.isEmpty(nodeDTO.getToken())) {
            nodeDO.setToken(nodeDTO.getToken());
            nodeRepository.save(nodeDO);
        }
        return nodeDTO;
    }


    private void fillByGrpcDomainQuery(NodeDTO nodeDTO) {
        nodeDTO.setProtocol(protocol);
        DomainOuterClass.QueryDomainRequest queryDomainRequest =
                DomainOuterClass.QueryDomainRequest.newBuilder().setDomainId(nodeDTO.getNodeId()).build();
        DomainOuterClass.QueryDomainResponse response = kusciaGrpcClientAdapter.queryDomain(queryDomainRequest);
        fillNodeDTOByKResp(nodeDTO, response);
    }


    private void fillNodeDTOByKResp(NodeDTO nodeDTO, DomainOuterClass.QueryDomainResponse response) {
        if (ObjectUtils.isNotEmpty(response) && response.getStatus().getCode() == 0) {
            nodeDTO.setNodeStatus(DomainConstants.DomainStatusEnum.NotReady.name());
            if (ObjectUtils.isNotEmpty(response.getData())) {
                String cert = response.getData().getCert();
                String role = response.getData().getRole();
                // cert, role are no judgment required for node_statuses
                nodeDTO.setCert(StringUtils.isEmpty(cert) ? DomainConstants.DomainCertConfigEnum.unconfirmed.name() : DomainConstants.DomainCertConfigEnum.configured.name());
                nodeDTO.setCertText(cert);
                nodeDTO.setNodeRole(role);
                List<DomainOuterClass.NodeStatus> nodeStatusesList = response.getData().getNodeStatusesList();
                if (ObjectUtils.isNotEmpty(nodeStatusesList)) {
                    List<NodeInstanceDTO> nodeInstanceDTOList = nodeStatusesList.stream().map(NodeInstanceDTO::formDomainNodeStatus).collect(Collectors.toList());
                    nodeDTO.setNodeInstances(nodeInstanceDTOList);
                    nodeInstanceDTOList.forEach(s -> {
                        if (s.getStatus().equals(DomainConstants.DomainStatusEnum.Ready.name())) {
                            nodeDTO.setNodeStatus(s.getStatus());
                        }
                    });
                }
                List<DomainOuterClass.DeployTokenStatus> deployTokenStatusesList = response.getData().getDeployTokenStatusesList();
                if (ObjectUtils.isNotEmpty(deployTokenStatusesList)) {
                    String token = nodeDTO.getToken();
                    if (StringUtils.isEmpty(token)) {
                        deployTokenStatusesList.forEach(t -> {
                            if (t.getState().equals(DomainConstants.TokenStatusEnum.unused.name())) {
                                nodeDTO.setToken(t.getToken());
                                nodeDTO.setTokenStatus(t.getState());
                            }
                        });
                    } else {
                        deployTokenStatusesList.forEach(t -> {
                            if (token.equals(t.getToken())) {
                                nodeDTO.setTokenStatus(t.getState());
                            }
                        });
                    }
                }
            }
        }
    }

    private List<NodeDTO> addNodeStatusByGrpcBatchQuery(List<NodeDTO> nodeList) {
        Set<String> nodeIdSet = nodeList.stream().map(NodeDTO::getNodeId).collect(Collectors.toSet());
        nodeIdSet.forEach(nodeId -> {

            DomainOuterClass.BatchQueryDomainRequest domainIds =
                    DomainOuterClass.BatchQueryDomainRequest.newBuilder().addDomainIds(nodeId).build();
            DomainOuterClass.BatchQueryDomainResponse domainStatusResponse = kusciaGrpcClientAdapter.batchQueryDomain(domainIds);

            if (domainStatusResponse.getStatus().getCode() == 0) {

                List<DomainOuterClass.Domain> domainsList = domainStatusResponse.getData().getDomainsList();
                Map<String, List<DomainOuterClass.NodeStatus>> domainId2StatusListMap = domainsList.stream().collect(Collectors.toMap(DomainOuterClass.Domain::getDomainId, DomainOuterClass.Domain::getNodeStatusesList));

                nodeList.forEach(node -> {
                    List<DomainOuterClass.NodeStatus> nodeStatusList = domainId2StatusListMap.getOrDefault(node.getNodeId(), Collections.emptyList());
                    List<NodeInstanceDTO> nodeInstanceDTOList = nodeStatusList.stream().map(NodeInstanceDTO::formDomainNodeStatus).toList();

                    nodeInstanceDTOList.forEach(s -> {
                        if (Objects.equals(s.getStatus(), DomainConstants.DomainStatusEnum.Ready.name())) {
                            node.setNodeStatus(s.getStatus());
                        }
                    });
                });
            }

        });
        return nodeList;

    }

    /**
     * Check whether node exists in domain service stub
     *
     * @param nodeId nodeId
     * @return whether node exists in domain service stub
     */
    @Override
    public boolean checkNodeExists(String nodeId) {
        return checkNodeExists(nodeId, null);
    }

    @Override
    public boolean checkNodeExists(String nodeId, String channelNodeId) {
        DomainOuterClass.QueryDomainRequest request = DomainOuterClass.QueryDomainRequest.newBuilder()
                .setDomainId(nodeId)
                .build();
        DomainOuterClass.QueryDomainResponse response;
        if (StringUtils.isBlank(channelNodeId)) {
            response = kusciaGrpcClientAdapter.queryDomain(request);

        } else {
            response = kusciaGrpcClientAdapter.queryDomain(request, channelNodeId);

        }
        return response.getStatus().getCode() == 0;
    }

    @Override
    public boolean checkNodeReady(String nodeId) {
        DomainOuterClass.QueryDomainRequest request = DomainOuterClass.QueryDomainRequest.newBuilder()
                .setDomainId(nodeId)
                .build();
        DomainOuterClass.QueryDomainResponse response = kusciaGrpcClientAdapter.queryDomain(request);
        log.info("checkNodeReady response  {} {} ", nodeId, response);
        if (response.getStatus().getCode() != 0) {
            return false;
        }
        List<DomainOuterClass.NodeStatus> nodeStatusesList = response.getData().getNodeStatusesList();
        log.info("checkNodeReady  {} {} ", nodeId, nodeStatusesList);
        boolean flag = false;
        for (DomainOuterClass.NodeStatus nodeStatus : nodeStatusesList) {
            if (nodeStatus.getStatus().equals(DomainConstants.DomainStatusEnum.Ready.name())) {
                flag = true;
            }
        }
        if (!flag) {
            log.warn("nodeStatus not ready {}", nodeStatusesList);
            return false;
        }
        return true;
    }

    @Override
    public List<NodeDTO> listTeeNode() {
        List<Integer> teeModes = new ArrayList<>();
        teeModes.add(DomainConstants.DomainModeEnum.tee.code);
        teeModes.add(DomainConstants.DomainModeEnum.teeAndMpc.code);
        return nodeRepository.findByModeIn(teeModes).stream().map(this::fillByGrpcDomainQuery).collect(Collectors.toList());
    }

    @Override
    public void checkSrcAddressAndDstAddressEquals(String srcNodeAddress, String dstNodeAddress) {
        if (StringUtils.isNotBlank(dstNodeAddress) && StringUtils.equals(srcNodeAddress, dstNodeAddress)) {
            throw SecretpadException.of(NodeRouteErrorCode.NODE_ROUTE_CONFIG_ERROR,
                    "");
        }
    }

    @Override
    public void checkNodeCert(String nodeId, CreateNodeParam request) {
        if (StringUtils.isBlank(request.getCertText()) || getCerts(nodeId, nodeId).equals(request.getCertText()) || nodeId.equals(request.getDstNodeId())) {
            throw SecretpadException.of(NodeErrorCode.NODE_CERT_CONFIG_ERROR, "");
        }
    }

    @Override
    public void initialNode(String nodeId, String instName) {
        String instId;
        String netAddr = "127.0.0.1:28080";
        if (nodeRepository.existsById(nodeId)) {
            log.info("node {} exists", nodeId);
            NodeDO nodeDO = nodeRepository.findByNodeId(nodeId);
            if (StringUtils.isNotBlank(nodeDO.getNetAddress())) {
                netAddr = nodeDO.getNetAddress();
            }
            if (StringUtils.isNotBlank(nodeDO.getInstId())) {
                log.info("instId replace");
                //new version,reinstall
                instId = nodeDO.getInstId();
            } else {
                //old version, reinstall,should create new inst
                instId = createInst(instName);
            }
            nodeRepository.deleteAuthentic(nodeId);
        } else {
            //new version,install a new node,create inst
            log.info("instId create,instName={}", instName);
            instId = createInst(instName);
        }
        NodeDO nodeBuild = NodeDO.builder().nodeId(nodeId).instId(instId)
                .description(nodeId).type(DomainConstants.DomainTypeEnum.primary.name())
                .mode(1).name(nodeId).netAddress(netAddr).controlNodeId(nodeId)
                .instId(instId).protocol(protocol)
                .masterNodeId(localNodeId).build();
        nodeRepository.saveAndFlush(nodeBuild);
    }

    private String createInst(String instName) {
        String instId = UUIDUtils.random(8);
        return createInst(instId, instName);
    }

    private String createInst(String instId, String instName) {
        InstDO instDO = InstDO.builder().instId(instId).name(instName).build();
        instRepository.saveAndFlush(instDO);
        return instId;
    }

    /**
     * Filter project result list by kind conditions
     *
     * @param projectResultDOList projectResultDOList
     * @param kindFilters         kindFilters
     * @return filtered project result list
     */
    private List<ProjectResultDO> filterByKind(List<ProjectResultDO> projectResultDOList, List<String> kindFilters) {
        if (CollectionUtils.isEmpty(kindFilters)) {
            return projectResultDOList;
        }
        return projectResultDOList.stream().filter(
                it -> {
                    for (String kindFilter : kindFilters) {
                        if (it.getUpk().getKind().getName().equalsIgnoreCase(kindFilter)) {
                            return true;
                        }
                    }
                    return false;
                }
        ).collect(Collectors.toList());
    }

    /**
     * Fuzzy filtering based on the name of the presentation, project and training flow
     * Note that the filtering here, although called by name, is actually based on the domain data id
     *
     * @return node result list
     */
    private List<NodeResultDTO> filterByName(
            List<NodeResultDTO> nodeResultDTOList,
            String nameFilter
    ) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return nodeResultDTOList;
        }
        LOGGER.info("afy got name filter (actual id) {}", nameFilter);
        return nodeResultDTOList.stream().filter(
                it -> (it.getDomainDataId() != null && it.getDomainDataId().contains(nameFilter)) ||
                        (it.getSourceProjectName() != null && it.getSourceProjectName().contains(nameFilter)) ||
                        (it.getTrainFlow() != null && it.getTrainFlow().contains(nameFilter))
        ).collect(Collectors.toList());

    }

    /**
     * do not query domain data at beginning ,do it at last
     *
     * @param projectResultDO
     * @return
     */
    private NodeResultDTO mergeNodeResult(ProjectResultDO projectResultDO) {
        Optional<ProjectDO> projectDO = projectRepository.findById(projectResultDO.getUpk().getProjectId());
        if (projectDO.isEmpty()) {
            // the results are displayed normally even after the item has been deleted
            LOGGER.warn("The project is deleted, and the result will not show any project info and train flow info.");
            return NodeResultDTO.builder()
                    .domainDataId(projectResultDO.getUpk().getRefId())
                    .sourceProjectId(null)
                    .sourceProjectName(null)
                    .trainFlow(null)
                    .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                    .jobId(projectResultDO.getJobId())
                    .build();
        }
        // query project graph list from project graph table
        Optional<ProjectJobDO> projectJobDO = projectJobRepository.findByJobId(projectResultDO.getJobId());
        if (projectJobDO.isPresent()) {
            // Use graph name saved in project_job.name
            return NodeResultDTO.builder()
                    .domainDataId(projectResultDO.getUpk().getRefId())
                    .sourceProjectId(projectDO.get().getProjectId())
                    .sourceProjectName(projectDO.get().getName())
                    .trainFlow(projectJobDO.get().getName())
                    .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                    .jobId(projectResultDO.getJobId())
                    .computeMode(projectDO.get().getComputeMode())
                    .build();
        } else {
            LOGGER.warn("Cannot find project job when list node results.");
        }
        return NodeResultDTO.builder()
                .domainDataId(projectResultDO.getUpk().getRefId())
                .sourceProjectId(projectDO.get().getProjectId())
                .sourceProjectName(projectDO.get().getName())
                .trainFlow(null)
                .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                .jobId(projectResultDO.getJobId())
                .computeMode(projectDO.get().getComputeMode())
                .build();
    }


    /**
     * Find NodeResultDTO by domainData and projectResultDO
     *
     * @param domainData
     * @param projectResultDO
     * @return NodeResultDTO
     */
    private NodeResultDTO findNodeResult(Domaindata.DomainData domainData, ProjectResultDO projectResultDO) {

        DatasourceDTO.NodeDatasourceId from = DatasourceDTO.NodeDatasourceId.from(domainData.getDomainId(), domainData.getDatasourceId());
        Optional<DatasourceDTO> datasourceDTO = datasourceManager.findById(from);
        if (datasourceDTO.isEmpty()) {
            LOGGER.warn("findNodeResult find datasource fail, from={}", JsonUtils.toJSONString(from));
        }

        // query projectDO from project table
        Optional<ProjectDO> projectDO = projectRepository.findById(projectResultDO.getUpk().getProjectId());
        if (projectDO.isEmpty()) {
            // the results are displayed normally even after the item has been deleted
            LOGGER.warn("The project is deleted, and the result will not show any project info and train flow info.");
            return NodeResultDTO.builder()
                    .domainDataId(domainData.getDomaindataId())
                    .datasourceId(datasourceDTO.get().getDatasourceId())
                    .datasourceType(DataSourceTypeEnum.kuscia2platform(datasourceDTO.get().getType()))
                    .resultName(domainData.getDomaindataId())
                    .resultKind(domainData.getType())
                    .sourceProjectId(null)
                    .sourceProjectName(null)
                    .trainFlow(null)
                    .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                    .relativeUri(domainData.getRelativeUri())
                    .jobId(projectResultDO.getJobId())
                    .build();
        }
        // query project graph list from project graph table
        Optional<ProjectJobDO> projectJobDO = projectJobRepository.findByJobId(projectResultDO.getJobId());
        if (projectJobDO.isPresent()) {
            // Use graph name saved in project_job.name
            return NodeResultDTO.builder()
                    .domainDataId(domainData.getDomaindataId())
                    .datasourceId(datasourceDTO.get().getDatasourceId())
                    .datasourceType(DataSourceTypeEnum.kuscia2platform(datasourceDTO.get().getType()))
                    .resultName(domainData.getDomaindataId())
                    .resultKind(domainData.getType())
                    .sourceProjectId(projectDO.get().getProjectId())
                    .sourceProjectName(projectDO.get().getName())
                    .trainFlow(projectJobDO.get().getName())
                    .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                    .relativeUri(domainData.getRelativeUri())
                    .jobId(projectResultDO.getJobId())
                    .computeMode(projectDO.get().getComputeMode())
                    .build();
        } else {
            LOGGER.warn("Cannot find project job when list node results.");
        }
        return NodeResultDTO.builder()
                .domainDataId(domainData.getDomaindataId())
                .datasourceId(datasourceDTO.get().getDatasourceId())
                .datasourceType(DataSourceTypeEnum.kuscia2platform(datasourceDTO.get().getType()))
                .resultName(domainData.getDomaindataId())
                .resultKind(domainData.getType())
                .sourceProjectId(projectDO.get().getProjectId())
                .sourceProjectName(projectDO.get().getName())
                .trainFlow(null)
                .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                .relativeUri(domainData.getRelativeUri())
                .jobId(projectResultDO.getJobId())
                .computeMode(projectDO.get().getComputeMode())
                .build();
    }


    /**
     * Sort node results by timeSortingRule
     *
     * @param nodeResultDTOList
     * @param timeSortingRule
     */
    private void sortNodeResults(List<NodeResultDTO> nodeResultDTOList, String timeSortingRule) {
        if (ASCENDING_SORT_RULE.equalsIgnoreCase(timeSortingRule)) {
            nodeResultDTOList.sort(
                    (o1, o2) -> (int) (DateTimes.rfc3339ToLong(o1.getGmtCreate()) - DateTimes.rfc3339ToLong(o2.getGmtCreate()))
            );
        } else if (DESCENDING_SORT_RULE.equalsIgnoreCase(timeSortingRule)) {
            nodeResultDTOList.sort(
                    (o1, o2) -> (int) (DateTimes.rfc3339ToLong(o2.getGmtCreate()) - DateTimes.rfc3339ToLong(o1.getGmtCreate()))
            );
        }
    }

    protected String genDomainId() {
        return UUIDUtils.random(8);
    }


    /***/
    private boolean isLocalInstNode(String nodeId) {
        if (StringUtils.equalsIgnoreCase(nodeId, this.localNodeId)) {
            return true;
        }
        return P2pDataSyncProducerTemplate.nodeIds.contains(nodeId);
    }

    /*** only one */
    private boolean onlyOneNode() {
        return P2pDataSyncProducerTemplate.nodeIds.size() == 1;
    }

    /**
     * not found , return null
     * nodeId:input node
     * vo:relationship
     * match one
     */
    private static String searchTargetNode(String nodeId, ParticipantNodeInstVO bo) {
        /* search from initiator*/
        if (StringUtils.equals(nodeId, bo.getInitiatorNodeId())) {
            return bo.getInvitees().get(0).getInviteeId();
        }
        /* search from invitee*/
        Optional<ParticipantNodeInstVO.NodeInstVO> first = bo.getInvitees().stream()
                .filter(invitee -> StringUtils.equals(invitee.getInviteeId(), nodeId))
                .findFirst();
        if (first.isPresent()) {
            return bo.getInitiatorNodeId();
        }
        return null;
    }

    /**
     * match all
     */
    private static List<String> searchAllTargetNode(String nodeId, ParticipantNodeInstVO bo) {
        /* search from initiator*/
        if (StringUtils.equals(nodeId, bo.getInitiatorNodeId())) {
            return bo.getInvitees().stream().map(ParticipantNodeInstVO.NodeInstVO::getInviteeId).collect(Collectors.toList());
        }
        List<String> nodeIds = new ArrayList<>();
        /* search from invitee*/
        Optional<ParticipantNodeInstVO.NodeInstVO> first = bo.getInvitees().stream()
                .filter(invitee -> StringUtils.equals(invitee.getInviteeId(), nodeId))
                .findFirst();
        if (first.isPresent()) {
            nodeIds.add(bo.getInitiatorNodeId());
        }
        return nodeIds;
    }

    /**
     * search targetNode in project
     * [{"initiatorNodeId":"b1","invitees":[{"inviteeId":"a1"}]}]
     */
    @Override
    public String getTargetNodeId(String nodeId, String projectId) {
        if (PlatformTypeEnum.AUTONOMY.name().equals(this.platformType)) {
            //only one no need to search
            if (onlyOneNode()) {
                return localNodeId;
            }
            if (isLocalInstNode(nodeId)) {
                return nodeId;
            }
            // not found then search from config
            Optional<ProjectApprovalConfigDO> projectConDO = projectApprovalConfigRepository.findByProjectId(projectId);
            List<ParticipantNodeInstVO> nodeTree = projectConDO.get().getParticipantNodeInfo();
            Optional<String> targetNodeOpt = nodeTree
                    .stream().map(vo -> searchTargetNode(nodeId, vo))
                    .filter(node -> node != null && isLocalInstNode(node))
                    .findFirst();
            // not found use local as default
            return targetNodeOpt.orElseGet(() -> localNodeId);
        }
        return nodeId;
    }

    @Override
    public Set<String> getTargetNodeIds(String nodeId, String projectId) {
        if (PlatformTypeEnum.AUTONOMY.name().equals(this.platformType) && !isLocalInstNode(nodeId)) {
            Optional<ProjectApprovalConfigDO> projectConDO = projectApprovalConfigRepository.findByProjectId(projectId);
            List<ParticipantNodeInstVO> participantNodeInstVOS = projectConDO.get().getParticipantNodeInfo();
            return participantNodeInstVOS.stream().flatMap(vo -> searchAllTargetNode(nodeId, vo).stream()).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
}
