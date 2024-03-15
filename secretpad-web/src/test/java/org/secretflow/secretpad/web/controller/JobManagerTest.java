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

import org.secretflow.secretpad.manager.integration.job.JobManager;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author yutu
 * @date 2024/02/24
 */
public class JobManagerTest extends ControllerTest {

    @Resource
    private JobManager jobManager;
    @MockBean
    JobServiceGrpc.JobServiceBlockingStub jobStub;

    @Test
    void test() {
        Mockito.when(jobStub.watchJob(Mockito.any())).thenReturn(buildADDEDWatchJobEventResponse());
        Assertions.assertDoesNotThrow(() -> jobManager.startSync());

        Mockito.when(jobStub.watchJob(Mockito.any())).thenReturn(buildMODIFIEDWatchJobEventResponse());
        Assertions.assertDoesNotThrow(() -> jobManager.startSync());
    }

    Iterator<Job.WatchJobEventResponse> buildMODIFIEDWatchJobEventResponse() {
        List<Job.WatchJobEventResponse> list = new ArrayList<>();
        list.add(Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("mljy")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("mljy-wzmysluh-node-3").build())
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("mljy-wzmysluh-node-4")
                                        .setState("Pending")
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("bob")
                                                .setState("Pending")
                                                .build())
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("alice")
                                                .setState("Pending")
                                                .build())
                                        .build())
                                .build()).build())
                .build());
        return list.iterator();
    }

    Iterator<Job.WatchJobEventResponse> buildADDEDWatchJobEventResponse() {
        List<Job.WatchJobEventResponse> list = new ArrayList<>();
        list.add(Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.ADDED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("ftdp")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder()
                                        .setTaskId("cqoh-model-export")
                                        .setState("Running")
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("bob")
                                                .setState("Running")
                                                .build())
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("alice")
                                                .setState("Running")
                                                .build())
                                        .build())
                                .build()).build())
                .build());
        return list.iterator();
    }

}