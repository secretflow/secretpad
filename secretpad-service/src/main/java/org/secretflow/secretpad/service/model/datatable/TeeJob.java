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

package org.secretflow.secretpad.service.model.datatable;

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.TeeNodeDatatableManagementDO;
import org.secretflow.secretpad.persistence.model.TeeJobKind;
import org.secretflow.secretpad.service.model.graph.ProjectJob;
import org.secretflow.secretpad.service.util.JobUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tee job
 *
 * @author xujiening
 * @date 2023/9/18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeeJob implements Serializable {

    public static final String PROJECT_ID = "project_id";
    public static final String RELATIVE_URI = "relativeUri";
    public static final String VOTE_RESULT = "vote_result";
    public static final String PROJECT_JOB_ID = "projectJobId";
    public static final String PROJECT_JOB_TASK_ID = "projectJobTaskId";
    public static final String RESULT_TYPE = "resultType";

    /**
     * Node id
     */
    private String nodeId;
    /**
     * Node cert
     */
    private String nodeCert;
    /**
     * Authorization node id list
     */
    private List<String> authNodeIds;
    /**
     * Authorization node cert list
     */
    private List<String> authNodeCerts;
    /**
     * Tee node id
     */
    private String teeNodeId;
    /**
     * Datatable id
     */
    private String datatableId;
    /**
     * Tee node datatable management job id
     */
    private String jobId;
    /**
     * Tee job kind
     */
    private TeeJobKind kind;
    /**
     * Project id, authorize operation will be used
     */
    private String projectId;
    /**
     * Datasource id
     */
    private String datasourceId;
    /**
     * Relative uri
     */
    private String relativeUri;
    /**
     * Vote result
     */
    private String voteResult;
    /**
     * Job task list
     */
    private List<ProjectJob.JobTask> tasks;

    /**
     * Build tee job class
     *
     * @param managementDO  management data object
     * @param parties       parties
     * @param cert          owner cert
     * @param authNodeIds   target auth nodeId list
     * @param authNodeCerts target auth cert list
     * @return tee job class
     */
    public static TeeJob genTeeJob(TeeNodeDatatableManagementDO managementDO, List<String> parties, String cert, List<String> authNodeIds,
                                   List<String> authNodeCerts) {
        String jobId = managementDO.getUpk().getJobId();
        TeeJob.TeeJobBuilder jobBuilder = TeeJob.builder()
                .projectId(StringUtils.isBlank(managementDO.getOperateInfo()) || !getOperateInfoMap(managementDO.getOperateInfo()).containsKey(PROJECT_ID) ? "" :
                        getOperateInfoMap(managementDO.getOperateInfo()).get(PROJECT_ID).toString())
                .nodeId(managementDO.getUpk().getNodeId())
                .nodeCert(cert)
                .authNodeIds(authNodeIds)
                .authNodeCerts(authNodeCerts)
                .datatableId(managementDO.getUpk().getDatatableId())
                .teeNodeId(managementDO.getUpk().getTeeNodeId())
                .jobId(jobId)
                .kind(managementDO.getKind())
                .datasourceId(managementDO.getDatasourceId())
                .relativeUri(StringUtils.isBlank(managementDO.getOperateInfo()) || !getOperateInfoMap(managementDO.getOperateInfo()).containsKey(RELATIVE_URI) ? "" :
                        getOperateInfoMap(managementDO.getOperateInfo()).get(RELATIVE_URI).toString())
                .voteResult(StringUtils.isBlank(managementDO.getOperateInfo()) || !getOperateInfoMap(managementDO.getOperateInfo()).containsKey(VOTE_RESULT) ? "" :
                        getOperateInfoMap(managementDO.getOperateInfo()).get(VOTE_RESULT).toString());

        List<ProjectJob.JobTask> tasks = new ArrayList<>();
        ProjectJob.JobTask task = ProjectJob.JobTask.builder()
                .taskId(JobUtils.genTaskId(jobId, managementDO.getUpk().getNodeId()))
                .parties(parties)
                .build();
        tasks.add(task);
        jobBuilder.tasks(tasks);
        return jobBuilder.build();
    }

    /**
     * Get operateInfo map via operateInfo
     *
     * @param operateInfo operate information string
     * @return
     */
    public static Map<String, Object> getOperateInfoMap(String operateInfo) {
        if (StringUtils.isBlank(operateInfo)) {
            return Collections.emptyMap();
        }
        return JsonUtils.toJavaMap(operateInfo, Object.class);
    }
}
