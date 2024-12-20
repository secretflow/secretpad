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

package org.secretflow.secretpad.service.model.graph;

import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.service.model.node.NodeSimpleInfo;
import org.secretflow.secretpad.service.model.project.MergedProjectResult;
import org.secretflow.secretpad.service.model.project.ProjectResultBaseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Graph node detail
 *
 * @author yansi
 * @date 2023/5/26
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GraphNodeDetail extends GraphNodeInfo {
    /**
     * Graph node task status
     */
    private GraphNodeTaskStatus status;
    /**
     * JobId，the Job associated with this GraphNode is empty if it is not associated
     */
    @Schema(description = "jobId，the Job associated with this GraphNode is empty if it is not associated")
    private String jobId;
    /**
     * TaskId，the Task associated with this GraphNode is empty if it is not associated
     */
    @Schema(description = "taskId，the Task associated with this GraphNode is empty if it is not associated")
    private String taskId;
    /**
     * Project result base view object list
     */
    private List<ProjectResultBaseVO> results;

    /**
     * the graph node running parties
     */
    private List<NodeSimpleInfo> parties;

    /**
     * the graph node running progress
     */
    private Float progress;

    public GraphNodeDetail(ProjectGraphNodeDO graphNodeDO, GraphNodeTaskStatus status, List<MergedProjectResult> results) {
        this.codeName = graphNodeDO.getCodeName();
        this.graphNodeId = graphNodeDO.getUpk().getGraphNodeId();
        this.label = graphNodeDO.getLabel();
        this.x = graphNodeDO.getX();
        this.y = graphNodeDO.getY();
        this.inputs = graphNodeDO.getInputs();
        this.outputs = graphNodeDO.getOutputs();
        this.nodeDef = graphNodeDO.getNodeDef();
        this.status = status;
        if (!CollectionUtils.isEmpty(results)) {
            this.results = results.stream().map(ProjectResultBaseVO::of).collect(Collectors.toList());
        }
    }

    public static GraphNodeDetail fromDO(ProjectGraphNodeDO graphNodeDO, GraphNodeTaskStatus status) {
        return GraphNodeDetail.builder()
                .codeName(graphNodeDO.getCodeName())
                .graphNodeId(graphNodeDO.getUpk().getGraphNodeId())
                .label(graphNodeDO.getLabel())
                .x(graphNodeDO.getX())
                .y(graphNodeDO.getY())
                .inputs(graphNodeDO.getInputs())
                .outputs(graphNodeDO.getOutputs())
                .nodeDef(graphNodeDO.getNodeDef())
                .status(status)
                .build();
    }

    /**
     * Build a new graph node detail from project graph node data object, graph node task status and merged project result list
     *
     * @param graphNodeDO project graph node data object
     * @param status      graph node task status
     * @param results     merged project result list
     * @return a new graph node detail
     */
    public static GraphNodeDetail fromDO(ProjectGraphNodeDO graphNodeDO, GraphNodeTaskStatus status, List<MergedProjectResult> results) {
        return new GraphNodeDetail(graphNodeDO, status, results);
    }

    /**
     * Batch build graph node detail list from project graph node data object list and graph node status view object list
     *
     * @param graphNodeDOList project graph node data object list
     * @param nodeStatus      graph node status view object list
     * @return graph node detail list
     */
    public static List<GraphNodeDetail> fromDOList(List<ProjectGraphNodeDO> graphNodeDOList, List<GraphNodeStatusVO> nodeStatus) {
        final Map<String, GraphNodeTaskStatus> statusMap = new HashMap<>();
        final Map<String, GraphNodeStatusVO> jobTaskMap = new HashMap<>();
        final Map<String, List<NodeSimpleInfo>> partyMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(nodeStatus)) {
            statusMap.putAll(nodeStatus.stream().collect(Collectors.toMap(GraphNodeStatusVO::getGraphNodeId, GraphNodeStatusVO::getStatus)));
            jobTaskMap.putAll(nodeStatus.stream().collect(Collectors.toMap(GraphNodeStatusVO::getGraphNodeId, Function.identity())));
            Map<String, List<NodeSimpleInfo>> infosMap = nodeStatus.stream()
                    .collect(Collectors.toMap(
                            GraphNodeStatusVO::getGraphNodeId,
                            nodeStatusVO -> (nodeStatusVO.getParties() != null ? nodeStatusVO.getParties() : new ArrayList<>()),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new));
            partyMap.putAll(infosMap);
        }
        if (!CollectionUtils.isEmpty(graphNodeDOList)) {
            return graphNodeDOList.stream().map(graphNodeDO -> fromDO(graphNodeDO,
                                    statusMap.getOrDefault(graphNodeDO.getUpk().getGraphNodeId(), GraphNodeTaskStatus.STAGING), null)
                                    .withJobTask(jobTaskMap.get(graphNodeDO.getUpk().getGraphNodeId()).getJobId(), jobTaskMap.get(graphNodeDO.getUpk().getGraphNodeId()).getTaskId())
                                    .withJobParties(partyMap.get(graphNodeDO.getUpk().getGraphNodeId()))
                                    .withTaskProgress(jobTaskMap.get(graphNodeDO.getUpk().getGraphNodeId()).getProgress()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Build graph node detail via filling jobId and taskId
     *
     * @param jobId  target jobId
     * @param taskId target taskId
     * @return graph node detail
     */
    public GraphNodeDetail withJobTask(String jobId, String taskId) {
        this.jobId = jobId;
        this.taskId = taskId;
        return this;
    }

    public GraphNodeDetail withJobParties(List<NodeSimpleInfo> parties) {
        this.parties = parties;
        return this;
    }

    public GraphNodeDetail withTaskProgress(Float progress) {
        this.progress = progress;
        return this;
    }

}
