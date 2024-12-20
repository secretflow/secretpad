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

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.DynamicKusciaChannelProvider;
import org.secretflow.secretpad.manager.integration.datatablegrant.DatatableGrantManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.repository.*;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yutu
 * @date 2024/02/24
 */
@TestPropertySource(properties = {
        "job.sync.enabled=false"
})
public class JobManagerTest extends ControllerTest {

    private static int index = 1;
    @MockBean
    JobServiceGrpc.JobServiceStub jobServiceAsyncStub;
    @Resource
    private JobManager jobManager;
    @Resource
    private ProjectJobRepository projectJobRepository;
    @Resource
    private ProjectGraphRepository projectGraphRepository;
    @Resource
    private ProjectRepository projectRepository;
    @Resource
    private ProjectJobTaskRepository projectJobTaskRepository;
    @Resource
    private ProjectGraphNodeRepository projectGraphNodeRepository;
    private Server mockServer;
    private ManagedChannel inProcessChannel;
    @MockBean
    private DatatableGrantManager datatableGrantManager;
    @Resource
    private ProjectScheduleJobRepository projectScheduleJobRepository;
    @MockBean
    private DynamicKusciaChannelProvider dynamicKusciaChannelProvider;

    static Job.WatchJobEventResponse buildTaskEmptyJobSuccessWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Succeeded")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .build()).build())
                .build();
    }

    static Job.WatchJobEventResponse buildTaskEmptyProjectScheduleJobSuccessWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test1")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Succeeded")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .build()).build())
                .build();
    }

    static Job.WatchJobEventResponse buildTaskRuningJobSuccessJobSuccessWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Succeeded")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1")
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
                .build();
    }

    static Job.WatchJobEventResponse buildTaskRuningProjectScheduleJobSuccessJobSuccessButDbJobIsFinishWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test2")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Succeeded")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1")
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
                .build();
    }


    static Job.WatchJobEventResponse buildTaskRuningProjectScheduleJobSuccessJobSuccessWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test1")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Succeeded")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1")
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
                .build();
    }


    static Job.WatchJobEventResponse buildTaskWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1")
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
                .build();
    }

    static Job.WatchJobEventResponse buildTaskEmptyWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .build()).build())
                .build();
    }

    static Job.WatchJobEventResponse buildTaskEmptyNotExistWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .build()).build())
                .build();
    }

    static Job.WatchJobEventResponse buildJobEndTaskRunPendingdWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Failed")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1")
                                        .setState("Pending")
                                        .build())
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-2")
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
                .build();
    }

    static Job.WatchJobEventResponse buildPendingdWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1").build())
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-2")
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
                .build();
    }

    static Job.WatchJobEventResponse buildSuccessdWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1").build())
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-2")
                                        .setState("Success")
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("bob")
                                                .setState("Success")
                                                .build())
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("alice")
                                                .setState("Success")
                                                .build())
                                        .build())
                                .build()).build())
                .build();
    }

    static Job.WatchJobEventResponse buildExitSuccessdWatchJobEventResponse() {
        return Job.WatchJobEventResponse.newBuilder().setType(Job.EventType.MODIFIED)
                .setObject(Job.JobStatus.newBuilder()
                        .setJobId("test")
                        .setStatus(Job.JobStatusDetail.newBuilder()
                                .setState("Running")
                                .setCreateTime("2024-02-23T15:35:00Z")
                                .setStartTime("2024-02-23T15:35:00Z")
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-1").build())
                                .addTasks(Job.TaskStatus.newBuilder().setTaskId("test-atxtxxwc-node-2")
                                        .setState("Success")
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("bob")
                                                .setState("Success")
                                                .build())
                                        .addParties(Job.PartyStatus.newBuilder()
                                                .setDomainId("alice")
                                                .setState("Success")
                                                .build())
                                        .build())
                                .build()).build())
                .build();
    }

    @BeforeEach
    public void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        mockServer = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new JobServiceStubService())
                .build()
                .start();

        inProcessChannel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();
        projectRepository.deleteAllAuthentic();
        projectGraphRepository.deleteAll();
        projectGraphNodeRepository.deleteAll();
        projectJobRepository.deleteAllAuthentic();
        projectJobTaskRepository.deleteAllAuthentic();
        projectScheduleJobRepository.deleteAllAuthentic();

        projectRepository.saveAndFlush(buildProjectDO());
        projectGraphNodeRepository.saveAllAndFlush(buildProjectGraphNodeDOs());
        projectGraphRepository.saveAndFlush(buildProjectGraphDO());
        projectJobTaskRepository.saveAndFlush(buildProjectTaskDO());
        projectJobRepository.saveAndFlush(buildProjectJobDO());
        projectScheduleJobRepository.saveAndFlush(buildProjectScheduleJobDO());
        projectScheduleJobRepository.saveAndFlush(buildProjectScheduleJob2DO());
    }

    @AfterEach
    public void tearDown() {
        inProcessChannel.shutdownNow();
        mockServer.shutdownNow();
        projectRepository.deleteAllAuthentic();
        projectGraphRepository.deleteAll();
        projectGraphNodeRepository.deleteAll();
        projectJobRepository.deleteAllAuthentic();
        projectJobTaskRepository.deleteAllAuthentic();
        projectScheduleJobRepository.deleteAllAuthentic();
    }

    @Test
    void testJobAsyncStub() {
        jobServiceAsyncStub = JobServiceGrpc.newStub(inProcessChannel);
        Mockito.when(dynamicKusciaChannelProvider.createStub("kuscia-system", JobServiceGrpc.JobServiceStub.class)).thenReturn(jobServiceAsyncStub);
        Assertions.assertDoesNotThrow(() -> jobManager.startSync());
        index++;
        Assertions.assertDoesNotThrow(() -> jobManager.startSync());
        Mockito.when(datatableGrantManager.createDomainGrant(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new RuntimeException());
        jobManager.setPlaformType(PlatformTypeEnum.CENTER.name());
        Assertions.assertDoesNotThrow(() -> jobManager.checkOrCreateDomainDataGrant("alice", "bob", "test"));
        jobManager.setPlaformType(PlatformTypeEnum.AUTONOMY.name());
        Assertions.assertDoesNotThrow(() -> jobManager.checkOrCreateDomainDataGrant("alice", "bob", "test"));
    }

    @Test
    void test() {
        Mockito.when(datatableGrantManager.createDomainGrant(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn("");
        jobManager.setPlaformType(PlatformTypeEnum.CENTER.name());
        ProjectTaskDO projectTaskDO = ProjectTaskDO.builder()
                .upk(ProjectTaskDO.UPK.builder().taskId("task").projectId("project").jobId("job").build())
                .parties(List.of("alice"))
                .build();
        Assertions.assertDoesNotThrow(() -> jobManager.checkOrCreateDomainDataGrant("alice", "bob", "test"));
        jobManager.setPlaformType(PlatformTypeEnum.AUTONOMY.name());
        Thread workerThread = new Thread(() -> {
            jobManager.checkOrCreateDomainDataGrant("alice", "bob", "test");
        });
        workerThread.start();
        workerThread.interrupt();
        String distData = """
                {
                      "name": "ouel_uzcbxztr_node_13_output_0",
                      "type": "sf.table.individual",
                      "meta": {
                      "@type": "type.googleapis.com/secretflow.spec.v1.IndividualTable",
                      "schema": {
                      "ids": [
                      "id2"
                      ],
                      "features": [
                      "y"
                      ],
                      "labels": [
                      "pred"
                      ],
                      "idTypes": [
                      "str"
                      ],
                      "featureTypes": [
                      "int"
                      ],
                      "labelTypes": [
                      "float32"
                      ]
                      },
                      "lineCount": "2473"
                      },
                      "dataRefs": [
                      {
                      "uri": "ouel_uzcbxztr_node_13_output_0",
                      "party": "bob",
                      "format": "orc",
                      "nullStrs": []
                      }
                      ]
                      }
                """;
        jobManager.parse(distData);
        jobManager.parse("{\"a\":\"ouel_uzcbxztr_node_13_output_0\",\"b\":\"sf.table");
    }

    ProjectGraphDO buildProjectGraphDO() {
        ProjectGraphDO build = ProjectGraphDO.builder()
                .upk(new ProjectGraphDO.UPK("test", "atxtxxwc"))
                .edges(List.of())
                .nodeMaxIndex(0)
                .ownerId("alice")
                .nodeMaxIndex(0)
                .build();
        build.setNodes(buildProjectGraphNodeDOs());
        return build;
    }

    List<ProjectGraphNodeDO> buildProjectGraphNodeDOs() {
        return List.of(
                ProjectGraphNodeDO.builder()
                        .upk(new ProjectGraphNodeDO.UPK("test", "atxtxxwc", "atxtxxwc-node-1"))
                        .nodeDef("{\"attrPaths\":[\"input/receiver_input/key\",\"input/sender_input/key\"],\"attrs\":[{\"is_na\":false,\"ss\":[\"id1\"]},{\"is_na\":false,\"ss\":[\"id2\"]}],\"domain\":\"data_prep\",\"name\":\"psi\",\"version\":\"0.0.2\"}")
                        .inputs(List.of())
                        .outputs(List.of("atxtxxwc-node-1-output-0"))
                        .build(),
                ProjectGraphNodeDO.builder()
                        .upk(new ProjectGraphNodeDO.UPK("test", "atxtxxwc", "atxtxxwc-node-2"))
                        .nodeDef("{\"attrPaths\":[\"input/receiver_input/key\",\"input/sender_input/key\"],\"attrs\":[{\"is_na\":false,\"ss\":[\"id1\"]},{\"is_na\":false,\"ss\":[\"id2\"]}],\"domain\":\"data_prep\",\"name\":\"psi\",\"version\":\"0.0.2\"}")
                        .inputs(List.of())
                        .outputs(List.of("atxtxxwc-node-2-output-0"))
                        .build()
        );
    }

    ProjectJobDO buildProjectJobDO() {
        Map<String, ProjectTaskDO> taskMap = new HashMap<>();
        taskMap.put("test-atxtxxwc-node-1", ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test", "test-atxtxxwc-node-1"))
                .parties(List.of("bob", "alice"))
                .graphNodeId("atxtxxwc-node-1")
                .graphNode(buildProjectGraphNodeDOs().get(0))
                .build());
        taskMap.put("test-atxtxxwc-node-2", ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test", "test-atxtxxwc-node-2"))
                .parties(List.of("bob", "alice"))
                .graphNodeId("atxtxxwc-node-2")
                .graphNode(buildProjectGraphNodeDOs().get(1))
                .build());
        return ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK("test", "test"))
                .graphId("atxtxxwc")
                .edges(List.of())
                .name("test")
                .tasks(taskMap)
                .status(GraphJobStatus.SUCCEED)
                .build();
    }

    ProjectTaskDO buildProjectTaskDO() {
        return ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test", "test-atxtxxwc-node-1"))
                .parties(List.of("bob", "alice"))
                .graphNode(buildProjectGraphNodeDOs().get(0))
                .build();
    }

    ProjectDO buildProjectDO() {
        return ProjectDO.builder()
                .projectId("test")
                .name("test")
                .description("test")
                .ownerId("kuscia-system")
                .computeMode("MPC")
                .computeFunc("ALL")
                .status(1)
                .build();
    }

    ProjectScheduleJobDO buildProjectScheduleJobDO() {
        Map<String, ProjectTaskDO> taskMap = new HashMap<>();
        taskMap.put("test-atxtxxwc-node-1", ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test1", "test-atxtxxwc-node-1"))
                .parties(List.of("bob", "alice"))
                .graphNodeId("atxtxxwc-node-1")
                .graphNode(buildProjectGraphNodeDOs().get(0))
                .build());
        taskMap.put("test-atxtxxwc-node-2", ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test1", "test-atxtxxwc-node-2"))
                .parties(List.of("bob", "alice"))
                .graphNodeId("atxtxxwc-node-2")
                .graphNode(buildProjectGraphNodeDOs().get(1))
                .build());
        return ProjectScheduleJobDO.builder()
                .upk(new ProjectScheduleJobDO.UPK("test", "test1"))
                .graphId("atxtxxwc")
                .edges(List.of())
                .name("test")
                .tasks(taskMap)
                .owner("kuscia-system")
                .scheduleTaskId("test2")
                .status(GraphJobStatus.RUNNING)
                .build();
    }

    ProjectScheduleJobDO buildProjectScheduleJob2DO() {
        Map<String, ProjectTaskDO> taskMap = new HashMap<>();
        taskMap.put("test-atxtxxwc-node-1", ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test2", "test-atxtxxwc-node-1"))
                .parties(List.of("bob", "alice"))
                .graphNodeId("atxtxxwc-node-1")
                .graphNode(buildProjectGraphNodeDOs().get(0))
                .build());
        taskMap.put("test-atxtxxwc-node-2", ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test2", "test-atxtxxwc-node-2"))
                .parties(List.of("bob", "alice"))
                .graphNodeId("atxtxxwc-node-2")
                .graphNode(buildProjectGraphNodeDOs().get(1))
                .build());
        return ProjectScheduleJobDO.builder()
                .upk(new ProjectScheduleJobDO.UPK("test", "test2"))
                .graphId("atxtxxwc")
                .edges(List.of())
                .name("test")
                .tasks(taskMap)
                .owner("kuscia-system")
                .scheduleTaskId("test2")
                .status(GraphJobStatus.SUCCEED)
                .build();
    }


    public static class JobServiceStubService extends JobServiceGrpc.JobServiceImplBase {
        @Override
        public void watchJob(Job.WatchJobRequest request, StreamObserver<Job.WatchJobEventResponse> responseObserver) {
            responseObserver.onNext(buildPendingdWatchJobEventResponse());
            responseObserver.onNext(buildSuccessdWatchJobEventResponse());
            responseObserver.onNext(buildExitSuccessdWatchJobEventResponse());
            responseObserver.onNext(buildTaskEmptyWatchJobEventResponse());
            responseObserver.onNext(buildJobEndTaskRunPendingdWatchJobEventResponse());
            responseObserver.onNext(buildTaskEmptyJobSuccessWatchJobEventResponse());
            responseObserver.onNext(buildTaskWatchJobEventResponse());
            responseObserver.onNext(buildTaskRuningJobSuccessJobSuccessWatchJobEventResponse());
            responseObserver.onNext(buildTaskRuningJobSuccessJobSuccessWatchJobEventResponse());
            responseObserver.onNext(buildTaskEmptyJobSuccessWatchJobEventResponse());
            responseObserver.onNext(buildTaskEmptyProjectScheduleJobSuccessWatchJobEventResponse());
            responseObserver.onNext(buildTaskRuningProjectScheduleJobSuccessJobSuccessWatchJobEventResponse());
            responseObserver.onNext(buildTaskRuningProjectScheduleJobSuccessJobSuccessButDbJobIsFinishWatchJobEventResponse());
            if (index % 2 == 0) {
                responseObserver.onError(new RuntimeException());
            } else {
                responseObserver.onCompleted();
            }
        }
    }


    /**
     * taskStatus is null
     */
    @Test
    public void testMergeExtraInfoWithNullTaskStatus() {
        Job.TaskStatus taskStatus = null;
        ProjectTaskDO.ExtraInfo extraInfo = new ProjectTaskDO.ExtraInfo();
        ProjectTaskDO.ExtraInfo result = JobManager.mergeExtraInfo(taskStatus, extraInfo);
        Assertions.assertEquals(extraInfo,result);
    }

    @Test
    public void testMergeExtraInfoWithZeroOrNegativeProgress() {
        Job.TaskStatus taskStatus = Job.TaskStatus.newBuilder().setProgress(0.0f).build();
        ProjectTaskDO.ExtraInfo extraInfo = new ProjectTaskDO.ExtraInfo();

        ProjectTaskDO.ExtraInfo result = JobManager.mergeExtraInfo(taskStatus, extraInfo);
        Assertions.assertEquals(extraInfo,result);
    }

    /**
     */
    @Test
    public void testMergeExtraInfoWithNullExtraInfo() {
        Job.TaskStatus taskStatus = Job.TaskStatus.newBuilder().setProgress(0.5f).build();
        ProjectTaskDO.ExtraInfo extraInfo = null;
        ProjectTaskDO.ExtraInfo result = JobManager.mergeExtraInfo(taskStatus, extraInfo);
        Assertions.assertEquals(0.5f, result.getProgress(), 0.0001);
    }

    /**
     */
    @Test
    public void testMergeExtraInfoWithNonNullTaskStatusAndExtraInfo() {
        Job.TaskStatus taskStatus = Job.TaskStatus.newBuilder().setProgress(0.8f).build();
        ProjectTaskDO.ExtraInfo extraInfo = new ProjectTaskDO.ExtraInfo();
        ProjectTaskDO.ExtraInfo result = JobManager.mergeExtraInfo(taskStatus, extraInfo);
        Assertions.assertEquals(0.8f, result.getProgress(), 0.0001);
    }

}