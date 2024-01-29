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

import org.secretflow.secretpad.common.util.UUIDUtils;
import org.secretflow.secretpad.persistence.entity.ProjectGraphDO;
import org.secretflow.secretpad.persistence.entity.ProjectGraphNodeDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.service.util.JobUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Project job
 *
 * @author yansi
 * @date 2023/5/31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectJob implements Serializable {
    @Serial
    private static final long serialVersionUID = 5005877919773504643L;
    /**
     * Project id
     */
    private String projectId;
    /**
     * Graph id
     */
    private String graphId;
    /**
     * Project job name
     */
    private String name;
    /**
     * Project job id
     */
    private String jobId;
    /**
     * All graph node information list
     */
    private List<GraphNodeInfo> fullNodes;
    /**
     * Graph edge list
     */
    private List<GraphEdge> edges;
    /**
     * Job task list
     */
    private List<JobTask> tasks;

    /**
     * Build a new project job via project graph data object, project graph node data object list and parties
     *
     * @param graphDO       project graph data object
     * @param selectedNodes project graph node data object list
     * @param parties       parties
     * @return a new project job
     */
    public static ProjectJob genProjectJob(ProjectGraphDO graphDO, List<ProjectGraphNodeDO> selectedNodes, List<String> parties) {
        String jobId = UUIDUtils.random(4);
        ProjectJobBuilder jobBuilder = ProjectJob.builder()
                .projectId(graphDO.getUpk().getProjectId())
                .graphId(graphDO.getUpk().getGraphId())
                .name(graphDO.getName())
                .jobId(jobId)
                .fullNodes(GraphNodeInfo.fromDOList(graphDO.getNodes()))
                .edges(GraphEdge.fromDOList(graphDO.getEdges()));

        if (!CollectionUtils.isEmpty(selectedNodes)) {
            List<JobTask> tasks = new ArrayList<>();
            for (ProjectGraphNodeDO graphNodeDO : selectedNodes) {
                String graphNodeId = graphNodeDO.getUpk().getGraphNodeId();
                JobTask task = JobTask.builder()
                        .taskId(JobUtils.genTaskId(jobId, graphNodeId))
                        .parties(parties)
                        .node(GraphNodeInfo.fromDO(graphNodeDO))
                        .build();
                tasks.add(task);
            }
            jobBuilder.tasks(tasks);
        }
        return jobBuilder.build();
    }

    /**
     * Build a new project job data object via project job
     *
     * @param job project job
     * @return a new project job data object
     */
    public static ProjectJobDO toDO(ProjectJob job) {
        return ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK(job.getProjectId(), job.getJobId()))
                .name(job.getName())
                .tasks(job.getTasks().stream().map(t -> ProjectTaskDO.builder()
                        .upk(new ProjectTaskDO.UPK(job.getProjectId(), job.getJobId(), t.getTaskId()))
                        .parties(t.getParties())
                        .status(t.getStatus())
                        .graphNodeId(t.getNode().getGraphNodeId())
                        .graphNode(GraphNodeDetail.toDO(job.getProjectId(), job.getGraphId(), t.getNode()))
                        .build()
                ).collect(Collectors.toMap(it -> it.getUpk().getTaskId(), Function.identity())))
                .graphId(job.getGraphId())
                .edges(GraphEdge.toDOList(job.getEdges()))
                .build();
    }

    /**
     * Job task
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobTask implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;
        /**
         * Task id
         */
        private String taskId;
        /**
         * parties
         */
        private List<String> parties;
        /**
         * Graph node task status
         */
        private GraphNodeTaskStatus status;
        /**
         * The dependencies of the task
         */
        private List<String> dependencies;
        /**
         * Graph node information
         */
        private GraphNodeInfo node;

        public static ProjectTaskDO toDO(ProjectJob job, JobTask task) {
            return ProjectTaskDO.builder()
                    .upk(new ProjectTaskDO.UPK(job.getProjectId(), job.getJobId(), task.getTaskId()))
                    .graphNodeId(job.getGraphId())
                    .parties(task.getParties())
                    .build();
        }
    }
}
