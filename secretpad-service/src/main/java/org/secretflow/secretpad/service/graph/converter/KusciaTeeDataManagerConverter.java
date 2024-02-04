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

import org.secretflow.secretpad.common.constant.ComponentConstants;
import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.service.constant.JobConstants;
import org.secretflow.secretpad.service.model.datatable.TeeJob;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import com.google.common.collect.Lists;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.secretflow.spec.v1.DistData;
import org.apache.commons.lang3.StringUtils;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.proto.pipeline.Pipeline;
import org.secretflow.proto.pipeline.TaskConfigOuterClass;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.secretflow.secretpad.service.constant.TeeJobConstants.*;

/**
 * Tee job converter for message in apiLite
 *
 * @author xujiening
 * @date 2023/9/20
 */
@Component
public class KusciaTeeDataManagerConverter implements JobConverter {
    private final static String DEFAULT_DS = "default-data-source";

    @Value("${job.max-parallelism:1}")
    private int maxParallelism;
    @Value("${tee.capsule-manager:capsule-manager.#.svc}")
    private String teeCapsuleMana;

    /**
     * Converter create job request from tee job
     *
     * @param job tee job class
     * @return create job request message
     */
    public Job.CreateJobRequest converter(TeeJob job) {
        List<ProjectJob.JobTask> tasks = job.getTasks();
        List<Job.Task> jobTasks = convertJobTasks(tasks, job);
        return Job.CreateJobRequest.newBuilder()
                .setJobId(job.getJobId())
                .setInitiator(CollectionUtils.isEmpty(tasks) || CollectionUtils.isEmpty(tasks.get(0).getParties()) ? "" : tasks.get(0).getParties().get(0))
                .setMaxParallelism(maxParallelism)
                .addAllTasks(jobTasks)
                .build();
    }

