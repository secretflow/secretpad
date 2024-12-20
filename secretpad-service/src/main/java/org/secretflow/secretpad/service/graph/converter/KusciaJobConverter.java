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

package org.secretflow.secretpad.service.graph.converter;

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.ProjectGraphDomainDatasourceService;
import org.secretflow.secretpad.service.ReadPartitionRuleAnalysisService;
import org.secretflow.secretpad.service.constant.ComponentConstants;
import org.secretflow.secretpad.service.constant.JobConstants;
import org.secretflow.secretpad.service.graph.ComponentTools;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;
import org.secretflow.secretpad.service.model.model.export.ModelExportPackageRequest;
import org.secretflow.secretpad.service.model.model.export.ModelPartyConfig;
import org.secretflow.secretpad.service.model.task.TaskConfigResult;
import org.secretflow.secretpad.service.util.GraphUtils;
import org.secretflow.secretpad.service.util.JobUtils;
import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.IndividualTable;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.secretflow.proto.component.Cluster;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.proto.pipeline.Pipeline;
import org.secretflow.scql.ScqlTaskConfig;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.common.constant.ComponentConstants.*;
import static org.secretflow.secretpad.common.constant.Constants.JOB_ID_LENGTH;
import static org.secretflow.secretpad.common.constant.DatabaseConstants.CSV_DB_TYPE;
import static org.secretflow.secretpad.common.constant.DomainDatasourceConstants.DEFAULT_MYSQL_DATASOURCE_TYPE;
import static org.secretflow.secretpad.common.constant.DomainDatasourceConstants.DEFAULT_ODPS_DATASOURCE_TYPE;
import static org.secretflow.secretpad.service.constant.ComponentConstants.*;


