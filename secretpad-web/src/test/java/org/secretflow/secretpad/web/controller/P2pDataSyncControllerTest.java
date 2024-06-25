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


import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.persistence.datasync.route.RouteDetection;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.repository.*;
import org.secretflow.secretpad.service.sync.p2p.DataSyncConsumerTemplate;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

/**
 * @author yutu
 * @date 2024/02/23
 */
@Slf4j
@TestPropertySource(properties = {
        "secretpad.gateway=127.0.0.1:9001",
        "secretpad.datasync.p2p=true",
        "secretpad.datasync.center=false",
        "secretpad.platform-type=AUTONOMY",
        "secretpad.node-id=test"
})
public class P2pDataSyncControllerTest extends ControllerTest {
    @Resource
    private ProjectRepository projectRepository;
    @Resource
    private ProjectDatatableRepository projectDatatableRepository;
    @Resource
    ProjectNodeRepository projectNodeRepository;
    @Resource
    private ProjectGraphRepository projectGraphRepository;
    @Resource
    private ProjectGraphNodeRepository projectGraphNodeRepository;
    @Resource
    private ProjectJobRepository projectJobRepository;
    @Resource
    private ProjectJobTaskRepository projectJobTaskRepository;
    @Resource
    private VoteRequestRepository voteRequestRepository;
    @Resource
    private VoteInviteRepository voteInviteRepository;
    @Resource
    private ProjectApprovalConfigRepository projectApprovalConfigRepository;
    @Resource
    private ProjectGraphNodeKusciaParamsRepository projectGraphNodeKusciaParamsRepository;
    @Resource
    RouteDetection routeDetection;
    @Resource
    DataSyncConsumerTemplate dataSyncConsumerTemplate;
    @Resource
    ProjectModelServiceRepository projectModelServiceRepository;
    @Resource
    ProjectModelPackRepository projectModelPackRepository;
    @Resource
    NodeRepository nodeRepository;

    @Resource
    ProjectGraphDomainDatasourceRepository projectGraphDomainDatasourceRepository;

    void saveProjectGraphDO() {
        ProjectGraphDO projectGraphDO = ProjectGraphDO.builder()
                .upk(new ProjectGraphDO.UPK("test", "test"))
                .name("test")
                .ownerId("test")
                .nodeMaxIndex(32)
                .maxParallelism(1)
                .build();
        projectGraphRepository.deleteAll();
        projectGraphRepository.saveAndFlush(projectGraphDO);
        projectGraphRepository.delete(projectGraphDO);
    }

    void saveProjectGraphDomainDatasourceDO() {
        ProjectGraphDomainDatasourceDO projectGraphDomainDatasourceDO = ProjectGraphDomainDatasourceDO.builder()
                .upk(new ProjectGraphDomainDatasourceDO.UPK("test", "test", "test"))
                .dataSourceId("test")
                .dataSourceName("test")
                .editEnable(true)
                .build();
        projectGraphDomainDatasourceRepository.deleteAll();
        projectGraphDomainDatasourceRepository.saveAndFlush(projectGraphDomainDatasourceDO);
        projectGraphDomainDatasourceRepository.delete(projectGraphDomainDatasourceDO);
    }


    void saveProjectGraphNodeDO() {
        ProjectGraphNodeDO projectGraphNodeDO = ProjectGraphNodeDO.builder()
                .upk(new ProjectGraphNodeDO.UPK("test", "test", "test"))
                .build();
        projectGraphNodeRepository.deleteAll();
        projectGraphNodeRepository.saveAndFlush(projectGraphNodeDO);
        projectGraphNodeRepository.delete(projectGraphNodeDO);
    }