    /**
     * Convert job task list to kuscia job task list
     *
     * @param tasks  project job task list
     * @param teeJob tee job
     * @return kuscia job task list
     */
    private List<Job.Task> convertJobTasks(List<ProjectJob.JobTask> tasks, TeeJob teeJob) {
        if (CollectionUtils.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        List<Job.Task> jobTasks = new ArrayList<>();
        for (ProjectJob.JobTask task : tasks) {
            String taskId = task.getTaskId();
            List<Job.Party> taskParties = new ArrayList<>();
            List<String> parties = task.getParties();
            if (!CollectionUtils.isEmpty(parties)) {
                taskParties = parties.stream().map(party -> Job.Party.newBuilder().setDomainId(party).build()).collect(Collectors.toList());
            }
            String taskInputConfig = renderTaskInputConfig(task, teeJob);
            Job.Task.Builder jobTaskBuilder = Job.Task.newBuilder()
                    .setTaskId(taskId)
                    .setAlias(taskId)
                    .setAppImage(JobConstants.TEE_IMAGE)
                    .addAllParties(taskParties)
                    .setTaskInputConfig(taskInputConfig);
            if (!CollectionUtils.isEmpty(task.getDependencies())) {
                jobTaskBuilder.addAllDependencies(task.getDependencies());
            }
            jobTasks.add(jobTaskBuilder.build());
        }
        return jobTasks;
    }

    /**
     * Render task input config message from project job task and tee job
     *
     * @param task project job task
     * @param job  tee job
     * @return json string of task input config message
     */
    private String renderTaskInputConfig(ProjectJob.JobTask task, TeeJob job) {
        List<String> parties = task.getParties();

        // build tee runtime config
        TaskConfigOuterClass.TaskConfig.Builder taskConfigBuilder = TaskConfigOuterClass.TaskConfig.newBuilder()
                .setCapsuleManagerEndpoint(getRealTeeCapsuleMana(job.getTeeNodeId()));

        // build basic task input config builder
        TaskConfig.TaskInputConfig.Builder taskInputConfigBuilder = TaskConfig.TaskInputConfig.newBuilder();
        taskInputConfigBuilder.putAllSfDatasourceConfig(defaultDatasourceConfig(parties));

        Pipeline.NodeDef.Builder nodeEvalParamBuilder = Pipeline.NodeDef.newBuilder();
        DistData.Builder distDataBuilder = DistData.newBuilder();
        String outputUri = "";
        // build input config with different tee job kind
        switch (job.getKind()) {
            case Push -> {
                String prefix = job.getTeeNodeId() + "-";
                distDataBuilder
                        .setName(PUSH_INPUT_NAME)
                        .setType(PUSH_INPUT_TYPE)
                        .addAllDataRefs(List.of(DistData.DataRef.newBuilder()
                                .setUri(buildUri(job.getDatatableId(), prefix)).build()));
                outputUri = buildOutputUriString(job);

                // build node eval param, push to tee does not need cert, kuscia will get from config manager
                nodeEvalParamBuilder.setDomain(DATA_MANAGEMENT)
                        .setName(TEE_PUSH_NAME)
                        .setVersion(VERSION)
                        .addAllAttrPaths(List.of(TEE_PUSH_ATTR_PATH_DOMAIN, TEE_PUSH_ATTR_PATH_CERT))
                        .addAllAttrs(List.of(
                                Struct.newBuilder().putFields(ComponentConstants.ATTRIBUTE_S, com.google.protobuf.Value.newBuilder().setStringValue(job.getNodeId()).build()).build(),
                                Struct.newBuilder().
                                        putFields(
                                                ComponentConstants.ATTRIBUTE_SS,
                                                com.google.protobuf.Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(Collections.emptyList()).build()).build())
                                        .build()
                        ))
                        .addAllInputs(List.of(distDataBuilder.build()))
                        .addAllOutputUris(List.of(outputUri));
                taskInputConfigBuilder.setSfNodeEvalParam(nodeEvalParamBuilder.build());
            }
            case Delete -> {
                distDataBuilder
                        .setName(DELETE_INPUT_NAME)
                        .setType(PUSH_INPUT_TYPE)
                        .addAllDataRefs(List.of(DistData.DataRef.newBuilder()
                                .setUri(buildUri(job.getDatatableId(), "")).build()));

                // build node eval param, delete from tee does not need cert, kuscia will get from config manager
                nodeEvalParamBuilder.setDomain(DATA_MANAGEMENT)
                        .setName(TEE_DELETE_NAME)
                        .setVersion(VERSION)
                        .addAllAttrPaths(List.of(TEE_DELETE_ATTR_PATH_DOMAIN))
                        .addAllAttrs(List.of(
                                Struct.newBuilder().putFields(ComponentConstants.ATTRIBUTE_S, com.google.protobuf.Value.newBuilder().setStringValue(job.getNodeId()).build()).build()
                        ))
                        .addAllInputs(List.of(distDataBuilder.build()));
                taskInputConfigBuilder.setSfNodeEvalParam(nodeEvalParamBuilder.build());
            }
            case Auth -> {
                distDataBuilder
                        .setName(AUTH_INPUT_NAME)
                        .setType(PUSH_INPUT_TYPE)
                        .addAllDataRefs(List.of(DistData.DataRef.newBuilder()
                                .setUri(buildUri(job.getDatatableId(), "")).build()));

                //
                List<String> authNodeIds = job.getAuthNodeIds();
                List<com.google.protobuf.Value> authNodeIdValueList = new ArrayList<>();
                authNodeIds.forEach(authNodeId -> {
                    authNodeIdValueList.add(com.google.protobuf.Value.newBuilder().setStringValue(authNodeId).build());
                });
                List<String> authNodeCerts = job.getAuthNodeCerts();
                List<com.google.protobuf.Value> authNodeCertValueList = new ArrayList<>();
                authNodeCerts.forEach(authNodeCert -> {
                    authNodeCertValueList.add(com.google.protobuf.Value.newBuilder().setStringValue(authNodeCert).build());
                });
                // build node eval param
                nodeEvalParamBuilder.setDomain(DATA_MANAGEMENT)
                        .setName(TEE_AUTH_NAME)
                        .setVersion(VERSION)
                        .addAllAttrPaths(Lists.newArrayList(TEE_AUTH_ATTR_OWNER_DOMAIN, TEE_AUTH_ATTR_AUTH_DOMAINS,
                                TEE_AUTH_ATTR_ROOT_CERTS, TEE_AUTH_ATTR_COLUMNS, TEE_AUTH_ATTR_PROJECT_ID))
                        .addAllAttrs(
                                Lists.newArrayList(
                                        Struct.newBuilder().putFields(ComponentConstants.ATTRIBUTE_S, com.google.protobuf.Value.newBuilder().setStringValue(job.getNodeId()).build()).build(),
                                        Struct.newBuilder().
                                                putFields(ComponentConstants.ATTRIBUTE_SS, com.google.protobuf.Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(authNodeIdValueList).build()).build())
                                                .build(),
                                        Struct.newBuilder().
                                                putFields(ComponentConstants.ATTRIBUTE_SS, com.google.protobuf.Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(authNodeCertValueList).build()).build())
                                                .build(),
                                        Struct.newBuilder().
                                                putFields(ComponentConstants.ATTRIBUTE_SS, com.google.protobuf.Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(Lists.newArrayList(com.google.protobuf.Value.newBuilder().setStringValue("*").build())).build()).build())
                                                .build(),
                                        Struct.newBuilder().putFields(ComponentConstants.ATTRIBUTE_S, com.google.protobuf.Value.newBuilder().setStringValue(job.getProjectId()).build()).build()
                                )
                        )
                        .addAllInputs(List.of(distDataBuilder.build()));
                taskInputConfigBuilder.setSfNodeEvalParam(nodeEvalParamBuilder.build());
                taskConfigBuilder.setScope(job.getProjectId());
            }
            case Pull -> {
                String edgePrefix = job.getNodeId() + "-";
                distDataBuilder
                        .setName(PULL_INPUT_NAME)
                        .setType(PUSH_INPUT_TYPE)
                        .addAllDataRefs(List.of(DistData.DataRef.newBuilder()
                                .setUri(buildUri(job.getDatatableId(), edgePrefix)).build()));
                outputUri = buildOutputUriString(job);

                // build node eval param, push to tee does not need cert, kuscia will get from config manager
                nodeEvalParamBuilder.setDomain(DATA_MANAGEMENT)
                        .setName(TEE_PULL_NAME)
                        .setVersion(VERSION)
                        .addAllAttrPaths(List.of(TEE_PULL_ATTR_PATH_DOMAIN, TEE_PULL_ATTR_VOTE_RESULT))
                        .addAllAttrs(List.of(
                                Struct.newBuilder().putFields(ComponentConstants.ATTRIBUTE_S, com.google.protobuf.Value.newBuilder().setStringValue(job.getNodeId()).build()).build(),
                                Struct.newBuilder().
                                        putFields(ComponentConstants.ATTRIBUTE_S, com.google.protobuf.Value.newBuilder().setStringValue(job.getVoteResult()).build())
                                        .build()
                        ))
                        .addAllInputs(List.of(distDataBuilder.build()))
                        .addAllOutputUris(List.of(outputUri));
                taskInputConfigBuilder.setSfNodeEvalParam(nodeEvalParamBuilder.build());
            }
            default -> {
            }
        }
        taskInputConfigBuilder.setTeeTaskConfig(taskConfigBuilder.build());
        return decode(ProtoUtils.toJsonString(taskInputConfigBuilder.build()));
    }


    /**
     * Build output string via tee job
     *
     * @param job target tee job
     * @return output string
     */
    private String buildOutputUriString(TeeJob job) {
        return StringUtils.isBlank(job.getRelativeUri()) ?
                OUTPUT_DATASOURCE_REF + job.getDatasourceId() + OUTPUT_ID_REF + job.getDatatableId() :
                OUTPUT_DATASOURCE_REF + job.getDatasourceId() + OUTPUT_ID_REF + job.getDatatableId()
                        + OUTPUT_RELATIVE_URI_REF + job.getRelativeUri();
    }

    /**
     * Build uri via datatableId and prefix
     *
     * @param datatableId target datatableId
     * @param prefix      target prefix
     * @return uri string
     */
    private String buildUri(String datatableId, String prefix) {
        return StringUtils.isBlank(prefix) ? DATA_REF + datatableId : DATA_REF + datatableId.replace(prefix, "");
    }


    /**
     * Set default map of party and datasource config
     *
     * @param parties target parties
     * @return map of party and datasource config
     */
    private Map<String, TaskConfig.DatasourceConfig> defaultDatasourceConfig(List<String> parties) {
        TaskConfig.DatasourceConfig datasourceConfig = TaskConfig.DatasourceConfig.newBuilder()
                .setId(DEFAULT_DS)
                .build();
        Map<String, TaskConfig.DatasourceConfig> datasourceConfigMap = new HashMap<>();
        for (String party : parties) {
            datasourceConfigMap.put(party, datasourceConfig);
        }
        return datasourceConfigMap;
    }

    private String getRealTeeCapsuleMana(String teeDomainId) {
        return teeCapsuleMana.replace("#", teeDomainId);
    }

    /**
     * Build tee datatableId avoid repeated
     *
     * @param nodeId      node id
     * @param datatableId datatable id
     * @return tee datatable id
     */
    public String buildTeeDatatableId(String nodeId, String datatableId) {
        return nodeId + "-" + datatableId;
    }
}
