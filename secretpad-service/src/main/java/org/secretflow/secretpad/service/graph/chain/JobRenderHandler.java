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

package org.secretflow.secretpad.service.graph.chain;

import org.secretflow.secretpad.common.errorcode.DatatableErrorCode;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.model.DatatableDTO;
import org.secretflow.secretpad.persistence.entity.ProjectDatatableDO;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.repository.ProjectDatatableRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskRepository;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.graph.ComponentTools;
import org.secretflow.secretpad.service.graph.DistDataVO;
import org.secretflow.secretpad.service.graph.GraphBuilder;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.graph.adapter.NodeDefAdapterFactory;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;
import org.secretflow.secretpad.service.util.JobUtils;

import com.secretflow.spec.v1.DistData;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Render job params, including inputs and outputs
 *
 * @author yansi
 * @date 2023/6/8
 */
@Slf4j
@Component
public class JobRenderHandler extends AbstractJobHandler<ProjectJob> {
    @Resource
    private ComponentService componentService;
    @Resource
    private ProjectDatatableRepository datatableRepository;
    @Resource
    private AbstractDatatableManager datatableManager;
    @Resource
    private ProjectJobTaskRepository taskRepository;

    @Resource
    private NodeDefAdapterFactory nodeDefAdapterFactory;

    /**
     * Render job inputs, outputs and prune the secretpad component job
     *
     * @param job target job
     */
    @Override
    public void doHandler(ProjectJob job) {
        ProjectJob newJob = JsonUtils.toJavaObject(JsonUtils.toJSONString(job), ProjectJob.class);
        renderInputs(newJob);
        renderOutputs(newJob);
        renderCustomizeSpec(newJob);
        pruneJob(newJob);
        if (next != null) {
            next.doHandler(newJob);
        }
    }

    /**
     * Render job inputs
     *
     * @param job target job
     */
    void renderInputs(ProjectJob job) {
        String projectId = job.getProjectId();
        GraphBuilder graphBuilder = new GraphBuilder(job.getFullNodes());
        List<ProjectJob.JobTask> jobTasks = job.getTasks();
        List<String> selectedNodes = jobTasks.stream().map(task -> task.getNode().getGraphNodeId()).toList();
        for (ProjectJob.JobTask task : jobTasks) {
            GraphNodeInfo graphNodeInfo = task.getNode();
            List<String> newInputs = new ArrayList<>();
            List<String> inputs = graphNodeInfo.getInputs();
            List<String> dependencies = new ArrayList<>();
            if (componentService.isSecretpadComponent(graphNodeInfo)) {
                continue;
            }

            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            ProtoUtils.fromObject(graphNodeInfo.getNodeDef(), nodeDefBuilder);
            nodeDefBuilder = ComponentTools.coverAttrByCustomAttr(nodeDefBuilder.build()).toBuilder();
            if (!CollectionUtils.isEmpty(inputs)) {
                for (String input : inputs) {
                    GraphNodeInfo dependencyGraphNode = graphBuilder.getNodeByInputId(input);
                    String dependencyGraphNodeId = dependencyGraphNode.getGraphNodeId();

                    if (componentService.isSecretpadComponent(dependencyGraphNode)) {
                        // dependency graph node is read data
                        if (!selectedNodes.contains(dependencyGraphNodeId)) {
                            // read data not exists in selectNodes, must have been run
                            Optional<ProjectTaskDO> projectTaskDOOptional = taskRepository.findLatestTasks(projectId, dependencyGraphNodeId);
                            if (projectTaskDOOptional.isEmpty()) {
                                throw SecretpadException.of(GraphErrorCode.GRAPH_DEPENDENT_NODE_NOT_RUN, dependencyGraphNodeId);
                            }
                        }
                        String datatableId = ComponentTools.getDataTableId(dependencyGraphNode);
                        List<ProjectDatatableDO> datatableDOS = datatableRepository.findByDatableId(projectId, datatableId);
                        if (CollectionUtils.isEmpty(datatableDOS)) {
                            throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
                        }
                        for (ProjectDatatableDO projectDatatableDO : datatableDOS) {
                            Optional<DatatableDTO> datatableDTOOptional = datatableManager.findById(DatatableDTO.NodeDatatableId.from(projectDatatableDO.getUpk().getNodeId(), datatableId));
                            if (datatableDTOOptional.isEmpty()) {
                                throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
                            }
                            DistData distData = DistDataVO.fromDatatable(projectDatatableDO, datatableDTOOptional.get());
                            nodeDefBuilder.addInputs(distData);
                        }
                        if (GraphContext.isTee()) {
                            newInputs.add(GraphContext.getTeeNodeId() + "-" + datatableId);
                        } else {
                            newInputs.add(datatableId);
                        }
                    } else {
                        // dependency graph node is sf
                        if (selectedNodes.contains(dependencyGraphNodeId)) {
                            // dependency sf graph node exists in selectNodes
                            newInputs.add(input);
                            dependencies.add(JobUtils.genTaskId(job.getJobId(), dependencyGraphNodeId));
                        } else {
                            // dependency sf graph node not exists in selectNodes
                            Optional<ProjectTaskDO> projectTaskDOOptional = taskRepository.findLatestTasks(projectId, dependencyGraphNodeId);
                            if (projectTaskDOOptional.isEmpty()) {
                                throw SecretpadException.of(DatatableErrorCode.DATATABLE_NOT_EXISTS);
                            }
                            String jobId = projectTaskDOOptional.get().getUpk().getJobId();
                            String taskOutputId = JobUtils.genTaskOutputId(jobId, input);
                            newInputs.add(taskOutputId);
                        }
                    }
                }
            }
            graphNodeInfo.setNodeDef(nodeDefBuilder.build());
            graphNodeInfo.setInputs(newInputs);
            task.setDependencies(dependencies);
        }
    }

