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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.DomainConstants;
import org.secretflow.secretpad.common.constant.DomainRouterConstants;
import org.secretflow.secretpad.common.constant.ScheduledConstants;
import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.errorcode.ProjectErrorCode;
import org.secretflow.secretpad.common.errorcode.ScheduledErrorCode;
import org.secretflow.secretpad.common.util.DateTimes;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.persistence.model.ScheduledGraphCreateRequest;
import org.secretflow.secretpad.persistence.model.ScheduledGraphOnceSuccessRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledDelRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledIdRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledInfoRequest;
import org.secretflow.secretpad.scheduled.model.ScheduledOfflineRequest;
import org.secretflow.secretpad.service.model.node.*;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.DomainOuterClass;
import org.secretflow.v1alpha1.kusciaapi.DomainRoute;
import org.secretflow.v1alpha1.kusciaapi.Domaindata;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.secretflow.secretpad.common.constant.Constants.SUCCESS_STATUS_MESSAGE;

/**
 * ScheduledControllerTest
 *
 * @author yutu
 * @date 2024/08/26
 */
class ScheduledControllerTest extends ControllerTest {

    @MockBean
    private KusciaGrpcClientAdapter kusciaGrpcClientAdapter;


    static String projectId = "ixpbkyyf";
    static String graphId = "yhvcpsdf";
    static String graphId1 = "yhvcpsdf1";

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void id() throws Exception {
        assertResponse(() -> {
            ScheduledIdRequest scheduledIdRequest = FakerUtils.fake(ScheduledIdRequest.class);
            scheduledIdRequest.setProjectId(projectId);
            scheduledIdRequest.setGraphId(graphId);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "id", ScheduledIdRequest.class))
                    .content(JsonUtils.toJSONString(scheduledIdRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void idByProjectNotReadyExistsException() throws Exception {
        assertErrorCode(() -> {
            ScheduledIdRequest scheduledIdRequest = FakerUtils.fake(ScheduledIdRequest.class);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "id", ScheduledIdRequest.class))
                    .content(JsonUtils.toJSONString(scheduledIdRequest));
        }, ProjectErrorCode.PROJECT_NOT_EXISTS);
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void idByGraphNotReadyExistsException() throws Exception {
        assertErrorCode(() -> {
            ScheduledIdRequest scheduledIdRequest = FakerUtils.fake(ScheduledIdRequest.class);
            scheduledIdRequest.setProjectId(projectId);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "id", ScheduledIdRequest.class))
                    .content(JsonUtils.toJSONString(scheduledIdRequest));
        }, GraphErrorCode.GRAPH_NODE_NOT_EXISTS);
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void onceSuccess() throws Exception {
        assertResponse(() -> {
            ScheduledGraphOnceSuccessRequest scheduledGraphOnceSuccessRequest = FakerUtils.fake(ScheduledGraphOnceSuccessRequest.class);
            scheduledGraphOnceSuccessRequest.setProjectId(projectId);
            scheduledGraphOnceSuccessRequest.setGraphId(graphId);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "onceSuccess", ScheduledGraphOnceSuccessRequest.class))
                    .content(JsonUtils.toJSONString(scheduledGraphOnceSuccessRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void onceSuccessProjectJobNotReadyExistsException() throws Exception {
        assertResponse(() -> {
            ScheduledGraphOnceSuccessRequest scheduledGraphOnceSuccessRequest = FakerUtils.fake(ScheduledGraphOnceSuccessRequest.class);
            scheduledGraphOnceSuccessRequest.setProjectId(projectId);
            scheduledGraphOnceSuccessRequest.setGraphId(graphId1);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "onceSuccess", ScheduledGraphOnceSuccessRequest.class))
                    .content(JsonUtils.toJSONString(scheduledGraphOnceSuccessRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void onceSuccessProjectJobSuccessNotReadyExistsException() throws Exception {
        assertResponse(() -> {
            ScheduledGraphOnceSuccessRequest scheduledGraphOnceSuccessRequest = FakerUtils.fake(ScheduledGraphOnceSuccessRequest.class);
            scheduledGraphOnceSuccessRequest.setProjectId(projectId);
            scheduledGraphOnceSuccessRequest.setGraphId(graphId1);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "onceSuccess", ScheduledGraphOnceSuccessRequest.class))
                    .content(JsonUtils.toJSONString(scheduledGraphOnceSuccessRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void create() throws Exception {
        assertResponseWithEmptyData(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            ScheduledGraphCreateRequest scheduledGraphCreateRequest = FakerUtils.fake(ScheduledGraphCreateRequest.class);
            ScheduledGraphCreateRequest.Cron cron = new ScheduledGraphCreateRequest.Cron();
            String start = DateTimes.localDateTimeString(LocalDateTime.now().plusMinutes(1));
            String end = DateTimes.localDateTimeString(LocalDateTime.now().plusDays(10));
            cron.setStartTime(start);
            cron.setEndTime(end);
            cron.setScheduleCycle(ScheduledConstants.SCHEDULED_CYCLE_DAY);
            cron.setScheduleTime("12:00");
            cron.setScheduleDate("");
            scheduledGraphCreateRequest.setCron(cron);
            scheduledGraphCreateRequest.setProjectId(projectId);
            scheduledGraphCreateRequest.setGraphId(graphId);
            scheduledGraphCreateRequest.setNodes(List.of("yhvcpsdf-node-1", "yhvcpsdf-node-2", "yhvcpsdf-node-3"));
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(DomainOuterClass.QueryDomainResponseData.newBuilder()
                            .addNodeStatuses(DomainOuterClass.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()))
                            .build())
                    .build());
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any())).thenReturn(
                    DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build()
            );
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any(), Mockito.any())).thenReturn(
                    DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build())
                            .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder().
                                    setStatus(DomainRoute.RouteStatus.newBuilder().setStatus(DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name()).build())
                                    .build())
                            .build()
            );
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Mockito.any())).thenReturn(Domaindata.QueryDomainDataResponse.newBuilder().getDefaultInstanceForType());
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "create", ScheduledGraphCreateRequest.class))
                    .content(JsonUtils.toJSONString(scheduledGraphCreateRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void createError() throws Exception {
        assertErrorCode(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            ScheduledGraphCreateRequest scheduledGraphCreateRequest = FakerUtils.fake(ScheduledGraphCreateRequest.class);
            ScheduledGraphCreateRequest.Cron cron = new ScheduledGraphCreateRequest.Cron();
            String start = DateTimes.localDateTimeString(LocalDateTime.now().plusMinutes(1));
            cron.setStartTime(start);
            cron.setEndTime(start);
            cron.setScheduleCycle(ScheduledConstants.SCHEDULED_CYCLE_DAY);
            cron.setScheduleTime("12:00");
            cron.setScheduleDate("");
            scheduledGraphCreateRequest.setCron(cron);
            scheduledGraphCreateRequest.setProjectId(projectId);
            scheduledGraphCreateRequest.setGraphId(graphId);
            scheduledGraphCreateRequest.setNodes(List.of("yhvcpsdf-node-1", "yhvcpsdf-node-2", "yhvcpsdf-node-3"));
            Mockito.when(kusciaGrpcClientAdapter.queryDomain(Mockito.any())).thenReturn(DomainOuterClass.QueryDomainResponse.newBuilder()
                    .setStatus(Common.Status.newBuilder().setCode(0).build())
                    .setData(DomainOuterClass.QueryDomainResponseData.newBuilder()
                            .addNodeStatuses(DomainOuterClass.NodeStatus.newBuilder().setStatus(DomainConstants.DomainStatusEnum.Ready.name()))
                            .build())
                    .build());
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any())).thenReturn(
                    DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build()
            );
            Mockito.when(kusciaGrpcClientAdapter.queryDomainRoute(Mockito.any(), Mockito.any())).thenReturn(
                    DomainRoute.QueryDomainRouteResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build())
                            .setData(DomainRoute.QueryDomainRouteResponseData.newBuilder().
                                    setStatus(DomainRoute.RouteStatus.newBuilder().setStatus(DomainRouterConstants.DomainRouterStatusEnum.Succeeded.name()).build())
                                    .build())
                            .build()
            );
            Mockito.when(kusciaGrpcClientAdapter.queryDomainData(Mockito.any())).thenReturn(Domaindata.QueryDomainDataResponse.newBuilder().getDefaultInstanceForType());
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "create", ScheduledGraphCreateRequest.class))
                    .content(JsonUtils.toJSONString(scheduledGraphCreateRequest));
        }, ScheduledErrorCode.SCHEDULE_CREATE_ERROR);
    }


    @Sql(scripts = {"/test-job.sql"})
    @Test
    void page() throws Exception {
        assertResponse(() -> {
            PageScheduledRequest pageScheduledRequest = FakerUtils.fake(PageScheduledRequest.class);
            pageScheduledRequest.setProjectId(projectId);
            pageScheduledRequest.setSearch("");
            pageScheduledRequest.setStatus("");
            pageScheduledRequest.setPage(1);
            pageScheduledRequest.setSize(10);
            pageScheduledRequest.setSort(null);
            Mockito.when(kusciaGrpcClientAdapter.createJob(Mockito.any())).thenReturn(Job.CreateJobResponse.newBuilder().
                    setStatus(Common.Status.newBuilder().setCode(0).setMessage(SUCCESS_STATUS_MESSAGE).build()).
                    getDefaultInstanceForType());
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "page", PageScheduledRequest.class))
                    .content(JsonUtils.toJSONString(pageScheduledRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void offline() throws Exception {
        assertResponseWithEmptyData(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            ScheduledOfflineRequest scheduledOfflineRequest = FakerUtils.fake(ScheduledOfflineRequest.class);
            scheduledOfflineRequest.setScheduleId("489A1F7577");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "offline", ScheduledOfflineRequest.class))
                    .content(JsonUtils.toJSONString(scheduledOfflineRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void del() throws Exception {
        assertErrorCode(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            ScheduledDelRequest scheduledDelRequest = FakerUtils.fake(ScheduledDelRequest.class);
            scheduledDelRequest.setScheduleId("489A1F7577");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "del", ScheduledDelRequest.class))
                    .content(JsonUtils.toJSONString(scheduledDelRequest));
        }, ScheduledErrorCode.SCHEDULE_UP_NOT_DEL);
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void info() throws Exception {
        assertResponse(() -> {
            ScheduledInfoRequest scheduledInfoRequest = FakerUtils.fake(ScheduledInfoRequest.class);
            scheduledInfoRequest.setScheduleId("489A1F7577");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "info", ScheduledInfoRequest.class))
                    .content(JsonUtils.toJSONString(scheduledInfoRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskPage() throws Exception {
        assertResponse(() -> {
            TaskPageScheduledRequest taskPageScheduledRequest = FakerUtils.fake(TaskPageScheduledRequest.class);
            taskPageScheduledRequest.setScheduleId("489A1F7577");
            taskPageScheduledRequest.setSearch("");
            taskPageScheduledRequest.setPage(1);
            taskPageScheduledRequest.setSize(10);
            taskPageScheduledRequest.setSort(null);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskPage", TaskPageScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskPageScheduledRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskStop() throws Exception {
        assertResponseWithEmptyData(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            TaskStopScheduledRequest taskStopScheduledRequest = FakerUtils.fake(TaskStopScheduledRequest.class);
            taskStopScheduledRequest.setScheduleId("489A1F7577");
            taskStopScheduledRequest.setScheduleTaskId("ujaj-20240904193254");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskStop", TaskStopScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskStopScheduledRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskRerunError() throws Exception {
        assertErrorCode(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            TaskReRunScheduledRequest taskReRunScheduledRequest = FakerUtils.fake(TaskReRunScheduledRequest.class);
            taskReRunScheduledRequest.setScheduleId("489A1F7577");
            taskReRunScheduledRequest.setScheduleTaskId("ujaj-20240904193254");
            taskReRunScheduledRequest.setType("0");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskRerun", TaskReRunScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskReRunScheduledRequest));
        }, ScheduledErrorCode.PROJECT_JOB_RESTART_ERROR);
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskRerunAll() throws Exception {
        assertResponseWithEmptyData(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            TaskReRunScheduledRequest taskReRunScheduledRequest = FakerUtils.fake(TaskReRunScheduledRequest.class);
            taskReRunScheduledRequest.setScheduleId("489A1F7577");
            taskReRunScheduledRequest.setScheduleTaskId("ujao-20240904193254");
            taskReRunScheduledRequest.setType("0");
            Mockito.when(kusciaGrpcClientAdapter.deleteJob(Mockito.any())).thenReturn(
                    Job.DeleteJobResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build()
            );
            Mockito.when(kusciaGrpcClientAdapter.createJob(Mockito.any())).thenReturn(
                    Job.CreateJobResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build()
            );
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskRerun", TaskReRunScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskReRunScheduledRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskRerun() throws Exception {
        assertResponseWithEmptyData(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("kuscia-system");
            TaskReRunScheduledRequest taskReRunScheduledRequest = FakerUtils.fake(TaskReRunScheduledRequest.class);
            taskReRunScheduledRequest.setScheduleId("489A1F7577");
            taskReRunScheduledRequest.setScheduleTaskId("ujao-20240904193254");
            taskReRunScheduledRequest.setType("1");
            Mockito.when(kusciaGrpcClientAdapter.restartJob(Mockito.any())).thenReturn(
                    Job.RestartJobResponse.newBuilder().setStatus(Common.Status.newBuilder().setCode(0).build()).build()
            );
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskRerun", TaskReRunScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskReRunScheduledRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskRerunNotOwner() throws Exception {
        assertErrorCode(() -> {
            UserContextDTO user = UserContext.getUser();
            user.setOwnerId("123");
            TaskReRunScheduledRequest taskReRunScheduledRequest = FakerUtils.fake(TaskReRunScheduledRequest.class);
            taskReRunScheduledRequest.setScheduleId("489A1F7577");
            taskReRunScheduledRequest.setScheduleTaskId("ujaj-20240904193254");
            taskReRunScheduledRequest.setType("0");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskRerun", TaskReRunScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskReRunScheduledRequest));
        }, ScheduledErrorCode.USER_NOT_OWNER);
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void taskInfo() throws Exception {
        assertResponse(() -> {
            TaskInfoScheduledRequest taskInfoScheduledRequest = FakerUtils.fake(TaskInfoScheduledRequest.class);
            taskInfoScheduledRequest.setScheduleId("489A1F7577");
            taskInfoScheduledRequest.setScheduleTaskId("ujaj-20240904193254");
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "taskInfo", TaskInfoScheduledRequest.class))
                    .content(JsonUtils.toJSONString(taskInfoScheduledRequest));
        });
    }

    @Sql(scripts = {"/test-job.sql"})
    @Test
    void listJob() throws Exception {
        assertResponse(() -> {
            ScheduleListProjectJobRequest scheduleListProjectJobRequest = FakerUtils.fake(ScheduleListProjectJobRequest.class);
            scheduleListProjectJobRequest.setProjectId(projectId);
            scheduleListProjectJobRequest.setGraphId(graphId);
            scheduleListProjectJobRequest.setScheduleTaskId("ujaj-20240904193254");
            scheduleListProjectJobRequest.setPageNum(1);
            scheduleListProjectJobRequest.setPageSize(10);
            return MockMvcRequestBuilders.post(getMappingUrl(ScheduledController.class, "listJob", ScheduleListProjectJobRequest.class))
                    .content(JsonUtils.toJSONString(scheduleListProjectJobRequest));
        });
    }

}