/**
 * Job converter for message in apiLite
 *
 * @author yansi
 * @date 2023/5/30
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "sfcluster-desc")
public class KusciaJobConverter implements JobConverter {
    private final static String DEFAULTDS = "default-data-source";
    private static String crossSiloCommBackend;
    private static Map<String, String> deviceConfig;
    ThreadLocal<ProjectGraphNodeKusciaParamsDO> projectGraphNodeKusciaParamsDOThreadLocal = new ThreadLocal<>();
    @Value("${job.max-parallelism:1}")
    @Getter
    private int maxParallelism;
    @Value("${secretpad.platform-type}")
    private String platformType;
    @Resource
    private ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;
    @Resource
    private ProjectJobTaskRepository taskRepository;
    @Resource
    private ProjectGraphDomainDatasourceService projectGraphDomainDatasourceService;
    @Resource
    private EnvService envService;

    @Resource
    private ProjectGraphRepository graphRepository;

    @Resource
    private ProjectGraphNodeRepository graphNodeRepository;

    @Resource
    private ProjectDatatableRepository projectDatatableRepository;

    @Resource
    private ReadPartitionRuleAnalysisService readPartitionRuleAnalysisService;

    @SuppressWarnings("unused")
    @Value("${sfcluster-desc.ray-fed-config.cross-silo-comm-backend:brpc_link}")
    private void setCrossSiloCommBackend(String crossSiloCommBackend) {
        KusciaJobConverter.crossSiloCommBackend = crossSiloCommBackend;
    }

    public void setDeviceConfig(Map<String, String> deviceConfig) {
        KusciaJobConverter.deviceConfig = deviceConfig;
    }

    /**
     * Converter create job request from project job
     *
     * @param job project job class
     * @return create job request message
     */
    public Job.CreateJobRequest converter(ProjectJob job) {
        List<ProjectJob.JobTask> tasks = job.getTasks();
        List<Job.Task> jobTasks = new ArrayList<>();
        String initiator = "";
        if (!CollectionUtils.isEmpty(tasks)) {
            for (ProjectJob.JobTask task : tasks) {
                String taskId = task.getTaskId();
                List<Job.Party> taskParties = new ArrayList<>();
                List<String> parties = task.getParties();
                if (!CollectionUtils.isEmpty(parties)) {
                    initiator = parties.get(0);
                    if (PlatformTypeEnum.AUTONOMY.equals(PlatformTypeEnum.valueOf(platformType))) {
                        initiator = envService.findLocalNodeId(task);
                        log.info("KusciaJobConverter converter parties {} initiator {}", parties, initiator);
                    }
                    taskParties = parties.stream().map(party -> Job.Party.newBuilder().setDomainId(party).build()).collect(Collectors.toList());
                }

                ProjectGraphNodeKusciaParamsDO.UPK upk = new ProjectGraphNodeKusciaParamsDO.UPK(job.getProjectId(), job.getGraphId(), task.getNode().getGraphNodeId());
                projectGraphNodeKusciaParamsDOThreadLocal.set(ProjectGraphNodeKusciaParamsDO.builder().upk(upk).jobId(job.getJobId()).build());
                TaskConfigResult taskConfigResult = renderTaskInputConfig(task, job, taskParties);
                Job.Task.Builder jobTaskBuilder = Job.Task.newBuilder()
                        .setTaskId(taskId)
                        .setAlias(taskId)
                        .setAppImage(taskConfigResult.getAppImage())
                        .addAllParties(taskParties)
                        .setTaskInputConfig(taskConfigResult.getTaskInputConfig());
                if (!CollectionUtils.isEmpty(task.getDependencies())) {
                    jobTaskBuilder.addAllDependencies(task.getDependencies());
                }
                jobTasks.add(jobTaskBuilder.build());
                projectGraphNodeKusciaParamsDOThreadLocal.remove();
            }
        }
        return Job.CreateJobRequest.newBuilder()
                .setJobId(job.getJobId())
                .setInitiator(initiator)
                .setMaxParallelism(job.getMaxParallelism())
                .addAllTasks(jobTasks)
                .build();
    }

    /**
     * Render task input config message from project job task
     *
     * @param task project job task
     * @return json string of task input config message
     */
    private TaskConfigResult renderTaskInputConfig(ProjectJob.JobTask task, ProjectJob job, List<Job.Party> taskParties) {
        GraphNodeInfo graphNode = task.getNode();
        Object nodeDef = graphNode.getNodeDef();
        List<String> inputs = graphNode.getInputs();
        List<String> outputs = graphNode.getOutputs();
        List<String> parties = task.getParties();
        String projectId = job.getProjectId();
        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(IndividualTable.getDescriptor()).build();
        Pipeline.NodeDef pipelineNodeDef = ComponentTools.getNodeDef(nodeDef);

        if (GraphContext.isBreakpoint()) {
            String checkpointUri = "";
            List<ProjectTaskDO> latestTasks = taskRepository.findLastTimeTasks(Objects.requireNonNull(GraphContext.getProject()).getProjectId(), task.getNode().graphNodeId);
            if (!CollectionUtils.isEmpty(latestTasks) && latestTasks.size() == 2 && !ObjectUtils.isEmpty(latestTasks.get(1))) {
                ProjectTaskDO latestTask = latestTasks.get(1);
                List<String> lastTasksOutput = latestTask.getGraphNode().getOutputs();
                if (!lastTasksOutput.isEmpty()) {
                    checkpointUri = JobUtils.genTaskOutputId(CHECKPOINT_PRE + latestTask.getUpk().getJobId(), lastTasksOutput.get(0));
                    pipelineNodeDef = pipelineNodeDef.toBuilder().clearCheckpointUri().setCheckpointUri(checkpointUri).build();
                }
            }
            log.info("breakpoint task {} node {} checkpointUri {}", task.getTaskId(), task.getNode().graphNodeId, checkpointUri);
        } else {
            if (!CollectionUtils.isEmpty(outputs) && StringUtils.isNotEmpty(outputs.get(0))) {
                pipelineNodeDef = pipelineNodeDef.toBuilder().clearCheckpointUri().setCheckpointUri(CHECKPOINT_PRE + outputs.get(0)).build();
            }
        }
        List<String> outputUris = outputs.stream().map(output -> output.replace(DOMAIN_DATA_TABLE_DELIMITER, DATA_TABLE_DELIMITER)).toList();
        if (GraphContext.isScheduled()) {
            outputUris = outputUris.stream().map(output -> output + DATA_TABLE_DELIMITER + GraphContext.getScheduleExpectStartDate()).toList();
        }
        Map<String, TaskConfig.DatasourceConfig> stringDatasourceConfigMap = buildDatasourceConfig(parties, projectId, job.getGraphId());
        Cluster.SFClusterDesc sfClusterDesc = buildSfClusterDesc(parties);
        List<TaskConfig.TableAttr> tableAttrs = GraphContext.getTableAttrs();
        ProjectGraphNodeKusciaParamsDO projectGraphNodeKusciaParamsDO = projectGraphNodeKusciaParamsDOThreadLocal.get();
        projectGraphNodeKusciaParamsDO.setTaskId(task.getTaskId());
        projectGraphNodeKusciaParamsDO.setInputs(JsonUtils.toJSONString(inputs));
        projectGraphNodeKusciaParamsDO.setOutputs(JsonUtils.toJSONString(outputs));
        projectGraphNodeKusciaParamsDO.setNodeEvalParam(ProtoUtils.toJsonString(pipelineNodeDef, typeRegistry));
        projectGraphNodeKusciaParamsRepository.saveAndFlush(projectGraphNodeKusciaParamsDO);
        if (STATS_SCQL_ANALYSIS.equalsIgnoreCase(graphNode.getCodeName())) {
            return new TaskConfigResult(renderScqlInputConfig(job, task, pipelineNodeDef, stringDatasourceConfigMap, typeRegistry, parties, taskParties), JobConstants.SCQL_IMAGE);
        }
        return new TaskConfigResult(renderTaskInputConfig(stringDatasourceConfigMap, task, tableAttrs, outputUris, sfClusterDesc, pipelineNodeDef, typeRegistry), JobConstants.APP_IMAGE);


    }

    private String renderTaskInputConfig(Map<String, TaskConfig.DatasourceConfig> stringDatasourceConfigMap, ProjectJob.JobTask task,
                                         List<TaskConfig.TableAttr> tableAttrs, List<String> outputUris, Cluster.SFClusterDesc sfClusterDesc,
                                         Pipeline.NodeDef pipelineNodeDef, JsonFormat.TypeRegistry typeRegistry) {
        TaskConfig.TaskInputConfig taskInputConfig = TaskConfig.TaskInputConfig.newBuilder()
                .putAllSfDatasourceConfig(stringDatasourceConfigMap)
                .addAllSfInputIds(task.getNode().getInputs())
                .addAllTableAttrs(CollectionUtils.isEmpty(tableAttrs) ? new ArrayList<>() : tableAttrs)
                .addAllSfInputPartitionsSpec(buildSfInputPartitions(task.getNode().getInputs()))
                .addAllSfOutputIds(task.getNode().getOutputs())
                .addAllSfOutputUris(outputUris)
                .setSfClusterDesc(sfClusterDesc)
                .setSfNodeEvalParam(pipelineNodeDef)
                .build();
        return ProtoUtils.toJsonString(taskInputConfig, typeRegistry);
    }

    private String renderScqlInputConfig(ProjectJob job, ProjectJob.JobTask task, Pipeline.NodeDef pipelineNodeDef,
                                         Map<String, TaskConfig.DatasourceConfig> stringDatasourceConfigMap,
                                         JsonFormat.TypeRegistry typeRegistry, List<String> parties, List<Job.Party> taskParties) {
        List<ProjectDatatableDO> datatableDOS = new ArrayList<>();
        List<String> inputs = task.getNode().getInputs();
        String projectId = job.getProjectId();
        ProjectGraphDO graphDO = ownerCheck(projectId, job.getGraphId());
        //Get the graphNodeId of the upstream sample table of the scql analysis component
        Set<String> topNodes = GraphUtils.findTopNodes(graphDO.getEdges(), task.getNode().getGraphNodeId());
        List<String> dataTableIds = new ArrayList<>();
        //Find the dataTableId of the upstream sample table based on the graphNodeId of the upstream sample table
        for (String graphNodeId : topNodes) {
            Optional<ProjectGraphNodeDO> optionalProjectGraphNodeDO = graphNodeRepository.findById(new ProjectGraphNodeDO.UPK(projectId, job.getGraphId(), graphNodeId));
            if(optionalProjectGraphNodeDO.isEmpty()){
                log.error("projectGraphNodeDO not found, projectId: {}, graphId: {}, graphNodeId: {}", projectId, job.getGraphId(), graphNodeId);
                continue;
            }
            String dataTableId = ComponentTools.getDataTableId(GraphNodeInfo.fromDO(optionalProjectGraphNodeDO.get()));
            dataTableIds.add(dataTableId);
        }
        for (String dataTableId : dataTableIds) {
            List<ProjectDatatableDO> projectDatatables = projectDatatableRepository.findByDatableId(projectId, dataTableId);
            datatableDOS.add(projectDatatables.get(0));
        }
        String query = "";
        String initiator = "";
        for (int i = 0; i < pipelineNodeDef.getAttrPathsList().size(); i++) {
            if (SCRIPT_INPUT.equals(pipelineNodeDef.getAttrPaths(i))) {
                query = pipelineNodeDef.getAttrs(i).getFieldsMap().get(ATTRIBUTE_S).getStringValue();
            }
            if (TASK_INITIATOR.equals(pipelineNodeDef.getAttrPaths(i))) {
                initiator = pipelineNodeDef.getAttrs(i).getFieldsMap().get(ATTRIBUTE_SS).getListValue().getValuesList().get(0).getStringValue();
            }
        }
        // If the result receiver is not among the job participants, it needs to be added to the job participants.
        if(!taskParties.contains(initiator)){
            taskParties.add(Job.Party.newBuilder().setDomainId(initiator).build());
        }
        // If the result receiver is not among the job participants, it needs to be added to the job participants, mainly for ccl configuration.
        if(!parties.contains(initiator)){
            parties.add(initiator);
        }
        if (query.isBlank()) {
            log.error("query is blank, task: {}, pipelineNodeDef: {}", task.getTaskId(), pipelineNodeDef);
        }
        Map<String, ScqlTaskConfig.TableList> tableListMap = buildTableList(datatableDOS, stringDatasourceConfigMap, inputs);
        Map<String, ScqlTaskConfig.PrivacyPolicy> privacyPolicyMap = buildPrivacyPolicy(datatableDOS, inputs, parties);
        Map<String, String> outputIds = new HashMap<>();
        if (!task.getNode().getOutputs().isEmpty()) {
            outputIds.put(initiator, task.getNode().getOutputs().get(0));
        }
        ScqlTaskConfig.ScqlTaskInputConfig scqlTaskInputConfig = ScqlTaskConfig.ScqlTaskInputConfig.newBuilder()
                .setProjectId(task.getTaskId().replace(DOMAIN_DATA_TABLE_DELIMITER, StringUtils.EMPTY))
                .setQuery(query)
                .putAllCcls(privacyPolicyMap)
                .putAllOutputIds(outputIds)
                .setInitiator(initiator)
                .putAllTables(tableListMap)
                .build();
        return ProtoUtils.toJsonString(scqlTaskInputConfig, typeRegistry);
    }

    private Map<String, ScqlTaskConfig.PrivacyPolicy> buildPrivacyPolicy(List<ProjectDatatableDO> datatableDOS, List<String> inputs, List<String> parties) {
        Map<String, ScqlTaskConfig.PrivacyPolicy> privacyPolicyMap = new HashMap<>();
        String input = inputs.get(0);
        for (ProjectDatatableDO datatableDO : datatableDOS) {
            List<ProjectDatatableDO.TableColumnConfig> tableConfigs = datatableDO.getTableConfig();
            List<ScqlTaskConfig.ColumnControl> columnControls = new ArrayList<>();

            // buildTableName
            String tableNamePrefix = getTableName(input, datatableDO);
            tableConfigs.forEach(tableConfig -> {
                ScqlTaskConfig.ColumnDef columnDef = buildColumnDef(tableConfig, tableNamePrefix);
                parties.stream()
                        .map(party -> buildColumnControl(tableConfig, columnDef, party, datatableDO.getNodeId()))
                        .forEach(columnControls::add);
            });


            privacyPolicyMap.put(datatableDO.getNodeId(), ScqlTaskConfig.PrivacyPolicy.newBuilder().addAllColumnControlList(columnControls).build());
        }
        return privacyPolicyMap;
    }

    private ScqlTaskConfig.ColumnDef buildColumnDef(ProjectDatatableDO.TableColumnConfig tableConfig, String tableNamePrefix) {
        return ScqlTaskConfig.ColumnDef.newBuilder()
                .setColumnName(tableConfig.getColName())
                .setTableName(tableNamePrefix)
                .build();
    }

    private ScqlTaskConfig.ColumnControl buildColumnControl(ProjectDatatableDO.TableColumnConfig tableConfig,
                                                            ScqlTaskConfig.ColumnDef columnDef, String nodeId, String tableOwnerId) {
        ScqlTaskConfig.ColumnControl.Builder columnControlBuilder = ScqlTaskConfig.ColumnControl.newBuilder()
                .setCol(columnDef)
                .setPartyCode(nodeId);

        // build ccl
        //If it is the initiator, set all fields ccl to PLAINTEXT
        if (tableOwnerId.equals(nodeId)) {
            return columnControlBuilder.setConstraint(ScqlTaskConfig.Constraint.PLAINTEXT).build();
        }
        if (tableConfig.isProtection()) {
            columnControlBuilder.setConstraint(getConstraint(tableConfig));
        } else {
            columnControlBuilder.setConstraint(ScqlTaskConfig.Constraint.PLAINTEXT);
        }

        return columnControlBuilder.build();
    }

    private ScqlTaskConfig.Constraint getConstraint(ProjectDatatableDO.TableColumnConfig tableConfig) {
        if (tableConfig.isAssociateKey()) {
            return ScqlTaskConfig.Constraint.PLAINTEXT_AFTER_JOIN;
        } else if (tableConfig.isGroupKey()) {
            return ScqlTaskConfig.Constraint.PLAINTEXT_AFTER_GROUP_BY;
        } else {
            return ScqlTaskConfig.Constraint.PLAINTEXT_AFTER_AGGREGATE;
        }
    }

    private Map<String, ScqlTaskConfig.TableList> buildTableList(List<ProjectDatatableDO> datatableDOS, Map<String, TaskConfig.DatasourceConfig> stringDatasourceConfigMap, List<String> inputs) {
        Map<String, ScqlTaskConfig.TableList> tableListMap = new HashMap<>();
        String input = inputs.get(0);
        for (ProjectDatatableDO datatableDO : datatableDOS) {
            //get datasource config
            TaskConfig.DatasourceConfig datasourceConfig = stringDatasourceConfigMap.get(datatableDO.getNodeId());
            List<ProjectDatatableDO.TableColumnConfig> tableConfigs = datatableDO.getTableConfig();
            List<ScqlTaskConfig.TableMeta.Column> columnList = tableConfigs.stream()
                    .map(tableConfig -> ScqlTaskConfig.TableMeta.Column.newBuilder()
                            .setName(tableConfig.getColName())
                            .setDtype(tableConfig.getColType()).build())
                    .toList();
            ScqlTaskConfig.TableMeta tableMeta = ScqlTaskConfig.TableMeta.newBuilder()
                    .setTableName(getTableName(input, datatableDO))
                    //When directly connecting to the sample table, use the original table id as tableName.
                    .setTableOwner(datatableDO.getNodeId())
                    .setRefTable(input)
                    .setDbType(getDbType(datasourceConfig))
                    .addAllColumns(columnList)
                    .build();

            tableListMap.put(datatableDO.getNodeId(), ScqlTaskConfig.TableList.newBuilder().addTbls(tableMeta).build());
        }

        return tableListMap;
    }

    private static String getTableName(String input, ProjectDatatableDO datatableDO) {
        return input.contains(DOMAIN_DATA_TABLE_DELIMITER)
                ? (datatableDO.getNodeId() + input.substring(JOB_ID_LENGTH)).replaceAll(DOMAIN_DATA_TABLE_DELIMITER, DATA_TABLE_DELIMITER)
                : datatableDO.getNodeId().replaceAll(DOMAIN_DATA_TABLE_DELIMITER, DATA_TABLE_DELIMITER) + DATA_TABLE_DELIMITER + input;
    }

    private static @NotNull String getDbType(TaskConfig.DatasourceConfig datasourceConfig) {
        String id = datasourceConfig.getId();
        if (id.startsWith(DEFAULT_MYSQL_DATASOURCE_TYPE.toLowerCase())) {
            return DEFAULT_MYSQL_DATASOURCE_TYPE.toLowerCase();
        }
        if (id.startsWith(DEFAULT_ODPS_DATASOURCE_TYPE.toLowerCase())) {
            return DEFAULT_ODPS_DATASOURCE_TYPE.toLowerCase();
        }
        return CSV_DB_TYPE;
    }

    /**
     * Set default map of party and datasource config
     *
     * @param parties target parties
     * @return map of party and datasource config
     */
    public Map<String, TaskConfig.DatasourceConfig> buildDatasourceConfig(List<String> parties, String projectId, String graphId) {
        TaskConfig.DatasourceConfig datasourceConfig = TaskConfig.DatasourceConfig.newBuilder()
                .setId(DEFAULTDS)
                .build();

        Map<String, TaskConfig.DatasourceConfig> datasourceConfigMap = new HashMap<>();
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = null;
        for (String party : parties) {
            if (StringUtils.isNotEmpty(projectId) && StringUtils.isNotEmpty(graphId)) {
                projectGraphDomainDatasourceDO = projectGraphDomainDatasourceService.getById(projectId, graphId, party);
            }
            if (projectGraphDomainDatasourceDO == null || StringUtils.isEmpty(projectGraphDomainDatasourceDO.getDataSourceId())) {
                datasourceConfigMap.put(party, datasourceConfig);
            } else {
                datasourceConfigMap.put(party, datasourceConfig.toBuilder().setId(projectGraphDomainDatasourceDO.getDataSourceId()).build());
            }
        }
        return datasourceConfigMap;
    }

    /**
     * Set default map of party and datasource config
     *
     * @param parties target parties
     * @return map of party and datasource config
     */
    public Map<String, TaskConfig.DatasourceConfig> modelExportDatasourceConfig(List<String> parties, String projectId, String graphId, ModelExportPackageRequest request) {
        Map<String, TaskConfig.DatasourceConfig> stringDatasourceConfigMap = buildDatasourceConfig(parties, projectId, graphId);
        if (ObjectUtils.isEmpty(request) || ObjectUtils.isEmpty(request.getModelPartyConfig())) {
            return buildDatasourceConfig(parties, request.getProjectId(), graphId);
        }
        for (ModelPartyConfig modelPartyConfig : request.getModelPartyConfig()) {
            if (stringDatasourceConfigMap.containsKey(modelPartyConfig.getModelParty())) {
                stringDatasourceConfigMap.put(modelPartyConfig.getModelParty(), stringDatasourceConfigMap.get(modelPartyConfig.getModelParty()).toBuilder().setId(modelPartyConfig.getModelDataSource()).build());
            }
        }
        return stringDatasourceConfigMap;
    }

    public Cluster.SFClusterDesc buildSfClusterDesc(List<String> parties) {
        List<Cluster.SFClusterDesc.DeviceDesc> deviceDescs = new ArrayList<>();
        deviceConfig.forEach((key, value) -> {
            Cluster.SFClusterDesc.DeviceDesc deviceDesc = Cluster.SFClusterDesc.DeviceDesc.newBuilder()
                    .setType(key)
                    .setName(key)
                    .addAllParties(parties)
                    .setConfig(value)
                    .build();
            deviceDescs.add(deviceDesc);
        });
        Cluster.SFClusterDesc.RayFedConfig rayFedConfig = Cluster.SFClusterDesc.RayFedConfig.newBuilder()
                .setCrossSiloCommBackend(crossSiloCommBackend)
                .build();
        return Cluster.SFClusterDesc.newBuilder().addAllParties(parties).setRayFedConfig(rayFedConfig).addAllDevices(deviceDescs).build();
    }

    public List<String> buildSfInputPartitions(List<String> inputs) {
        List<String> sf_input_partitions_spec = new ArrayList<>(inputs.size());
        for (String input : inputs) {
            Map<String, GraphContext.PartitionInfo> tablePartitionMap = ObjectUtils.isEmpty(GraphContext.getTablePartitionRule()) ? new HashMap<>() : GraphContext.getTablePartitionRule();
            GraphContext.PartitionInfo partitionInfo = tablePartitionMap.getOrDefault(input, new GraphContext.PartitionInfo());
            String tablePartitionRule = partitionInfo.getReadRule();
            tablePartitionRule = StringUtils.isNotEmpty(tablePartitionRule)
                    ? tablePartitionRule.replace(ComponentConstants.DATA_TABLE_PARTITION_DELIMITER, ComponentConstants.DOMAIN_DATA_TABLE_PARTITION_DELIMITER)
                    : "";
            String tableName = partitionInfo.getTableName();
            String scheduleExpectStartDate = GraphContext.getScheduleExpectStartDate();
            Set<String> partitionColumns = partitionInfo.getPartitionColumns();
            sf_input_partitions_spec.add(readPartitionRuleAnalysisService.readPartitionRuleAnalysis(tableName, DataSourceTypeEnum.ODPS, tablePartitionRule, scheduleExpectStartDate, partitionColumns));
        }
        return sf_input_partitions_spec;
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

}