    /**
     * Render job outputs
     *
     * @param job target job
     */
    void renderOutputs(ProjectJob job) {
        String jobId = job.getJobId();
        List<ProjectJob.JobTask> tasks = job.getTasks();
        if (!CollectionUtils.isEmpty(tasks)) {
            Map<String, String> outputMap = new HashMap<>();
            for (ProjectJob.JobTask task : tasks) {
                GraphNodeInfo graphNode = task.getNode();
                List<String> outputs = graphNode.getOutputs();
                if (!CollectionUtils.isEmpty(outputs)) {
                    List<String> newOuts = new ArrayList<>();
                    for (String output : outputs) {
                        String newOutput = JobUtils.genTaskOutputId(jobId, output);
                        outputMap.put(output, newOutput);
                        newOuts.add(newOutput);
                    }
                    graphNode.setOutputs(newOuts);
                }
            }

            for (ProjectJob.JobTask task : tasks) {
                GraphNodeInfo graphNode = task.getNode();
                List<String> inputs = graphNode.getInputs();
                if (!CollectionUtils.isEmpty(inputs)) {
                    List<String> newInputs = new ArrayList<>();
                    for (String input : inputs) {
                        newInputs.add(outputMap.getOrDefault(input, input));
                    }
                    graphNode.setInputs(newInputs);
                }
            }
        }
    }

    /**
     * Prune the secretpad component job
     *
     * @param job target job
     */
    void pruneJob(ProjectJob job) {
        List<ProjectJob.JobTask> newTasks = job.getTasks().stream().filter(task -> !componentService.isSecretpadComponent(task.getNode())).collect(Collectors.toList());
        job.setTasks(newTasks);
    }

    /**
     * Render CustomizeSpec job
     *
     * @param job target job
     */
    void renderCustomizeSpec(ProjectJob job) {
        List<ProjectJob.JobTask> jobTasks = job.getTasks();
        List<ProjectJob.JobTask> tasks = new ArrayList<>(jobTasks);
        Map<Integer, ProjectJob.JobTask> map = new HashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            ProjectJob.JobTask task = tasks.get(i);
            GraphNodeInfo graphNodeInfo = task.getNode();
            Object nodeDef = graphNodeInfo.getNodeDef();
            Pipeline.NodeDef pipelineNodeDef;
            if (nodeDef instanceof Pipeline.NodeDef) {
                pipelineNodeDef = (Pipeline.NodeDef) nodeDef;
            } else {
                Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
                pipelineNodeDef = (Pipeline.NodeDef) ProtoUtils.fromObject(nodeDef, nodeDefBuilder);
            }
            ProjectJob.JobTask process = nodeDefAdapterFactory.process(pipelineNodeDef, graphNodeInfo, task);
            if (!ObjectUtils.isEmpty(process)) {
                log.info("extendTask tasks :{} ", process);
                map.put(i, process);
            } else {
                jobTasks.set(i, task);
            }
        }
        for (Map.Entry<Integer, ProjectJob.JobTask> entry : map.entrySet()) {
            jobTasks.add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
