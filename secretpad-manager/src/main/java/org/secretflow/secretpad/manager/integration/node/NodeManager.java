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
import org.secretflow.secretpad.common.errorcode.NodeErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.manager.integration.model.*;
import org.secretflow.secretpad.manager.kuscia.grpc.KusciaDomainRpc;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domain;
import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.DomainServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
    private final ProjectGraphRepository projectGraphRepository;
    private final ProjectRepository projectRepository;
    private final ProjectJobRepository projectJobRepository;
    private final ProjectNodeRepository projectNodeRepository;
    private final DomainDataServiceGrpc.DomainDataServiceBlockingStub domainDataStub;

    private final DomainServiceGrpc.DomainServiceBlockingStub domainServiceBlockingStub;
    private final KusciaDomainRpc kusciaDomainRpc;

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
        return nodeRepository.findAll().stream().map(this::fillByGrpcDomainQuery).collect(Collectors.toList());
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
        NodeDO nodeDO = NodeDO.builder().controlNodeId(nodeId).nodeId(nodeId).netAddress(nodeId + ":1080")
                .type(DomainConstants.DomainTypeEnum.normal.name()).name(param.getName()).build();
        nodeRepository.save(nodeDO);
        Domain.CreateDomainRequest request = Domain.CreateDomainRequest.newBuilder().setDomainId(nodeId)
                .setAuthCenter(
                        Domain.AuthCenter.newBuilder().setAuthenticationType("Token").setTokenGenMethod("UID-RSA-GEN").build())
                .build();
        try {
            kusciaDomainRpc.createDomain(request);
        } catch (Exception e) {
            throw SecretpadException.of(NodeErrorCode.NODE_CREATE_ERROR, e);
        }
        return nodeId;
    }

    @Override
    @Transactional
    public void deleteNode(String nodeId) {
        // check whether the node exists first
        check(nodeId);
        if (!checkNodeExists(nodeId)) {
            nodeRepository.deleteById(nodeId);
            nodeRepository.flush();
            LOGGER.error("node {} is not exist! but delete anyway", nodeId);
            return;
        }
        List<ProjectNodeDO> projectNodeList = projectNodeRepository.findByNodeId(nodeId);
        if (!CollectionUtils.isEmpty(projectNodeList)) {
            LOGGER.error("node {} has job running!", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, "node have job running");
        }
        // call the api interface to delete node
        Domain.DeleteDomainRequest request = Domain.DeleteDomainRequest.newBuilder().setDomainId(nodeId).build();
        nodeRepository.deleteById(nodeId);
        nodeRouteRepository.deleteByDstNodeId(nodeId);
        nodeRouteRepository.deleteByDstNodeId(nodeId);
        try {
            kusciaDomainRpc.deleteDomain(request);
        } catch (Exception e) {
            throw SecretpadException.of(NodeErrorCode.NODE_DELETE_ERROR, e);
        }
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
        Map<String, Domaindata.DomainData> domainDataMap = queryDomainDataMap(allProjectResults);
        List<NodeResultDTO> nodeResultDTOList = new ArrayList<>(allProjectResults.size());
        for (ProjectResultDO projectResultDO : allProjectResults) {
            Domaindata.DomainData domainData = domainDataMap.get(projectResultDO.getUpk().getRefId());
            if (domainData == null) {
                LOGGER.warn("Ref_id {} in project_result table does not exits in kuscia api.", projectResultDO.getUpk().getRefId());
            } else {
                nodeResultDTOList.add(
                        findNodeResult(domainData, projectResultDO)
                );
            }
        }
        LOGGER.info("Filter node results with name filter = {}", param.getNameFilter());
        nodeResultDTOList = filterByName(nodeResultDTOList, param.getNameFilter());
        int totalNodeResultNums = nodeResultDTOList.size();
        LOGGER.info("After filter, total node result nums = {}, now try to sort with the time sorting rule = {}", totalNodeResultNums, param.getTimeSortingRule());
        sortNodeResults(nodeResultDTOList, param.getTimeSortingRule());
        LOGGER.info("After sort node results, total nod result nums = {}, now paging.", nodeResultDTOList.size());
        int startIndex = param.getPageSize() * (param.getPageNumber() - 1);
        if (startIndex > nodeResultDTOList.size()) {
            throw SecretpadException.of(SystemErrorCode.OUT_OF_RANGE_ERROR, "page start index > results length.");
        }
        int endIndex = Math.min(startIndex + param.getPageSize(), nodeResultDTOList.size());
        nodeResultDTOList = nodeResultDTOList.subList(startIndex, endIndex);
        LOGGER.info("After page the, there are {} showing node results, from {}, to {}.", nodeResultDTOList.size(), startIndex, endIndex);
        return NodeResultListDTO.builder()
                .nodeResultDTOList(nodeResultDTOList)
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
        Domaindata.QueryDomainDataResponse queryDomainDataResponse = domainDataStub.queryDomainData(request);
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
            LOGGER.error("Cannot find node by nodeId {}.", nodeId);
            throw SecretpadException.of(NodeErrorCode.NODE_NOT_EXIST_ERROR);
        }
        Domain.QueryDomainRequest queryDomainRequest =
                Domain.QueryDomainRequest.newBuilder().setDomainId(nodeId).build();
        Domain.QueryDomainResponse response = kusciaDomainRpc.queryDomain(queryDomainRequest);
        List<Domain.DeployTokenStatus> deployTokenStatusesList = response.getData().getDeployTokenStatusesList();
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
        return fillByGrpcDomainQuery(nodeDO);
    }

    private NodeDTO fillByGrpcDomainQuery(NodeDO nodeDO) {
        NodeDTO nodeDTO = NodeDTO.fromDo(nodeDO);
        Domain.QueryDomainRequest queryDomainRequest =
                Domain.QueryDomainRequest.newBuilder().setDomainId(nodeDO.getNodeId()).build();
        Domain.QueryDomainResponse response = kusciaDomainRpc.queryDomainNoCheck(queryDomainRequest);
        if (response.getStatus().getCode() == 0) {
            nodeDTO.setNodeStatus(DomainConstants.DomainStatusEnum.NotReady.name());
            if (ObjectUtils.isNotEmpty(response.getData())) {
                String cert = response.getData().getCert();
                String role = response.getData().getRole();
                List<Domain.NodeStatus> nodeStatusesList = response.getData().getNodeStatusesList();
                if (ObjectUtils.isNotEmpty(nodeStatusesList)) {
                    List<NodeInstanceDTO> nodeInstanceDTOList = nodeStatusesList.stream().map(NodeInstanceDTO::formDomainNodeStatus).collect(Collectors.toList());
                    nodeDTO.setNodeInstances(nodeInstanceDTOList);
                    nodeDTO.setCert(StringUtils.isEmpty(cert) ? DomainConstants.DomainCertConfigEnum.unconfirmed.name() : DomainConstants.DomainCertConfigEnum.configured.name());
                    nodeDTO.setNodeRole(role);
                    nodeInstanceDTOList.forEach(s -> {
                        if (s.getStatus().equals(DomainConstants.DomainStatusEnum.Ready.name())) {
                            nodeDTO.setNodeStatus(s.getStatus());
                        }
                    });
                }
                List<Domain.DeployTokenStatus> deployTokenStatusesList = response.getData().getDeployTokenStatusesList();
                if (ObjectUtils.isNotEmpty(deployTokenStatusesList)) {
                    String token = nodeDTO.getToken();
                    if (StringUtils.isEmpty(token)) {
                        deployTokenStatusesList.forEach(t -> {
                            if (t.getState().equals(DomainConstants.TokenStatusEnum.unused.name())) {
                                nodeDO.setToken(t.getToken());
                                nodeRepository.save(nodeDO);
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
        return nodeDTO;
    }

    /**
     * Check whether node exists in domain service stub
     *
     * @param nodeId nodeId
     * @return whether node exists in domain service stub
     */
    @Override
    public boolean checkNodeExists(String nodeId) {
        Domain.QueryDomainRequest request = Domain.QueryDomainRequest.newBuilder()
                .setDomainId(nodeId)
                .build();
        Domain.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        return response.getStatus().getCode() == 0;
    }

    @Override
    public boolean checkNodeReady(String nodeId) {
        Domain.QueryDomainRequest request = Domain.QueryDomainRequest.newBuilder()
                .setDomainId(nodeId)
                .build();
        Domain.QueryDomainResponse response = domainServiceBlockingStub.queryDomain(request);
        log.info("checkNodeReady response  {} {} ", nodeId, response);
        if (response.getStatus().getCode() != 0) {
            return false;
        }
        List<Domain.NodeStatus> nodeStatusesList = response.getData().getNodeStatusesList();
        log.info("checkNodeReady  {} {} ", nodeId, nodeStatusesList);
        boolean flag = false;
        for (Domain.NodeStatus nodeStatus : nodeStatusesList) {
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
        if (nameFilter == null || "".equals(nameFilter)) {
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
     * Query all domain data by nodeId and RefId then collect to Map
     *
     * @param allProjectResults all project results
     * @return Map of domain data id and domain data
     */
    private Map<String, Domaindata.DomainData> queryDomainDataMap(List<ProjectResultDO> allProjectResults) {
        if (CollectionUtils.isEmpty(allProjectResults)) {
            return Collections.emptyMap();
        }
        // query all node domain data from apiLite
        LOGGER.info("afy len of all projectResult = {}", allProjectResults.size());
        LOGGER.info("afy try to batch query node id = {}, refid = {}", allProjectResults.get(0).getUpk().getNodeId(), allProjectResults.get(0).getUpk().getRefId());
        Domaindata.BatchQueryDomainDataRequest batchQueryDomainDataRequest = Domaindata.BatchQueryDomainDataRequest.newBuilder()
                .addAllData(
                        allProjectResults.stream().map(
                                it -> Domaindata.QueryDomainDataRequestData.newBuilder()
                                        .setDomainId(it.getUpk().getNodeId())
                                        .setDomaindataId(it.getUpk().getRefId())
                                        .build()
                        ).collect(Collectors.toList())
                )
                .build();
        Domaindata.BatchQueryDomainDataResponse response = domainDataStub.batchQueryDomainData(batchQueryDomainDataRequest);
        if (response.getStatus().getCode() != 0) {
            LOGGER.error("lock up from apilite failed: code={}, message={}, request={}",
                    response.getStatus().getCode(), response.getStatus().getMessage(), JsonUtils.toJSONString(allProjectResults));
            throw SecretpadException.of(NodeErrorCode.DOMAIN_DATA_NOT_EXISTS);
        }
        List<Domaindata.DomainData> dataList = response.getData().getDomaindataListList().stream().filter(
                it -> !"".equals(it.getDomaindataId())
        ).collect(Collectors.toList());
        return dataList.stream().collect(
                Collectors.toMap(
                        Domaindata.DomainData::getDomaindataId,
                        Function.identity()
                )
        );
    }

    /**
     * Find NodeResultDTO by domainData and projectResultDO
     *
     * @param domainData
     * @param projectResultDO
     * @return NodeResultDTO
     */
    private NodeResultDTO findNodeResult(Domaindata.DomainData domainData, ProjectResultDO projectResultDO) {
        // query projectDO from project table
        Optional<ProjectDO> projectDO = projectRepository.findById(projectResultDO.getUpk().getProjectId());
        if (!projectDO.isPresent()) {
            // the results are displayed normally even after the item has been deleted
            LOGGER.warn("The project is deleted, and the result will not show any project info and train flow info.");
            return NodeResultDTO.builder()
                    .domainDataId(domainData.getDomaindataId())
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
            Optional<ProjectGraphDO> projectGraphDO = projectGraphRepository.findByGraphId(projectJobDO.get().getGraphId(), projectDO.get().getProjectId());
            if (projectGraphDO.isPresent()) {
                return NodeResultDTO.builder()
                        .domainDataId(domainData.getDomaindataId())
                        .resultName(domainData.getDomaindataId())
                        .resultKind(domainData.getType())
                        .sourceProjectId(projectDO.get().getProjectId())
                        .sourceProjectName(projectDO.get().getName())
                        .trainFlow(projectGraphDO.get().getName())
                        .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                        .relativeUri(domainData.getRelativeUri())
                        .jobId(projectResultDO.getJobId())
                        .build();
            } else {
                LOGGER.warn("Cannot find graph when list node results.");
            }
        } else {
            LOGGER.warn("Cannot find project job when list node results.");
        }
        return NodeResultDTO.builder()
                .domainDataId(domainData.getDomaindataId())
                .resultName(domainData.getDomaindataId())
                .resultKind(domainData.getType())
                .sourceProjectId(projectDO.get().getProjectId())
                .sourceProjectName(projectDO.get().getName())
                .trainFlow(null)
                .gmtCreate(DateTimes.toRfc3339(projectResultDO.getGmtCreate()))
                .relativeUri(domainData.getRelativeUri())
                .jobId(projectResultDO.getJobId())
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

    private String genDomainId() {
        return UUIDUtils.random(8);
    }

}
