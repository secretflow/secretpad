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

import org.secretflow.secretpad.common.enums.ProjectStatusEnum;
import org.secretflow.secretpad.persistence.entity.*;
import org.secretflow.secretpad.persistence.model.GraphEdgeDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.model.GraphNodeTaskStatus;
import org.secretflow.secretpad.persistence.model.ResultKind;
import org.secretflow.secretpad.persistence.repository.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author yutu
 * @date 2024/11/27
 */
@Slf4j
public class RepositoryTest extends ControllerTest {

    @Resource
    private UserTokensRepository userTokensRepository;

    @Resource
    private NodeRepository nodeRepository;

    @Resource
    private NodeRouteRepository nodeRouteRepository;

    @Resource
    private ProjectInstRepository projectInstRepository;

    @Resource
    private ProjectJobRepository projectJobRepository;

    @Resource
    private ProjectJobTaskRepository projectJobTaskRepository;

    @Resource
    private ProjectNodeRepository projectNodeRepository;

    @Resource
    private ProjectRepository projectRepository;

    @Resource
    private ProjectResultRepository projectResultRepository;

    @Resource
    private ProjectScheduleJobRepository projectScheduleJobRepository;

    @Test
    public void test() {
        UUID uuid = UUID.randomUUID();
        userTokensRepository.save(TokensDO.builder().name("1").token(uuid.toString()).build());
        Optional<TokensDO> byToken = userTokensRepository.findByToken("1");
        byToken.ifPresent(tokensDO -> {
                    log.info("{}", tokensDO);
                    userTokensRepository.deleteByName("1");
                    userTokensRepository.deleteByNameAndToken("1", uuid.toString());
                }
        );
    }

    @Test
    public void test_nodeRepository() {
        nodeRepository.save(NodeDO.builder()
                .name("alice")
                .nodeId("alicess")
                .instId("AAAA")
                .mode(1)
                .controlNodeId("testNodeId")
                .build());

        NodeDO nodeDO1 = nodeRepository.findByNodeId("alicess");
        if (nodeDO1 != null) {
            log.info("{}", nodeDO1);
            nodeRepository.deleteByNodeId("alicess");
        }
    }

    @Test
    public void testCase1_nodeRouteRepository() {
        nodeRouteRepository.save(NodeRouteDO.builder()
                .routeId("testRouteId")
                .srcNodeId("testSrcNodeId")
                .dstNodeId("testDstNodeId")
                .dstNetAddress("testNetAddress")
                .dstNetAddress("testNetAddress")
                .build());
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId("testRouteId");
        if (nodeRouteDO != null) {
            log.info("{}", nodeRouteDO);
            nodeRouteRepository.deleteBySrcNodeId("testSrcNodeId");
        }
    }

    @Test
    public void testCase2_nodeRouteRepository() {
        nodeRouteRepository.save(NodeRouteDO.builder()
                .routeId("testRouteId")
                .srcNodeId("testSrcNodeId")
                .dstNodeId("testDstNodeId")
                .dstNetAddress("testNetAddress")
                .dstNetAddress("testNetAddress")
                .build());
        NodeRouteDO nodeRouteDO = nodeRouteRepository.findByRouteId("testRouteId");
        if (nodeRouteDO != null) {
            log.info("{}", nodeRouteDO);
            nodeRouteRepository.deleteByDstNodeId("testRouteId");
        }
    }

    @Test
    public void test_projectInstRepository() {
        projectInstRepository.save(ProjectInstDO.builder()
                .upk(new ProjectInstDO.UPK("testProjectId", "testInstId"))
                .build());
        List<ProjectInstDO> projectInstDOS = projectInstRepository.findByInstId("testInstId");
        ProjectInstDO projectInstDO = projectInstDOS.get(0);
        if (projectInstDO != null) {
            log.info("{}", projectInstDO);
            projectInstRepository.deleteByUpkProjectId("testProjectId");
        }
    }

    @Test
    public void test_projectJobRepository() {
        projectJobRepository.save(ProjectJobDO.builder()
                .upk(new ProjectJobDO.UPK("testProjectId", "testJobId"))
                .name("testName")
                .edges(List.of(GraphEdgeDO.builder()
                        .edgeId("testEdgeId")
                        .source("testSource")
                        .sourceAnchor("testSourceAnchor")
                        .targetAnchor("testTargetAnchor")
                        .build()))
                .graphId("testGraphId")
                .status(GraphJobStatus.SUCCEED)
                .build());
        Optional<ProjectJobDO> projectJobDOOptional = projectJobRepository.findByJobId("testJobId");
        projectJobDOOptional.ifPresent(projectJobDO -> {
                    log.info("{}", projectJobDO);
                    projectJobRepository.deleteAllAuthentic();
                }
        );
    }