    void saveProjectJobDO() {
        ProjectJobDO projectJobDO = ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK("test", "test"))
                .name("test")
                .build();
        ProjectJobDO projectJobDO1 = ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK("test1", "test1"))
                .name("test1")
                .status(GraphJobStatus.SUCCEED)
                .build();
        ProjectJobDO projectJobDO2 = ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK("test2", "test2"))
                .name("test2")
                .status(GraphJobStatus.RUNNING)
                .build();
        projectJobRepository.deleteAllAuthentic();
        projectJobRepository.saveAndFlush(projectJobDO);
        projectJobRepository.saveAndFlush(projectJobDO1);
        projectJobRepository.saveAndFlush(projectJobDO2);
        projectJobRepository.delete(projectJobDO);
    }

    void saveProjectTaskDO() {
        ProjectGraphNodeDO projectGraphNodeDO = ProjectGraphNodeDO.builder()
                .upk(new ProjectGraphNodeDO.UPK("test", "test", "test"))
                .build();
        ProjectTaskDO projectTaskDO = ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("test", "test", "test"))
                .parties(List.of("alice", "bob"))
                .graphNode(projectGraphNodeDO)
                .graphNodeId("test")
                .build();
        projectJobTaskRepository.deleteAllAuthentic();
        projectJobTaskRepository.saveAndFlush(projectTaskDO);
        projectJobTaskRepository.delete(projectTaskDO);
    }

    void saveProjectDatatableDO() {
        projectDatatableRepository.deleteAll();
        ProjectDatatableDO projectDatatableDO = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("test", "test", "test"))
                .tableConfig(List.of())
                .source(ProjectDatatableDO.ProjectDatatableSource.CREATED)
                .build();
        projectDatatableRepository.saveAndFlush(projectDatatableDO);
        projectDatatableDO = ProjectDatatableDO.builder()
                .upk(new ProjectDatatableDO.UPK("test", "test1", "test"))
                .tableConfig(List.of())
                .source(ProjectDatatableDO.ProjectDatatableSource.CREATED)
                .build();
        projectDatatableRepository.saveAndFlush(projectDatatableDO);
    }

    void saveVoteRequestDO() {
        VoteRequestDO voteRequestDO = VoteRequestDO.builder()
                .voteID("test")
                .initiator("test")
                .type("create_project")
                .voters(List.of(""))
                .voteCounter("")
                .executors(List.of(""))
                .approvedThreshold(0)
                .status(0)
                .executeStatus("")
                .desc("")
                .build();
        voteRequestRepository.deleteAll();
        voteRequestRepository.saveAndFlush(voteRequestDO);
        voteRequestRepository.delete(voteRequestDO);
    }

    void saveVoteInviteDO() {
        VoteInviteDO voteInviteDO = VoteInviteDO.builder()
                .upk(new VoteInviteDO.UPK("test", "test"))
                .action("")
                .desc("")
                .initiator("test")
                .action("")
                .type("")
                .build();
        voteInviteRepository.deleteAll();
        voteInviteRepository.saveAndFlush(voteInviteDO);
        voteInviteRepository.delete(voteInviteDO);
    }

    void saveProjectApprovalConfigDO() {
        ProjectApprovalConfigDO projectApprovalConfigDO = ProjectApprovalConfigDO.builder()
                .voteID("test")
                .initiator("test")
                .projectId("test")
                .inviteNodeId("test")
                .parties(List.of("test"))
                .type("")
                .build();
        projectApprovalConfigRepository.deleteAll();
        projectApprovalConfigRepository.saveAndFlush(projectApprovalConfigDO);
        projectApprovalConfigRepository.delete(projectApprovalConfigDO);
    }

    void saveProjectGraphNodeKusciaParamsDO() {
        ProjectGraphNodeKusciaParamsDO projectGraphNodeKusciaParamsDO = ProjectGraphNodeKusciaParamsDO.builder()
                .upk(new ProjectGraphNodeKusciaParamsDO.UPK("test", "test", "test"))
                .jobId("test")
                .taskId("test")
                .build();
        projectGraphNodeKusciaParamsRepository.deleteAll();
        projectGraphNodeKusciaParamsRepository.saveAndFlush(projectGraphNodeKusciaParamsDO);
        projectGraphNodeKusciaParamsRepository.delete(projectGraphNodeKusciaParamsDO);
    }

    void saveProjectNode() {
        ProjectNodeDO projectNodeDO = ProjectNodeDO.builder().upk(new ProjectNodeDO.UPK("test", "a")).build();
        projectNodeRepository.deleteAllAuthentic();
        projectNodeRepository.saveAndFlush(projectNodeDO);
    }

    void saveProjectModelServingDO() {
        ProjectModelServingDO.PartyEndpoints partyEndpoints = new ProjectModelServingDO.PartyEndpoints();
        partyEndpoints.setEndpoints("1");
        partyEndpoints.setNodeId("1");
        ProjectModelServingDO projectModelServingDO = ProjectModelServingDO.builder().initiator("a").projectId("test").servingStats("")
                .partyEndpoints(List.of(partyEndpoints))
                .servingId("123")
                .servingInputConfig("123")
                .build();
        projectModelServiceRepository.deleteAll();
        projectModelServiceRepository.saveAndFlush(projectModelServingDO);
        projectModelServiceRepository.delete(projectModelServingDO);
    }

    void saveProjectModelPackDO() {
        ProjectModelPackDO projectModelPackDO = ProjectModelPackDO.builder()
                .modelId("123")
                .initiator("a")
                .projectId("test")
                .modelName("name")
                .sampleTables("213")
                .modelStats(0)
                .servingId("1")
                .modelList(List.of("1"))
                .trainId("1")
                .modelReportId("1")
                .partyDataSources(List.of())
                .build();
        projectModelPackRepository.deleteAll();
        projectModelPackRepository.saveAndFlush(projectModelPackDO);
        projectModelPackRepository.delete(projectModelPackDO);
    }

    void saveNode() {
        nodeRepository.deleteAuthentic("a");
        nodeRepository.saveAndFlush(NodeDO.builder().nodeId("a").mode(0).name("test").controlNodeId("test").build());
    }

    @Test
    void testP2pDatasync() {
        saveProjectNode();
        routeDetection.addAvailableNode("a");
        projectRepository.deleteAllAuthentic();
        saveNode();
        ProjectDO projectDO = ProjectDO.builder()
                .projectId("test")
                .name("test")
                .ownerId("test1")
                .status(0)
                .description("")
                .computeMode("")
                .computeFunc("")
                .build();
        projectRepository.saveAndFlush(projectDO);
        saveProjectGraphDO();
        saveProjectGraphNodeDO();
        saveProjectTaskDO();
        saveProjectJobDO();
        saveProjectDatatableDO();
        saveVoteRequestDO();
        saveVoteInviteDO();
        saveProjectApprovalConfigDO();
        saveProjectGraphNodeKusciaParamsDO();
        saveProjectModelServingDO();
        saveProjectModelPackDO();
        saveProjectGraphDomainDatasourceDO();
        projectRepository.delete(projectDO);
        dataSyncConsumerTemplate.consumer("alice", SyncDataDTO.builder()
                .action("delete")
                .tableName(ProjectDO.class.getTypeName())
                .data(projectDO)
                .build());
        dataSyncConsumerTemplate.consumer("alice", SyncDataDTO.builder()
                .action("update")
                .tableName(ProjectJobDO.class.getTypeName())
                .data(ProjectJobDO.builder()
                        .upk(new ProjectJobDO.UPK("test1", "test1"))
                        .name("test1")
                        .status(GraphJobStatus.RUNNING)
                        .build())
                .build());
        dataSyncConsumerTemplate.consumer("alice", SyncDataDTO.builder()
                .action("create")
                .tableName(ProjectGraphDomainDatasourceDO.class.getTypeName())
                .data(ProjectGraphDomainDatasourceDO.builder()
                        .upk(new ProjectGraphDomainDatasourceDO.UPK("test", "test", "test"))
                        .dataSourceId("test")
                        .dataSourceName("test")
                        .editEnable(true)
                        .build())
                .build());
        projectNodeRepository.deleteAllAuthentic();
    }
}