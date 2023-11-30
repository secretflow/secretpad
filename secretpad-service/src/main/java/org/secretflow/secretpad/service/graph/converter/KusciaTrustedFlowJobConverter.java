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

import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.*;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.service.constant.JobConstants;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.DistData;
import com.secretflow.spec.v1.IndividualTable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.secretflow.proto.kuscia.TaskConfig;
import org.secretflow.proto.pipeline.Pipeline;
import org.secretflow.proto.pipeline.TaskConfigOuterClass;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yutu
 * @date 2023/10/09
 */
@Slf4j
@Component
public class KusciaTrustedFlowJobConverter implements JobConverter {
    @Value("${job.max-parallelism:1}")
    private int maxParallelism;
    @Value("${tee.capsule-manager:capsule-manager.#.svc}")
    private String teeCapsuleMana;

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
                        .setAppImage(JobConstants.TEE_APP_IMAGE)
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
        ProjectDO project = GraphContext.getProject();
        String teeNodeId = GraphContext.getTeeNodeId();
        assert project != null;
        String projectId = project.getProjectId();
        GraphNodeInfo graphNode = task.getNode();
        Object nodeDef = graphNode.getNodeDef();
        List<String> outputs = graphNode.getOutputs();
        List<String> parties = task.getParties();
        String taskInitiatorId, signRSA256;
        List<String> certs;

        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(IndividualTable.getDescriptor()).build();
        Pipeline.NodeDef pipelineNodeDef = (Pipeline.NodeDef) nodeDef;
        List<DistData> pipelineNodeDefInputsList = new ArrayList<>();
        List<String> inputs = graphNode.getInputs();
        for (String input : inputs) {
            DistData.Builder distDataBuilder = DistData.newBuilder();
            DistData.DataRef dataRef = DistData.DataRef.newBuilder()
                    .clearUri()
                    .setUri(buildDmInputUrl(input))
                    .build();
            pipelineNodeDefInputsList.add(distDataBuilder.addDataRefs(dataRef).build());
        }
        Pipeline.NodeDef.Builder PipelineNodeDefBuilder = Pipeline.NodeDef.newBuilder();
        Pipeline.NodeDef newPipelineNodeDef = PipelineNodeDefBuilder
                .mergeFrom(pipelineNodeDef)
                .clearInputs()
                .addAllInputs(pipelineNodeDefInputsList)
                .addAllOutputUris(outputs.stream().map(this::buildDmOutputUrl).toList())
                .build();
        String taskBody = ProtoUtils.toJsonString(newPipelineNodeDef, typeRegistry);

        try {
            log.info("tee node_eval_params {}", taskBody);
            taskBody = Base64.getEncoder().encodeToString(taskBody.getBytes());
            taskBody = decode(taskBody);
            taskInitiatorId = getTaskInitiatorId();
            signRSA256 = signRSA256(taskInitiatorId + "." + projectId + "." + taskBody);
            certs = getCerts();
        } catch (Exception e) {
            log.error("tee sign error", e);
            throw SecretpadException.of(GraphErrorCode.GRAPH_DEPENDENT_NODE_NOT_RUN, e);
        }
        TaskConfigOuterClass.TaskConfig sfTeeConfig = TaskConfigOuterClass.TaskConfig.newBuilder()
                .setTaskInitiatorId(taskInitiatorId)
                .setScope(projectId)
                .addAllTaskInitiatorCerts(certs)
                .setTaskBody(taskBody)
                .setSignature(signRSA256)
                .setSignAlgorithm("RS256")
                .setCapsuleManagerEndpoint(getRealTeeCapsuleMana(teeNodeId))
                .build();
        TaskConfig.TaskInputConfig taskInputConfig = TaskConfig.TaskInputConfig.newBuilder()
                .putAllSfDatasourceConfig(defaultDatasourceConfig(parties))
                .setTeeTaskConfig(sfTeeConfig)
                .build();
        String req = ProtoUtils.toJsonString(taskInputConfig, typeRegistry);
        req = decode(req);
        log.info("tee dag req : {}", req);
        return req;
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
        return teeCapsuleMana.replace("#", teeDomainId) + ":80";
    }

    public String signRSA256(String input) throws Exception {
        String strPk = FileUtils.readFile2String("./config/certs/client.pem");
        return EncryptUtils.signSHA256RSA(input, strPk);
    }

    public String getClientCert() throws IOException {
        String clientCertPath = "./config/certs/client.crt";
        return FileUtils.readFile2String(clientCertPath);
    }

    public String getCaCert() throws IOException {
        String caCertPath = "./config/certs/ca.crt";
        return FileUtils.readFile2String(caCertPath);
    }

    public String getTaskInitiatorId() throws CertificateException, IOException {
        //BASE32(SHA256(DER(X.509 public key)))
        X509Certificate x509Certificate = CertUtils.loadX509Cert("./config/certs/ca.crt");
        PublicKey publicKey = x509Certificate.getPublicKey();
        byte[] encoded = publicKey.getEncoded();
        byte[] hash = Sha256Utils.hash(encoded);
        return new Base32().encodeToString(hash).replaceAll("=", "");
    }

    public List<String> getCerts() throws IOException {
        return List.of(getClientCert(), getCaCert());
    }
}