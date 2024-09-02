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

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.manager.integration.data.DataManager;
import org.secretflow.secretpad.manager.integration.datasource.DatasourceManager;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.DatasourceDTO;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.manager.integration.node.AbstractNodeManager;
import org.secretflow.secretpad.manager.integration.noderoute.AbstractNodeRouteManager;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.*;
import org.secretflow.secretpad.persistence.projection.ProjectJobStatus;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.GraphService;
import org.secretflow.secretpad.service.ProjectService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.graph.ComponentTools;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.graph.JobChain;
import org.secretflow.secretpad.service.model.graph.*;
import org.secretflow.secretpad.service.model.node.NodeSimpleInfo;
import org.secretflow.secretpad.service.model.project.GetProjectJobTaskOutputRequest;
import org.secretflow.secretpad.service.model.project.StopProjectJobTaskRequest;
import org.secretflow.secretpad.service.util.AutonomyNodeRouteUtil;
import org.secretflow.secretpad.persistence.model.ParticipantNodeInstVO;
import org.secretflow.secretpad.service.util.GraphUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.secretflow.spec.v1.AttrType;
import com.secretflow.spec.v1.ComponentDef;
import com.secretflow.spec.v1.Table;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.common.constant.ComponentConstants.BINNING_MODIFICATIONS_CODENAME;
import static org.secretflow.secretpad.common.constant.ComponentConstants.MODEL_PARAM_MODIFICATIONS_CODENAME;
import static org.secretflow.secretpad.service.constant.ComponentConstants.READ_DATA_DATATABLE;
import static org.secretflow.secretpad.service.constant.Constants.TEE_PROJECT_MODE;
import static org.secretflow.secretpad.service.util.JobUtils.genTaskOutputId;

/**
 * Graph service implementation class
 *
 * @author yansi
 * @date 2023/5/29
 */
@Slf4j
@Service
public class GraphServiceImpl implements GraphService {

    private static final Integer DEFAULT_INITIAL_INDEX = 32;
    @Autowired
    private ProjectGraphRepository graphRepository;
    @Autowired
    private ProjectGraphNodeRepository graphNodeRepository;
    @Autowired
    private ProjectJobTaskRepository taskRepository;
    @Autowired
    private ComponentService componentService;
    @Autowired
    private ProjectResultRepository resultRepository;
    @Autowired
    private ProjectReportRepository reportRepository;
    @Autowired
    private ProjectDatatableRepository datatableRepository;
    @Autowired
    private AbstractDatatableManager datatableManager;
    @Autowired
    private ProjectJobTaskLogRepository jobTaskLogRepository;
    @Autowired
    private ProjectJobRepository jobRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private JobChain jobChain;
    @Autowired
    private AbstractNodeManager nodeManager;
    @Autowired
    private AbstractNodeRouteManager nodeRouteManager;
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Resource
    private ProjectReadDtaRepository projectReadDtaRepository;
    @Autowired
    private ProjectReadDtaRepository readDtaRepository;
    @Resource
    private ProjectGraphDomainDatasourceServiceImpl projectGraphDomainDatasourceService;
    @Resource
    private DataManager dataManager;
    @Resource
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;
    @Value("${tee.domain-id:tee}")
    private String teeNodeId;
    @Value("${secretpad.platform-type}")
    private String plaformType;
    @Value("${secretpad.node-id}")
    private String localNodeId;
    @Autowired
    private EnvServiceImpl envServiceImpl;
    @Resource
    private DatasourceManager datasourceManager;
    @Resource
    private ProjectNodeRepository projectNodeRepository;

    @Override
    public Map<String, CompListVO> listComponents() {
        return componentService.listComponents();
    }

    @Override
    public ComponentDef getComponent(GetComponentRequest request) {
        return componentService.getComponent(GetComponentRequest.toComponentKey(request));
    }

    @Override
    public List<ComponentDef> batchGetComponent(List<GetComponentRequest> request) {
        return componentService.batchGetComponent(GetComponentRequest.toComponentKeyList(request));
    }

    @Override
    public Object listComponentI18n() {
        return componentService.listComponentI18n();
    }

    @Transactional
    @Override
    public CreateGraphVO createGraph(CreateGraphRequest request) {
        String projectId = request.getProjectId();
        String name = request.getName();
        String graphId = UUIDUtils.random(8);
        String ownerId = UserContext.getUser().getOwnerId();
        ProjectGraphDO graphDO = ProjectGraphDO.builder().upk(new ProjectGraphDO.UPK(projectId, graphId)).name(name).ownerId(ownerId).maxParallelism(1).build();
        List<GraphNode> nodes = request.getNodes();
        if (!CollectionUtils.isEmpty(nodes)) {
            graphDO.setNodes(nodes.stream().map(node -> GraphNode.toDO(projectId, graphId, node)).collect(Collectors.toList()));
        }
        List<GraphEdge> edges = request.getEdges();
        if (!CollectionUtils.isEmpty(edges)) {
            graphDO.setEdges(edges.stream().map(GraphEdge::toDO).collect(Collectors.toList()));
        }
        graphDO.setNodeMaxIndex(DEFAULT_INITIAL_INDEX);
        graphRepository.save(graphDO);
        projectGraphDomainDatasourceService.createGraphAndInitDefaultDataSource(request, graphId);
        return CreateGraphVO.builder().graphId(graphId).build();
    }