    @Test
    public void test_projectJobTaskRepository() {
        projectJobTaskRepository.save(ProjectTaskDO.builder()
                .upk(new ProjectTaskDO.UPK("testProjectId", "testJobId", "testTaskId"))
                .status(GraphNodeTaskStatus.SUCCEED)
                .graphNodeId("testGraphNodeId")
                .parties(List.of("testParty"))
                .extraInfo(new ProjectTaskDO.ExtraInfo())
                .graphNode(ProjectGraphNodeDO.builder()
                        .upk(new ProjectGraphNodeDO.UPK("testProjectId", "testJobId", "testNodeId"))
                        .x(1)
                        .y(1)
                        .codeName("testCodeName")
                        .inputs(List.of("testInput"))
                        .outputs(List.of("testOutput"))
                        .build())
                .build());
        List<ProjectTaskDO> projectTaskDOS = projectJobTaskRepository.findLastTimeTasks("testProjectId", "testGraphNodeId");
        ProjectTaskDO projectTaskDO = projectTaskDOS.get(0);
        if (projectTaskDO != null) {
            log.info("{}", projectTaskDO);
            projectJobTaskRepository.deleteAllAuthentic();
        }
    }

    @Test
    public void testCase1_projectNodeRepository() {
        projectNodeRepository.save(ProjectNodeDO.builder()
                .upk(new ProjectNodeDO.UPK("testProjectId", "testNodeId"))
                .build());
        List<ProjectNodeDO> projectNodeDOS = projectNodeRepository.findByProjectId("testProjectId");
        ProjectNodeDO projectNodeDO = projectNodeDOS.get(0);
        if (projectNodeDO != null) {
            log.info("{}", projectNodeDO);
            projectNodeRepository.deleteByUpkProjectId("testProjectId");
        }
    }

    @Test
    public void testCase2_projectNodeRepository() {
        projectNodeRepository.save(ProjectNodeDO.builder()
                .upk(new ProjectNodeDO.UPK("testProjectId3", "testNodeId3"))
                .build());
        List<ProjectNodeDO> projectNodeDOS = projectNodeRepository.findByNodeId("testNodeId3");
        ProjectNodeDO projectNodeDO = projectNodeDOS.get(0);
        if (projectNodeDO != null) {
            log.info("{}", projectNodeDO);
            projectNodeRepository.deleteAllAuthentic();
        }
    }

    @Test
    public void test_projectRepository() {
        projectRepository.save(ProjectDO.builder()
                .projectId("testProjectId")
                .name("testName")
                .description("testDescription")
                .computeFunc("testComputeFunc")
                .computeMode("testComputeMode")
                .ownerId("testOwnerId")
                .projectInfo(ProjectInfoDO.builder()
                        .teeDomainId("testTeeDomainId")
                        .build())
                .status(ProjectStatusEnum.APPROVED.getCode())
                .build());
        List<ProjectDO> projectDOList = projectRepository.findByStatus(ProjectStatusEnum.APPROVED.getCode());
        ProjectDO projectDO = projectDOList.get(0);
        if (projectDO != null) {
            log.info("{}", projectDO);
            projectRepository.deleteAllAuthentic();
        }
    }

    @Test
    public void test_projectResultRepository() {
        projectResultRepository.save(ProjectResultDO.builder()
                .upk(new ProjectResultDO.UPK("testProjectId", ResultKind.FedTable, "testNodeId", "testTaskId"))
                .jobId("testJobId")
                .taskId("testTaskId")
                .build());
        List<ProjectResultDO> projectResultDOS = projectResultRepository.findByProjectId("testProjectId");
        ProjectResultDO projectResultDO = projectResultDOS.get(0);
        if (projectResultDO != null) {
            log.info("{}", projectResultDO);
            projectResultRepository.deleteByJobId("testProjectId", "testJobId");
        }
    }

    @Test
    public void test_projectScheduleJobRepository() {
        projectScheduleJobRepository.save(ProjectScheduleJobDO.builder()
                .upk(new ProjectScheduleJobDO.UPK("testProjectId", "testJobId"))
                .name("testName")
                .edges(List.of(GraphEdgeDO.builder()
                        .edgeId("testEdgeId")
                        .targetAnchor("testTargetAnchor")
                        .sourceAnchor("testSourceAnchor")
                        .target("testTarget")
                        .build()))
                .status(GraphJobStatus.SUCCEED)
                .graphId("testGraphId")
                .scheduleTaskId("testScheduleTaskId")
                .owner("testOwner")
                .build());
        List<ProjectScheduleJobDO> projectScheduleJobDOS = projectScheduleJobRepository.findByProjectIds(List.of("testProjectId"));
        ProjectScheduleJobDO projectScheduleJobDO = projectScheduleJobDOS.get(0);
        if (projectScheduleJobDO != null) {
            log.info("{}", projectScheduleJobDO);
            projectJobRepository.deleteAllAuthentic();
        }
    }

}