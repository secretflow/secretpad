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

package org.secretflow.secretpad.service.graph.adapter.impl;

import org.secretflow.secretpad.common.constant.ComponentConstants;
import org.secretflow.secretpad.persistence.entity.ProjectReadDataDO;
import org.secretflow.secretpad.persistence.repository.ProjectReadDataRepository;
import org.secretflow.secretpad.service.graph.adapter.NodeDefAdapter;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;
import org.secretflow.secretpad.service.model.graph.ProjectJob;
import org.secretflow.secretpad.service.util.JobUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.secretflow.spec.v1.ComponentDef;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yutu
 * @date 2023/11/30
 */
@Slf4j
@Service(ComponentConstants.BINNING_MODIFICATIONS)
public class BinningModificationsAdapter implements NodeDefAdapter {
    @Resource
    private ProjectReadDataRepository readDataRepository;

    @Override
    public ProjectJob.JobTask adapter(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, ProjectJob.JobTask task) {
        return adapter1(nodeDef, graphNodeInfo, task);
    }

    private ProjectJob.JobTask adapter1(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, ProjectJob.JobTask task) {
        List<Struct> attrsList = nodeDef.getAttrsList();
        List<String> outputs = new ArrayList<>(graphNodeInfo.getOutputs());
        ProjectJob.JobTask jobTask;
        convertToRead(nodeDef, graphNodeInfo, outputs, task);
        if (attrsList.isEmpty()) {
            jobTask = convertToIdentity(graphNodeInfo, task, outputs);
        } else {
            jobTask = convertToWrite(nodeDef, graphNodeInfo, task, outputs);
        }
        return jobTask;
    }

    private Pipeline.NodeDef convertToRead(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, List<String> outputs, ProjectJob.JobTask task) {
        ComponentDef componentDef = buildIoReadData();
        Pipeline.NodeDef build = Pipeline.NodeDef.newBuilder()
                .setDomain(componentDef.getDomain())
                .setName(componentDef.getName())
                .setVersion(componentDef.getVersion())
                .build();
        GraphNodeInfo readGraphNodeInfo = graphNodeInfo.toGraphNodeInfo();
        readGraphNodeInfo.setNodeDef(build);
        readGraphNodeInfo.setInputs(List.of(outputs.get(0)));
        readGraphNodeInfo.setOutputs(List.of(outputs.get(1)));
        task.setNode(readGraphNodeInfo);
        return nodeDef;
    }

    private ProjectJob.JobTask convertToWrite(
            Pipeline.NodeDef nodeDef,
            GraphNodeInfo graphNodeInfo,
            ProjectJob.JobTask task,
            List<String> outputs
    ) {
        ComponentDef componentDef = buildIoWriteData();
        Pipeline.NodeDef build = Pipeline.NodeDef.newBuilder()
                .mergeFrom(nodeDef)
                .setDomain(componentDef.getDomain())
                .setName(componentDef.getName())
                .setVersion(componentDef.getVersion())
                .clearAttrPaths()
                .addAllAttrPaths(List.of("write_data", "write_data_type"))
                .addAttrs(Struct.newBuilder().putFields(
                        ComponentConstants.ATTRIBUTE_S,
                        Value.newBuilder().setStringValue("sf.rule.binning").build()
                ).build())
                .build();
        GraphNodeInfo writeGraphNodeInfo = graphNodeInfo.toGraphNodeInfo();
        Struct struct = build.getAttrsList().get(0);
        String stringValue = struct.getFieldsOrDefault(ComponentConstants.ATTRIBUTE_S, Value.newBuilder().setStringValue("").build()).getStringValue();
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(stringValue, JsonElement.class);
        String modelHash = jsonElement.getAsJsonObject().get("modelHash").getAsString();
        String graphNodeId = graphNodeInfo.getGraphNodeId();
        // fix upload rule json
        if (modelHash.contains("_")) {
            graphNodeId = modelHash.substring(modelHash.indexOf('_') + 1);
            modelHash = modelHash.substring(0, modelHash.indexOf('_'));
            jsonElement.getAsJsonObject().addProperty("modelHash", modelHash);
            String asString = jsonElement.toString();
            struct = struct.toBuilder().putFields(ComponentConstants.ATTRIBUTE_S, Value.newBuilder().setStringValue(asString).build()).build();
            build = build.toBuilder().setAttrs(0, struct).build();
            log.debug("convertToWrite modelHash build {} ", build);
        }
        String new_outPut_id = outputs.get(0);
        List<ProjectReadDataDO> byHash = readDataRepository.findByHashAndGrapNodeId(modelHash, graphNodeId);
        if (!CollectionUtils.isEmpty(byHash) && !byHash.isEmpty()) {
            new_outPut_id = byHash.get(0).getUpk().getReportId();
            new_outPut_id = new_outPut_id.substring(0, new_outPut_id.lastIndexOf('-'));
            new_outPut_id = new_outPut_id + "-0";
        }
        writeGraphNodeInfo.setInputs(List.of(new_outPut_id));
        return getJobTask(build, writeGraphNodeInfo, task, outputs);
    }

    private ProjectJob.JobTask getJobTask(Pipeline.NodeDef nodeDef, GraphNodeInfo graphNodeInfo, ProjectJob.JobTask task, List<String> outputs) {
        GraphNodeInfo cpGraphNodeInfo = graphNodeInfo.toGraphNodeInfo();
        cpGraphNodeInfo.setNodeDef(nodeDef);
        // outputs must contain one element, and it's the same as input
        cpGraphNodeInfo.setOutputs(List.of(outputs.get(0)));
        String newTaskId = JobUtils.genExtendTaskId(task.getTaskId(), "1");
        List<String> dependencies = task.getDependencies();
        task.setDependencies(List.of(newTaskId));
        return ProjectJob.JobTask.builder()
                .taskId(newTaskId)
                .parties(task.getParties())
                .node(cpGraphNodeInfo)
                .dependencies(dependencies)
                .build();
    }

    private ProjectJob.JobTask convertToIdentity(
            GraphNodeInfo graphNodeInfo,
            ProjectJob.JobTask task,
            List<String> outputs
    ) {
        ComponentDef componentDef = buildIoIdentity();
        Pipeline.NodeDef build = Pipeline.NodeDef.newBuilder()
                .setDomain(componentDef.getDomain())
                .setName(componentDef.getName())
                .setVersion(componentDef.getVersion())
                .build();
        return getJobTask(build, graphNodeInfo, task, outputs);
    }
}