    @Override
    public void deleteGraph(DeleteGraphRequest request) {
        // check project graph owner
        ownerCheck(request.getProjectId(), request.getGraphId());
        graphRepository.deleteById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()));
    }

    @Override
    public List<GraphMetaVO> listGraph(ListGraphRequest request) {
        List<ProjectGraphDO> graphDOList = graphRepository.findByProjectId(request.getProjectId());
        if (!CollectionUtils.isEmpty(graphDOList)) {
            return graphDOList.stream().map(GraphMetaVO::fromDO).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public void updateGraphMeta(UpdateGraphMetaRequest request) {
        Optional<ProjectGraphDO> graphDOOptional = graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()));
        if (graphDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        if (PlatformTypeEnum.AUTONOMY.equals(UserContext.getUser().getPlatformType()) && !UserContext.getUser().getOwnerId().equalsIgnoreCase(graphDOOptional.get().getOwnerId())) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_OWNER_CANNOT_UPDATE);
        }
        ProjectGraphDO graphDO = graphDOOptional.get();
        graphDO.setName(request.getName());
        graphRepository.save(graphDO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void fullUpdateGraph(FullUpdateGraphRequest request) {
        String projectId = request.getProjectId();
        String graphId = request.getGraphId();
        // check project graph owner
        ProjectGraphDO graphDO = null;
        if (envServiceImpl.isCenter()) {
            graphDO = ownerCheck(projectId, graphId);
        } else {
            Optional<ProjectGraphDO> graphOptional = graphRepository.findById(new ProjectGraphDO.UPK(projectId, graphId));
            if (graphOptional.isEmpty()) {
                throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
            }
            graphDO = graphOptional.get();
        }
        if (!CollectionUtils.isEmpty(request.getNodes())) {
            graphDO.getNodes().clear();
            graphDO.setNodes(GraphNodeInfo.toDOList(projectId, graphId, request.getNodes()));
        }
        if (!CollectionUtils.isEmpty(request.getEdges())) {
            graphDO.setEdges(GraphEdge.toDOList(request.getEdges()));
        }
        if (Objects.nonNull(request.getMaxParallelism())) {
            graphDO.setMaxParallelism(request.getMaxParallelism());
        }
        graphRepository.save(graphDO);
        projectGraphDomainDatasourceService.updateProjectGraphDomainDatasourceDOByFullUpdateGraphRequest(request);
    }

    @Override
    public void updateGraphNode(UpdateGraphNodeRequest request) {
        String projectId = request.getProjectId();
        String graphId = request.getGraphId();
        // check project graph owner
        ownerCheck(projectId, graphId);
        String graphNodeId = request.getNode().getGraphNodeId();
        Optional<ProjectGraphNodeDO> graphNodeDOOptional = graphNodeRepository.findById(new ProjectGraphNodeDO.UPK(projectId, graphId, graphNodeId));
        if (graphNodeDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_NOT_EXISTS);
        }

        ProjectGraphNodeDO graphNodeDO = GraphNodeInfo.toDO(projectId, graphId, request.getNode());
        graphNodeRepository.save(graphNodeDO);
    }

    @Transactional
    @Override
    public GraphDetailVO getGraphDetail(GetGraphRequest request) {
        Optional<ProjectGraphDO> graphDOOptional = graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()));
        if (graphDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        ProjectGraphDO graphDO = graphDOOptional.get();
        List<GraphNodeStatusVO> nodeStatus = getLatestTaskStatus(graphDO).getNodes();
        GraphDetailVO graphDetailVO = GraphDetailVO.fromDO(graphDO, nodeStatus);
        graphDetailVO.setDataSourceConfig(projectGraphDomainDatasourceService.convertToGraphDetailVODataSourceConfig(request));
        return graphDetailVO;
    }

    @Override
    public GraphNodeOutputVO getGraphNodeOutput(GraphNodeOutputRequest request) {
        String projectId = request.getProjectId();
        Optional<ProjectTaskDO> taskDOOptional = taskRepository.findLatestTasks(projectId, request.getGraphNodeId());
        if (taskDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
        }
        return getGraphNodeTaskOutputVO(taskDOOptional.get(), request.getOutputId());
    }

    @Override
    public GraphNodeOutputVO getGraphNodeTaskOutputVO(GetProjectJobTaskOutputRequest request) {
        ProjectTaskDO jobTask = openProjectJobTask(request.getJobId(), request.getTaskId());
        return getGraphNodeTaskOutputVO(jobTask, request.getOutputId());
    }

    @Override
    public GraphNodeMaxIndexRefreshVO refreshNodeMaxIndex(GraphNodeMaxIndexRefreshRequest request) {
        Optional<ProjectGraphDO> projectGraphDOOptional = graphRepository.findById(new ProjectGraphDO.UPK(request.getProjectId(), request.getGraphId()));
        if (projectGraphDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        ProjectGraphDO projectGraphDO = projectGraphDOOptional.get();
        Integer index = projectGraphDO.getNodeMaxIndex();
        if (Objects.nonNull(request.getCurrentIndex()) && request.getCurrentIndex() > index) {
            projectGraphDO.setNodeMaxIndex(request.getCurrentIndex() + 1);
            index = request.getCurrentIndex();
        } else {
            projectGraphDO.setNodeMaxIndex(index + 1);
        }
        graphRepository.save(projectGraphDO);
        return GraphNodeMaxIndexRefreshVO.builder().maxIndex(index).build();
    }

    /**
     * Query project job by jobId then return job tasks by taskId
     *
     * @param jobId  target jobId
     * @param taskId target taskId
     * @return project task data object
     */
    private ProjectTaskDO openProjectJobTask(String jobId, String taskId) {
        Optional<ProjectJobDO> jobOpt = jobRepository.findByJobId(jobId);
        if (jobOpt.isEmpty()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
        }
        ProjectJobDO job = jobOpt.get();
        if (!job.getTasks().containsKey(taskId)) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
        }
        return job.getTasks().get(taskId);
    }

    /**
     * Build graph node output view object by project task data object and outputId
     *
     * @param taskDO   target project task data object
     * @param outputId target outputId
     * @return graph node output view object
     */
    private GraphNodeOutputVO getGraphNodeTaskOutputVO(ProjectTaskDO taskDO, String outputId) {
        String projectId = taskDO.getUpk().getProjectId();
        GraphNodeOutputVO outputVO = GraphNodeOutputVO.builder().build();
        List<GraphNodeOutputVO.OutputResult> outputResults = new ArrayList<>();
        Table.HeaderItem fileHeader = Table.HeaderItem.newBuilder().setType(String.valueOf(AttrType.AT_STRING)).setName("metas").build();
        ProjectGraphNodeDO graphNode = taskDO.getGraphNode();
        GraphNodeInfo graphNodeInfo = GraphNodeInfo.fromDO(graphNode);
        if (componentService.isSecretpadComponent(graphNodeInfo)) {
            outputVO.setType("table");
            String datatableId = ComponentTools.getDataTableId(graphNodeInfo);
            List<ProjectDatatableDO> datatableDOS = datatableRepository.findByDatableId(projectId, datatableId);
            if (!CollectionUtils.isEmpty(datatableDOS)) {
                for (ProjectDatatableDO datatableDO : datatableDOS) {
                    GraphNodeOutputVO.OutputResult outputResult = fromDatatable(datatableDO, null, null);
                    outputResults.add(outputResult);
                }
                outputVO.setGmtCreate(DateTimes.toRfc3339(datatableDOS.get(0).getGmtCreate()));
                outputVO.setGmtModified(DateTimes.toRfc3339(datatableDOS.get(0).getGmtModified()));
            }
        } else {
            String jobId = taskDO.getUpk().getJobId();
            String taskId = taskDO.getUpk().getTaskId();
            outputVO.setTaskId(taskId);
            outputVO.setJobId(jobId);
            Optional<ProjectJobDO> projectJobDOOptional = jobRepository.findById(new ProjectJobDO.UPK(projectId, jobId));
            if (projectJobDOOptional.isEmpty()) {
                throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NOT_EXISTS);
            }
            outputVO.setGraphID(projectJobDOOptional.get().getGraphId());
            List<String> outputs = taskDO.getGraphNode().getOutputs();
            if (CollectionUtils.isEmpty(outputs) || outputs.contains(outputId)) {
                String latestOutputId = genTaskOutputId(jobId, outputId);
                List<ProjectResultDO> resultDOS = resultRepository.findByOutputId(projectId, taskId, latestOutputId);
                //task file compensation binning modifications and model param modifications
                compensationSecretPadComponent(taskDO, outputId, outputVO);
                if (!CollectionUtils.isEmpty(resultDOS)) {
                    for (ProjectResultDO resultDO : resultDOS) {
                        ResultKind resultKind = resultDO.getUpk().getKind();
                        outputVO.setType(GraphNodeOutputVO.typeFromResultKind(resultKind));
                        outputVO.setCodeName(taskDO.getGraphNode().getCodeName());
                        outputVO.setGmtCreate(DateTimes.toRfc3339(resultDO.getGmtCreate()));
                        outputVO.setGmtModified(DateTimes.toRfc3339(resultDO.getGmtModified()));
                        String nodeId = resultDO.getUpk().getNodeId();
                        String refId = resultDO.getUpk().getRefId();
                        GraphNodeOutputVO.OutputResult outputResult;
                        String content; //TODO:not sure
                        String targetNodeId = nodeId;
                        if (PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(plaformType)) && !P2pDataSyncProducerTemplate.nodeIds.contains(targetNodeId)) {
                            List<ProjectNodeDO> projectNodeDOList = projectNodeRepository.findByProjectId(projectId);
                            if (!CollectionUtils.isEmpty(projectNodeDOList)) {
                                List<String> list = projectNodeDOList.stream().map(ProjectNodeDO::getUpk)
                                        .map(ProjectNodeDO.UPK::getNodeId).filter(n -> !taskDO.getParties().contains(n)).toList();
                                if (!CollectionUtils.isEmpty(list)) {
                                    targetNodeId = list.get(0);
                                }
                            }
                        }
                        Domaindata.DomainData domainData = dataManager.queryDomainData(nodeId, refId, targetNodeId);
                        String datasourceId = domainData.getDatasourceId();
                        Optional<DatasourceDTO> datasourceOpt = datasourceManager.findById(DatasourceDTO.NodeDatasourceId.from(targetNodeId, datasourceId));
//                                kusciaGrpcClientAdapter.queryDomainDataSource(Domaindatasource.QueryDomainDataSourceRequest.newBuilder()
//                                .setDomainId(nodeId).setDatasourceId(datasourceId).build());
                        String datasourceType = DataSourceTypeEnum.kuscia2platform(datasourceOpt.get().getType());

                        switch (resultKind) {
                            case Report:
                                Optional<ProjectReportDO> reportDOOptional = reportRepository.findById(new ProjectReportDO.UPK(projectId, latestOutputId));
                                if (reportDOOptional.isEmpty()) {
                                    throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
                                }
                                ProjectReportDO reportDO = reportDOOptional.get();
                                content = reportDO.getContent();
                                JsonNode jsonNode = JsonUtils.parseObject(content);
                                Object tabs = null;
                                if (ObjectUtils.isNotEmpty(jsonNode)) {
                                    JsonNode meta = jsonNode.get("meta");
                                    if (ObjectUtils.isNotEmpty(meta)) {
                                        tabs = meta.get("tabs");
                                    }
                                }
                                outputVO.setTabs(ObjectUtils.isEmpty(tabs) ? new ArrayList<>() : tabs);
                                return outputVO;
                            case READ_DATA:
                                Optional<ProjectReadDataDO> readDataDOOptional = projectReadDtaRepository.findById(new ProjectReadDataDO.UPK(projectId, latestOutputId));
                                if (readDataDOOptional.isEmpty()) {
                                    throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
                                }
                                ProjectReadDataDO readDataDO = readDataDOOptional.get();
                                content = readDataDO.getContent();
                                log.info("content is {}", content);

                                Gson gson = new Gson();
                                String outputTabs = (String) outputVO.getTabs();
                                String json1 = gson.toJson(outputTabs);
                                JsonElement jsonElement = gson.fromJson(json1, JsonElement.class);
                                log.info("json1 is {}", json1);

                                JsonElement contentElement = gson.fromJson(content, JsonElement.class);
                                JsonArray contentJsonArray = contentElement.getAsJsonArray();
                                contentJsonArray.set(0, jsonElement);
                                String json = gson.toJson(contentJsonArray);

                                log.info("json is {}", json);
                                outputVO.setTabs(json);
                                return outputVO;
                            case Model:
                                outputResult = GraphNodeOutputVO.OutputResult.builder().nodeId(nodeId).path(latestOutputId).type(ResultKind.Model.getName()).tableId(latestOutputId).dsId(datasourceId).datasourceType(datasourceType).build();
                                outputResults.add(outputResult);
                                break;
                            case Rule:
                                outputResult = GraphNodeOutputVO.OutputResult.builder().nodeId(nodeId).path(latestOutputId).type(ResultKind.Rule.getName()).tableId(latestOutputId).dsId(datasourceId).datasourceType(datasourceType).build();
                                outputResults.add(outputResult);
                                break;
                            case FedTable:
                                Optional<ProjectDatatableDO> datatableDOOptional = datatableRepository.findById(new ProjectDatatableDO.UPK(projectId, nodeId, latestOutputId));
                                if (datatableDOOptional.isEmpty()) {
                                    throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
                                }
                                outputResult = fromDatatable(datatableDOOptional.get(), null, null);
                                outputResult.setDatasourceType(datasourceType);
                                outputResults.add(outputResult);
                                break;
                            default:
                                throw SecretpadException.of(DatatableErrorCode.UNSUPPORTED_DATATABLE_TYPE);
                        }
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(outputResults)) {
            for (GraphNodeOutputVO.OutputResult outputResult : outputResults) {
                NodeDO nodeDO = nodeRepository.findByNodeId(outputResult.getNodeId());
                String nodeName = ObjectUtils.isEmpty(nodeDO) ? outputResult.getNodeId() : nodeDO.getName();
                outputResult.setNodeName(nodeName);
            }
        }
        GraphNodeOutputVO.FileMeta fileMeta = GraphNodeOutputVO.FileMeta.builder().headers(ProtoUtils.protosToListMap(List.of(fileHeader))).rows(outputResults).build();
        outputVO.setMeta(fileMeta);
        return outputVO;
    }

    private void compensationSecretPadComponent(ProjectTaskDO taskDO, String outputId, GraphNodeOutputVO outputVO) {
        String projectId = taskDO.getUpk().getProjectId();
        ProjectGraphNodeDO graphNode = taskDO.getGraphNode();
        String type = outputId.substring(outputId.length() - 1);
        log.debug("compensationSecretPadComponent CodeName:{}  outputId:{}  type：{}  Label:{}", graphNode.getCodeName(), outputId, type, graphNode.getLabel());
        if ((BINNING_MODIFICATIONS_CODENAME.equals(graphNode.getCodeName()) && "1".equals(type)) || (MODEL_PARAM_MODIFICATIONS_CODENAME.equals(graphNode.getCodeName()) && "1".equals(type))) {
            String inputId = taskDO.getGraphNode().getInputs().get(0);
            String graphNodeId = inputId;
            int i = graphNodeId.lastIndexOf('-');
            graphNodeId = graphNodeId.substring(0, i);
            i = graphNodeId.lastIndexOf('-');
            graphNodeId = graphNodeId.substring(0, i);
            log.debug("-- inputId {} graphNodeId {}", inputId, graphNodeId);
            Optional<ProjectTaskDO> projectTaskDOOptional = taskRepository.findLatestTasks(projectId, graphNodeId);
            if (projectTaskDOOptional.isEmpty()) {
                throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
            }
            String taskId = projectTaskDOOptional.get().getUpk().getJobId();
            String taskOutputId = genTaskOutputId(taskId, inputId);

            ProjectReadDataDO projectReadDataDO = readDtaRepository.findByProjectIdAndOutputIdLaste(projectId, taskOutputId);
            if (!ObjectUtils.isEmpty(projectReadDataDO)) {
                String contentResult = projectReadDataDO.getRaw();
                outputVO.setTabs(contentResult);
                log.debug("tabs result is {}", contentResult);
                outputVO.setType(GraphNodeOutputVO.typeFromResultKind(ResultKind.READ_DATA));
                outputVO.setCodeName(taskDO.getGraphNode().getCodeName());
            }
        }

    }

    @Override
    public GraphNodeOutputVO getResultOutputVO(String nodeId, String resultId) {
        GraphNodeOutputVO outputVO = GraphNodeOutputVO.builder().build();
        List<GraphNodeOutputVO.OutputResult> outputResults = new ArrayList<>();
        Table.HeaderItem fileHeader = Table.HeaderItem.newBuilder().setType(String.valueOf(AttrType.AT_STRING)).setName("metas").build();
        Optional<ProjectResultDO> resultOpt = resultRepository.findByNodeIdAndRefId(nodeId, resultId);
        if (resultOpt.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
        }
        ProjectResultDO resultDO = resultOpt.get();
        ProjectTaskDO task = openProjectJobTask(resultDO.getJobId(), resultDO.getTaskId());
        ResultKind resultKind = resultDO.getUpk().getKind();
        outputVO.setType(GraphNodeOutputVO.typeFromResultKind(resultKind));
        outputVO.setCodeName(task.getGraphNode().getCodeName());
        outputVO.setGmtCreate(DateTimes.toRfc3339(resultDO.getGmtCreate()));
        outputVO.setGmtModified(DateTimes.toRfc3339(resultDO.getGmtModified()));
        String projectId = resultDO.getUpk().getProjectId();
        Optional<ProjectDO> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        // rebuild node id and result id when project is tee mode
        String centerNodeId = nodeId;
        String centerResultId = resultId;
        if (StringUtils.endsWithIgnoreCase(projectOpt.get().getComputeMode(), TEE_PROJECT_MODE)) {
            centerResultId = resultId.replace(nodeId + "-", "");
            centerNodeId = teeNodeId;
        }
        GraphNodeOutputVO.OutputResult outputResult;
        switch (resultKind) {
            case Report:
                Optional<ProjectReportDO> reportDOOptional = reportRepository.findById(new ProjectReportDO.UPK(projectId, centerResultId));
                if (reportDOOptional.isEmpty()) {
                    throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_OUTPUT_NOT_EXISTS);
                }
                ProjectReportDO reportDO = reportDOOptional.get();
                String content = reportDO.getContent();
                Object tabs = JsonUtils.parseObject(content).get("meta").get("tabs");
                outputVO.setTabs(tabs);
                return outputVO;
            case Model:
            case Rule:
                outputResult = GraphNodeOutputVO.OutputResult.builder().nodeId(centerNodeId).path(centerResultId).build();
                outputResults.add(outputResult);
                break;
            case FedTable:
                Optional<ProjectDatatableDO> datatableDOOptional = datatableRepository.findById(new ProjectDatatableDO.UPK(projectId, centerNodeId, centerResultId));
                if (datatableDOOptional.isEmpty()) {
                    throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
                }
                outputResult = fromDatatable(datatableDOOptional.get(), nodeId, resultId);
                outputResults.add(outputResult);
                break;
            default:
                throw SecretpadException.of(DatatableErrorCode.UNSUPPORTED_DATATABLE_TYPE);
        }
        GraphNodeOutputVO.FileMeta fileMeta = GraphNodeOutputVO.FileMeta.builder().headers(ProtoUtils.protosToListMap(List.of(fileHeader))).rows(outputResults).build();
        outputVO.setMeta(fileMeta);
        return outputVO;
    }

    /**
     * Build graph node output result from project datatable data object
     *
     * @param datatableDO target project datatable data object
     * @param edgeNodeId  edge node id
     * @param edgeTableId edge table id
     * @return graph node output result
     */
    private GraphNodeOutputVO.OutputResult fromDatatable(ProjectDatatableDO datatableDO, String edgeNodeId, String edgeTableId) {
        List<ProjectDatatableDO.TableColumnConfig> tableConfig = datatableDO.getTableConfig();
        List<String> fields = new ArrayList<>();
        List<String> types = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tableConfig)) {
            tableConfig.forEach(config -> {
                fields.add(config.getColName());
                types.add(config.getColType());
            });
        }
        String projectId = datatableDO.getProjectId();
        String nodeId = datatableDO.getUpk().getNodeId();
        String tableId = datatableDO.getUpk().getDatatableId();
        GraphNodeOutputVO.OutputResult outputResult = GraphNodeOutputVO.OutputResult.builder().nodeId(nodeId).type(nodeRepository.findByNodeId(nodeId).getType()).fields(String.join(",", fields)).fieldTypes(String.join(",", types)).tableId(tableId).build();
        Optional<ProjectDO> projectOpt = projectRepository.findById(datatableDO.getProjectId());
        if (projectOpt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        log.warn("recode  edgeNodeId={} graphNodeId={} , datatableDO={}", edgeNodeId, edgeTableId, JsonUtils.toJSONString(datatableDO));
        DatatableDTO.NodeDatatableId query = DatatableDTO.NodeDatatableId.from(nodeManager.getTargetNodeId(nodeId, projectId), tableId);
        Optional<DatatableDTO> datatableDTOOptional = datatableManager.findById(query);

        if (datatableDTOOptional.isPresent()) {
            DatatableDTO datatableDTO = datatableDTOOptional.get();
            outputResult.setPath(datatableDTO.getRelativeUri());
            outputResult.setDsId(datatableDTO.getDatasourceId());
        }
        return outputResult;
    }

    @Transactional
    @Override
    public StartGraphVO startGraph(StartGraphRequest request) {
        // check project graph owner
        ProjectGraphDO graphDO = ownerCheck(request.getProjectId(), request.getGraphId());
        List<ProjectGraphNodeDO> nodeDOList = graphDO.getNodes();
        if (CollectionUtils.isEmpty(nodeDOList)) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_NOT_EXISTS);
        }
        List<String> nodeIds = request.getNodes();
        List<ProjectGraphNodeDO> selectedNodes = nodeDOList.stream().filter(nodeDO -> nodeIds.contains(nodeDO.getUpk().getGraphNodeId())).collect(Collectors.toList());
        if (selectedNodes.size() != nodeIds.size()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_NOT_EXISTS);
        }
        Optional<ProjectDO> projectOpt = projectRepository.findById(request.getProjectId());
        if (projectOpt.isEmpty()) {
            throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS);
        }
        List<GraphContext.GraphParty> partyList = new ArrayList<>();
        Map<String, Set<String>> topNodes = findTopNodes(graphDO.getEdges(), selectedNodes);
        Map<String, Set<String>> parties = findParties(graphDO.getNodes(), topNodes, request.getProjectId(), partyList);
        GraphContext.set(projectOpt.get(), GraphContext.GraphParties.builder().parties(partyList).build(), request.getBreakpoint());
        if (GraphContext.isTee()) {
            parties = new HashMap<>();
            String teeNodeId = GraphContext.getTeeNodeId();
            Set<String> partyNodes = new HashSet<>();
            partyNodes.add(teeNodeId);
            for (Map.Entry<String, Set<String>> entry : topNodes.entrySet()) {
                parties.put(entry.getKey(), partyNodes);
            }
        }

        verifyNodeAndRouteHealthy(parties.values().stream().flatMap(Set::stream).collect(Collectors.toSet()), request.getProjectId());
        ProjectJob projectJob = ProjectJob.genProjectJob(graphDO, selectedNodes, parties);
        jobChain.proceed(projectJob);
        GraphContext.remove();
        return new StartGraphVO(projectJob.getJobId());
    }

    /**
     * check project Graph owner
     *
     * @param projectId Project id
     * @param graphId   Graph Id
     * @return ProjectGraphDO
     */
    private ProjectGraphDO ownerCheck(String projectId, String graphId) {
        Optional<ProjectGraphDO> graphOptional = graphRepository.findById(new ProjectGraphDO.UPK(projectId, graphId));
        if (graphOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        ProjectGraphDO graphDO = graphOptional.get();
        String ownerId = UserContext.getUser().getOwnerId();
        if (!StringUtils.equals(ownerId, graphDO.getOwnerId())) {
            throw SecretpadException.of(GraphErrorCode.NON_OUR_CREATION_CAN_VIEWED);
        }
        return graphDO;
    }

    @Override
    public GraphStatus listGraphNodeStatus(ListGraphNodeStatusRequest request) {
        String projectId = request.getProjectId();
        String graphId = request.getGraphId();
        Optional<ProjectGraphDO> graphDOOptional = graphRepository.findById(new ProjectGraphDO.UPK(projectId, graphId));
        if (graphDOOptional.isEmpty()) {
            throw SecretpadException.of(GraphErrorCode.GRAPH_NOT_EXISTS);
        }
        return getLatestTaskStatus(graphDOOptional.get());
    }

    /**
     * Find latest task status
     * Todo: find latest task with one sql
     *
     * @param graphDO target graph data object
     * @return latest graph task status
     */
    public GraphStatus getLatestTaskStatus(ProjectGraphDO graphDO) {
        String projectId = graphDO.getUpk().getProjectId();
        List<ProjectGraphNodeDO> nodes = graphDO.getNodes();
        GraphStatus graphStatus = new GraphStatus();
        List<GraphNodeStatusVO> nodeStatus = new ArrayList<>();
        List<String> jobIds = new ArrayList<>();

        // find the latest task associated with graphNode
        if (!CollectionUtils.isEmpty(nodes)) {
            List<String> graphNodeIds = nodes.stream().map(node -> node.getUpk().getGraphNodeId()).toList();
            for (String graphNodeId : graphNodeIds) {
                GraphNodeStatusVO nodeStatusVO = new GraphNodeStatusVO();
                nodeStatusVO.setGraphNodeId(graphNodeId);
                Optional<ProjectTaskDO> taskDOOptional = taskRepository.findLatestTasks(projectId, graphNodeId);
                GraphNodeTaskStatus status = GraphNodeTaskStatus.STAGING;
                if (taskDOOptional.isPresent()) {
                    status = taskDOOptional.get().getStatus();
                    nodeStatusVO.setTaskId(taskDOOptional.get().getUpk().getTaskId());
                    nodeStatusVO.setJobId(taskDOOptional.get().getUpk().getJobId());
                    nodeStatusVO.setParties(nodeRepository.findByNodeIdIn(taskDOOptional.get().getParties()).stream().map(e -> NodeSimpleInfo.builder().nodeName(e.getName()).nodeId(e.getNodeId()).build()).collect(Collectors.toList()));
                    jobIds.add(taskDOOptional.get().getUpk().getJobId());
                }
                nodeStatusVO.setStatus(status);
                nodeStatus.add(nodeStatusVO);
            }
        }

        // resolve job status
        boolean finished = true;
        if (!CollectionUtils.isEmpty(jobIds)) {
            List<ProjectJobStatus> jobStatuses = jobRepository.findStatusByJobIds(projectId, jobIds);
            for (ProjectJobStatus job : jobStatuses) {
                if (!job.isFinished()) {
                    finished = false;
                    break;
                }
            }
        }

        graphStatus.setNodes(nodeStatus);
        graphStatus.setFinished(finished);
        return graphStatus;
    }


    @Override
    public GraphNodeTaskLogsVO getGraphNodeLogs(GraphNodeLogsRequest request) {
        Optional<ProjectTaskDO> taskDOOptional = taskRepository.findLatestTasks(request.getProjectId(), request.getGraphNodeId());
        if (taskDOOptional.isEmpty()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
        }
        ProjectTaskDO task = taskDOOptional.get();
        GraphNodeTaskLogsVO graphNodeTaskLogsVO = new GraphNodeTaskLogsVO(task.getStatus(),
                jobTaskLogRepository.findAllByJobTaskId(task.getUpk().getJobId(), task.getUpk().getTaskId())
                        .stream().map(ProjectJobTaskLogDO::getLog).distinct().collect(Collectors.toList()));
        if (graphNodeTaskLogsVO.getLogs().isEmpty() && READ_DATA_DATATABLE.equals(task.getGraphNode().getCodeName())) {
            String jobId = task.getUpk().getJobId();
            String taskId = task.getUpk().getTaskId();
            graphNodeTaskLogsVO.setLogs(Arrays.asList(
                    ProjectJobTaskLogDO.makeLog(task.getGmtCreate(), String.format("the jobId=%s, taskId=%s start ...", jobId, taskId)),
                    ProjectJobTaskLogDO.makeLog(task.getGmtCreate(), String.format("the jobId=%s, taskId=%s succeed", jobId, taskId))
            ));
        }
        String logPrefix = String.format("INFO the jobId=%s, taskId=%s-%s", task.getUpk().getJobId(), task.getUpk().getJobId(), request.getGraphNodeId());
        log.info("log de duplication matching， {}", logPrefix);
        distinctSpecifyLogs(graphNodeTaskLogsVO, logPrefix + " start");
        distinctSpecifyLogs(graphNodeTaskLogsVO, logPrefix + " succeed");
        return graphNodeTaskLogsVO;
    }

    @Override
    public void stopGraphNode(StopGraphNodeRequest request) {
        String projectId = request.getProjectId();
        String graphId = request.getGraphId();
        String graphNodeId = request.getGraphNodeId();
        // check project graph owner
        ownerCheck(projectId, graphId);
        List<StopProjectJobTaskRequest> stopRequests = new ArrayList<>();
        if (Strings.isNullOrEmpty(graphNodeId)) {
            // find all running jobs in whole graph
            List<ProjectJobDO> runningJobs = jobRepository.findByStatus(projectId, graphId, GraphJobStatus.RUNNING);
            if (!CollectionUtils.isEmpty(runningJobs)) {
                stopRequests = runningJobs.stream().map(job -> new StopProjectJobTaskRequest(projectId, job.getUpk().getJobId())).collect(Collectors.toList());
            }
        } else {
            // find all running jobs associated with graphNode
            List<ProjectTaskDO> runningTasks = taskRepository.findByStatus(projectId, graphNodeId, GraphNodeTaskStatus.RUNNING);
            if (!CollectionUtils.isEmpty(runningTasks)) {
                stopRequests = runningTasks.stream().map(task -> new StopProjectJobTaskRequest(projectId, task.getUpk().getJobId())).collect(Collectors.toList());
            }
        }
        if (!CollectionUtils.isEmpty(stopRequests)) {
            stopRequests.forEach(req -> projectService.stopProjectJob(req));
        }
    }


    public void verifyNodeAndRouteHealthy(Set<String> parties, String projectId) {
        log.info("before graph run healthy check: {}", parties);
        if (PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(plaformType))) {
            // unilateral mission
            if (parties.size() == 1) {
                return;
            }
            Set<String> instNodeIds = nodeRepository.findByInstId(UserContext.getUser().getOwnerId()).stream().map(NodeDO::getNodeId).collect(Collectors.toSet());
            instNodeIds.retainAll(parties);
            Map<String, List<AutonomyNodeRouteUtil.AutonomySourceNodeRouteInfo>> autonomySelfDstNodeRouteInfoMap = AutonomyNodeRouteUtil.getAutonomySelfDstNodeRouteInfoMap();

            Map<String, List<AutonomyNodeRouteUtil.AutonomySourceNodeRouteInfo>> filterAutonomySelfDstNodeRouteInfoMap = autonomySelfDstNodeRouteInfoMap.entrySet().stream()
                    .filter(entry -> instNodeIds.contains(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Optional<ProjectApprovalConfigDO> projectApprovalConfigDOOptional = projectApprovalConfigRepository.findByProjectIdAndType(projectId, VoteTypeEnum.PROJECT_CREATE.name());
            if (projectApprovalConfigDOOptional.isEmpty()) {
                throw SecretpadException.of(ProjectErrorCode.PROJECT_NOT_EXISTS, "project approval config not exists");
            }
            List<ParticipantNodeInstVO> participantNodeInstVOS = projectApprovalConfigDOOptional.get().getParticipantNodeInfo();
            for (String party : parties) {
                if (!filterAutonomySelfDstNodeRouteInfoMap.containsKey(party)) {
                    boolean find = false;
                    for (Map.Entry<String, List<AutonomyNodeRouteUtil.AutonomySourceNodeRouteInfo>> entry : filterAutonomySelfDstNodeRouteInfoMap.entrySet()) {
                        Optional<AutonomyNodeRouteUtil.AutonomySourceNodeRouteInfo> nodeRouteInfoOptional = entry.getValue().stream().filter(e -> StringUtils.equals(e.getSourceNodeId(), party)).findAny();
                        if (nodeRouteInfoOptional.isPresent()) {
                            find = true;
                            if (!nodeRouteInfoOptional.get().isSourceToDstIsAvailable()) {
                                for (ParticipantNodeInstVO vo : participantNodeInstVOS) {
                                    if (vo.getInitiatorNodeId().equals(nodeRouteInfoOptional.get().getSourceNodeId())) {
                                        if (vo.getInvitees().contains(entry.getKey())) {
                                            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_ROUTE_NOT_EXISTS, party + "->" + entry.getKey());
                                        }
                                    } else if (vo.getInvitees().contains(nodeRouteInfoOptional.get().getSourceNodeId())) {
                                        String initiatorNodeId = vo.getInitiatorNodeId();
                                        if (StringUtils.equals(initiatorNodeId, entry.getKey())) {
                                            throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_ROUTE_NOT_EXISTS, party + "-> " + entry.getKey());
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                    if (!find) {
                        throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_ROUTE_NOT_EXISTS, party + "-> " + parties);
                    }
                }
            }


            // now allow The Initiator Not parties
            /*if (!parties.contains(localNodeId)) {
                throw SecretpadException.of(GraphErrorCode.GRAPH_JOB_INVALID, "parties must contains " + localNodeId);
            }

            for (String party : parties) {
                if (StringUtils.equals(party, localNodeId)) {
                    continue;
                }
                if (!nodeRouteManager.checkNodeRouteReady(party, localNodeId)) {
                    throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_ROUTE_NOT_EXISTS, party + "->" + localNodeId);
                }
            }*/


            return;
        }
        parties.forEach(node -> {
            if (!nodeManager.checkNodeReady(node)) {
                NodeDO nodeDO = nodeRepository.findByNodeId(node);
                String msg = ObjectUtils.isEmpty(nodeDO) ? node : nodeDO.getName();
                throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_NOT_EXISTS, msg);
            }
        });
        for (String partySrc : parties) {
            for (String partyDst : parties) {
                if (!partySrc.equals(partyDst) && !nodeRouteManager.checkNodeRouteReady(partySrc, partyDst, localNodeId)) {
                    NodeDO partySrcNodeDO = nodeRepository.findByNodeId(partySrc);
                    NodeDO partyDstNodeDO = nodeRepository.findByNodeId(partyDst);
                    String msg1 = ObjectUtils.isEmpty(partySrcNodeDO) ? partySrc : partySrcNodeDO.getName();
                    String msg2 = ObjectUtils.isEmpty(partyDstNodeDO) ? partyDst : partyDstNodeDO.getName();
                    throw SecretpadException.of(GraphErrorCode.GRAPH_NODE_ROUTE_NOT_EXISTS, msg1 + "->" + msg2);
                }
            }
        }
    }


    //Delete redundant start and succeed logs
    private void distinctSpecifyLogs(GraphNodeTaskLogsVO graphNodeTaskLogsVO, String distinctValue) {
        List<String> logs = graphNodeTaskLogsVO.getLogs();
        List<String> uniqueList = new ArrayList<>();
        boolean flag = false;
        for (String str : logs) {
            if (str.contains(distinctValue) && !str.contains("failed")) {
                if (!flag) {
                    uniqueList.add(str);
                } else {
                    log.info("remove log {}", str);
                }
                flag = true;
            } else {
                uniqueList.add(str);
            }
        }
        graphNodeTaskLogsVO.setLogs(uniqueList);
    }

    private Map<String, Set<String>> findTopNodes(List<GraphEdgeDO> edges, List<ProjectGraphNodeDO> selectedNodes) {
        Map<String, Set<String>> tops = new HashMap<>();
        selectedNodes.forEach(node -> {
            Set<String> topNodes = GraphUtils.findTopNodes(edges, node.getUpk().getGraphNodeId());
            tops.put(node.getUpk().getGraphNodeId(), topNodes);
        });
        return tops;
    }

    private Map<String, Set<String>> findParties(List<ProjectGraphNodeDO> nodes, Map<String, Set<String>> tops, String projectId, List<GraphContext.GraphParty> partyList) {
        Map<String, Set<String>> result = new HashMap<>();
        Map<String, ProjectGraphNodeDO> nodeDOMap = nodes.stream().collect(Collectors.toMap(e -> (e.getUpk()).getGraphNodeId(), Function.identity()));
        tops.forEach((key, value) -> {
            Set<String> parties = new HashSet<>();
            value.forEach(e -> {
                ProjectGraphNodeDO projectGraphNodeDO = nodeDOMap.get(e);
                GraphNodeInfo graphNodeInfo = GraphNodeInfo.fromDO(projectGraphNodeDO);
                String datatableId = ComponentTools.getDataTableId(graphNodeInfo);
                if (StringUtils.isNotBlank(datatableId)) {
                    List<ProjectDatatableDO> datatableDOS = datatableRepository.findByDatableId(projectId, datatableId);
                    if (!CollectionUtils.isEmpty(datatableDOS)) {
                        parties.addAll(datatableDOS.stream().map(datatableDO -> datatableDO.getUpk().getNodeId()).toList());
                        partyList.add(GraphContext.GraphParty.builder().datatableId(datatableId).node(datatableDOS.get(0).getUpk().getNodeId()).build());
                    }
                }

            });
            result.put(key, parties);
        });
        return result;
    }
}
