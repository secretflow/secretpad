/*
 * Copyright 2024 Ant Group Co., Ltd.
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

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.errorcode.JobErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectJobTaskRepository;
import org.secretflow.secretpad.service.ICloudLogService;
import org.secretflow.secretpad.service.model.CloudGraphNodeTaskLogsVO;
import org.secretflow.secretpad.service.model.graph.GraphNodeCloudLogsRequest;
import org.secretflow.secretpad.service.model.node.NodeSimpleInfo;
import org.secretflow.secretpad.service.properties.LogConfigProperties;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogStore;
import com.aliyun.openservices.log.exception.LogException;
import com.aliyun.openservices.log.request.CreateLogStoreRequest;
import com.aliyun.openservices.log.request.CreateProjectRequest;
import com.aliyun.openservices.log.request.GetLogStoreRequest;
import com.aliyun.openservices.log.response.GetLogsResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author chenmingliang
 * @date 2024/04/18
 */
@Slf4j
public class SLSCloudLogServiceImpl implements ICloudLogService {

    private final String projectName;

    private final Client client;

    private static final String searchIndex = "\"__tag__:__path__\"";

    private final ProjectJobTaskRepository taskRepository;

    private final NodeRepository nodeRepository;

    private static final String LOG_STORE_PREFIX = "secretpad-engin-log-";

    private static final String EMBED_ALICE = "alice";
    private static final String EMBED_BOB = "bob";

    private String localNodeId;

    private String platformType;

