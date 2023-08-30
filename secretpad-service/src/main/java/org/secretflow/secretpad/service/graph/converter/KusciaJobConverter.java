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

import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.service.constant.JobConstants;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import com.google.protobuf.util.JsonFormat;
import org.secretflow.proto.component.Cluster;
import org.secretflow.proto.component.Data;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.proto.pipeline.Pipeline;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Job converter for message in apiLite
 *
 * @author yansi
 * @date 2023/5/30
 */
@Component
public class KusciaJobConverter implements JobConverter {
    private final static String defaultDS = "default-data-source";
    @Value("${job.max-parallelism:1}")
    private int maxParallelism;
    private static final Map<String, String> deviceConfig = Map.of
            (
                    "spu", "{\"runtime_config\":{\"protocol\":\"REF2K\",\"field\":\"FM64\"},\"link_desc\":{\"connect_retry_times\":60,\"connect_retry_interval_ms\":1000,\"brpc_channel_protocol\":\"http\",\"brpc_channel_connection_type\":\"pooled\",\"recv_timeout_ms\":1200000,\"http_timeout_ms\":1200000}}",
                    "heu", "{\"mode\": \"PHEU\", \"schema\": \"paillier\", \"key_size\": 2048}"
            );

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
                    taskParties = parties.stream().map(party -> Job.Party.newBuilder().setDomainId(party).build()).collect(Collectors.toList());
                }
                String taskInputConfig = renderTaskInputConfig(task);
                Job.Task.Builder jobTaskBuilder = Job.Task.newBuilder()
                        .setTaskId(taskId)
                        .setAlias(taskId)
                        .setAppImage(JobConstants.APP_IMAGE)
                        .addAllParties(taskParties)
                        .setTaskInputConfig(taskInputConfig);
                if (!CollectionUtils.isEmpty(task.getDependencies())) {
                    jobTaskBuilder.addAllDependencies(task.getDependencies());
                }
                jobTasks.add(jobTaskBuilder.build());
            }
        }
        return Job.CreateJobRequest.newBuilder()
                .setJobId(job.getJobId())
                .setInitiator(initiator)
                .setMaxParallelism(maxParallelism)
                .addAllTasks(jobTasks)
                .build();
    }

    /**
     * Render task input config message from project job task
     *
     * @param task project job task
     * @return json string of task input config message
     */
    private String renderTaskInputConfig(ProjectJob.JobTask task) {
        GraphNodeInfo graphNode = task.getNode();
        Object nodeDef = graphNode.getNodeDef();
        List<String> inputs = graphNode.getInputs();
        List<String> outputs = graphNode.getOutputs();
        List<String> parties = task.getParties();
        List<Cluster.SFClusterDesc.DeviceDesc> deviceDescs = new ArrayList<>();
        deviceConfig.entrySet().forEach(entry -> {
            Cluster.SFClusterDesc.DeviceDesc deviceDesc = Cluster.SFClusterDesc.DeviceDesc.newBuilder()
                    .setType(entry.getKey())
                    .setName(entry.getKey())
                    .addAllParties(parties)
                    .setConfig(entry.getValue())
                    .build();
            deviceDescs.add(deviceDesc);
        });

        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(Data.IndividualTable.getDescriptor()).build();
        Pipeline.NodeDef pipelineNodeDef;
        if (nodeDef instanceof Pipeline.NodeDef) {
            pipelineNodeDef = (Pipeline.NodeDef) nodeDef;
        } else {
            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            pipelineNodeDef = (Pipeline.NodeDef) ProtoUtils.fromObject(nodeDef, nodeDefBuilder);
        }

        Cluster.SFClusterDesc sfClusterDesc = Cluster.SFClusterDesc.newBuilder().addAllParties(parties).addAllDevices(deviceDescs).build();
        TaskConfig.TaskInputConfig taskInputConfig = TaskConfig.TaskInputConfig.newBuilder()
                .putAllSfDatasourceConfig(defaultDatasourceConfig(parties))
                .addAllSfInputIds(inputs)
                .addAllSfOutputIds(outputs)
                .addAllSfOutputUris(outputs)
                .setSfClusterDesc(sfClusterDesc)
                .setSfNodeEvalParam(pipelineNodeDef)
                .build();
        return ProtoUtils.toJsonString(taskInputConfig, typeRegistry);
    }

    /**
     * Set default map of party and datasource config
     *
     * @param parties target parties
     * @return map of party and datasource config
     */
    private Map<String, TaskConfig.DatasourceConfig> defaultDatasourceConfig(List<String> parties) {
        TaskConfig.DatasourceConfig datasourceConfig = TaskConfig.DatasourceConfig.newBuilder()
                .setId(defaultDS)
                .build();
        Map<String, TaskConfig.DatasourceConfig> datasourceConfigMap = new HashMap<>();
        for (String party : parties) {
            datasourceConfigMap.put(party, datasourceConfig);
        }
        return datasourceConfigMap;
    }
}