    @Override
    public CloudGraphNodeTaskLogsVO fetchLog(GraphNodeCloudLogsRequest request) {
        Optional<ProjectTaskDO> taskDOOptional;
        //query appointed job history log
        if (StringUtils.isNotBlank(request.getJobId()) && StringUtils.isNotBlank(request.getTaskId())) {
            taskDOOptional = taskRepository.findById(new ProjectTaskDO.UPK(request.getProjectId(), request.getJobId(), request.getTaskId()));
        } else {
            //query latest job parties to fronted
            if (Objects.nonNull(request.getQueryParties()) && request.getQueryParties() && StringUtils.isNotBlank(request.getGraphNodeId())) {
                taskDOOptional = taskRepository.findLatestTasks(request.getProjectId(), request.getGraphNodeId());
                List<String> parties = taskDOOptional.get().getParties();
                List<NodeSimpleInfo> simpleInfos = nodeRepository.findByNodeIdIn(parties).stream().map(e -> {
                    NodeSimpleInfo build = NodeSimpleInfo.builder().nodeName(e.getName()).nodeId(e.getNodeId()).build();
                    return build;
                }).collect(Collectors.toList());
                return CloudGraphNodeTaskLogsVO.buildQueryNodePartiesResult(simpleInfos);
            } else if (StringUtils.isBlank(request.getGraphNodeId()) && StringUtils.isBlank(request.getJobId()) && StringUtils.isBlank(request.getTaskId())) {
                //query SLS service ready status
                return CloudGraphNodeTaskLogsVO.buildReadyResult();
            } else {
                //query latest job log
                taskDOOptional = taskRepository.findLatestTasks(request.getProjectId(), request.getGraphNodeId());
            }
        }
        if (taskDOOptional.isEmpty()) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_TASK_NOT_EXISTS);
        }
        platformPermission(platformType, localNodeId, request.getNodeId());
        ProjectTaskDO task = taskDOOptional.get();
        String taskId = task.getUpk().getTaskId();
        StringBuilder query = new StringBuilder("* |select content where ");
        String querySql = query.append(searchIndex).append(" like ").append("'%").append(taskId).append("%'").append(" limit ").append(Integer.MAX_VALUE).toString();
        log.info("query sls log,sql = {},logStore is {}", querySql, LOG_STORE_PREFIX + request.getNodeId());
        GetLogsResponse getLogsResponse;
        try {
            getLogsResponse = client.GetLogs(projectName, LOG_STORE_PREFIX + request.getNodeId(), 0, (int) (System.currentTimeMillis() / 1000), "", querySql);
        } catch (LogException e) {
            throw SecretpadException.of(JobErrorCode.PROJECT_JOB_CLOUD_LOG_ERROR, e, e.GetErrorMessage());
        }
        List<String> logs = getLogsResponse.getLogs().stream().flatMap(e -> e.GetLogItem().GetLogContents().stream()).map(e -> e.GetValue()).collect(Collectors.toList());
        return new CloudGraphNodeTaskLogsVO(task.getStatus(), logs, true);
    }

    private void platformPermission(String platform, String nodeId, String requesterNodeId) {
        if (PlatformTypeEnum.valueOf(platform) == PlatformTypeEnum.AUTONOMY) {
            if (!nodeId.equalsIgnoreCase(requesterNodeId)) {
                throw SecretpadException.of(JobErrorCode.PROJECT_JOB_NODE_PERMISSION_ERROR);
            }
        }
    }

    public SLSCloudLogServiceImpl(LogConfigProperties.SLSConfig slsConfig, String platformType, String nodeId, ProjectJobTaskRepository taskRepository, NodeRepository nodeRepository) throws LogException {
        this.projectName = slsConfig.getProject();
        this.client = new Client(slsConfig.getHost(), slsConfig.getAk(), slsConfig.getSk());
        this.taskRepository = taskRepository;
        this.nodeRepository = nodeRepository;
        this.localNodeId = nodeId;
        this.platformType = platformType;
        queryOrCreateProject(projectName);
        queryOrCreateLogStore(projectName, platformType, nodeId);
    }

    public SLSCloudLogServiceImpl(ProjectJobTaskRepository taskRepository, NodeRepository nodeRepository, Client client, LogConfigProperties.SLSConfig slsConfig, String platformType) {
        this.taskRepository = taskRepository;
        this.nodeRepository = nodeRepository;
        this.client = client;
        this.projectName = slsConfig.getProject();
        this.platformType = platformType;
    }


    private void createProject(String projectName) throws LogException {
        CreateProjectRequest createProjectRequest = new CreateProjectRequest(projectName, "", "");
        client.createProject(createProjectRequest);
        log.info("Project --> {} NotExist,create SLS project successfully", projectName);
    }

    private void createLogstore(String logStoreName, String projectName) throws LogException {
        LogStore logStore = new LogStore(logStoreName, 60, 2, true);
        logStore.setmAutoSplit(true);
        logStore.setmMaxSplitShard(64);
        logStore.setAppendMeta(true);
        logStore.setHotTTL(30);
        logStore.setMode("standard");

        CreateLogStoreRequest request = new CreateLogStoreRequest(projectName, logStore);
        client.CreateLogStore(request);
        log.info("create logstore {} success", logStoreName);
    }

    private void queryOrCreateProject(String projectName) throws LogException {
        try {
            client.GetProject(projectName);
        } catch (LogException e) {
            if ("ProjectNotExist".equals(e.getErrorCode())) {
                log.info("Project --> {} NotExist,start create SLS project", projectName);
                createProject(projectName);
            } else {
                throw e;
            }
        }
    }

    private void queryOrCreateLogStore(String projectName, String platformType, String nodeId) throws LogException {
        switch (PlatformTypeEnum.valueOf(platformType)) {
            case AUTONOMY, EDGE -> {
                doQueryOrCreate(projectName, LOG_STORE_PREFIX + nodeId);
            }
            case CENTER -> {
                String aliceLogStore = LOG_STORE_PREFIX + EMBED_ALICE;
                doQueryOrCreate(projectName, aliceLogStore);
                String bobLogStore = LOG_STORE_PREFIX + EMBED_BOB;
                doQueryOrCreate(projectName, bobLogStore);
            }
        }

    }

    private void doQueryOrCreate(String projectName, String logStoreName) throws LogException {
        try {
            queryLogstore(logStoreName, projectName);
        } catch (LogException e) {
            if ("LogStoreNotExist".equals(e.getErrorCode())) {
                createLogstore(logStoreName, projectName);
            } else {
                throw e;
            }
        }
    }

    private void queryLogstore(String logStoreName, String projectName) throws LogException {
        log.info("start to query logstore");
        GetLogStoreRequest request = new GetLogStoreRequest(projectName, logStoreName);
        client.GetLogStore(request);
        log.info("end to query logstore");
    }
}
