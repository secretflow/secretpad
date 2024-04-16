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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.util.Base64Utils;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.CreateApprovalRequest;
import org.secretflow.secretpad.service.model.approval.NodeRouteVoteConfig;
import org.secretflow.secretpad.service.model.approval.ProjectCreateApprovalConfig;
import org.secretflow.secretpad.service.model.approval.PullStatusRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.secretflow.v1alpha1.common.Common;
import org.secretflow.v1alpha1.kusciaapi.Certificate;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

/**
 * ApprovalControllerTest.
 *
 * @author cml
 * @date 2023/11/09
 * @since 4.3
 */
public class ApprovalControllerTest extends ControllerTest {


    @MockBean
    private ProjectJobRepository projectJobRepository;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private CertificateService certificateService;

    @Test
    public void createNodeRoute() throws Exception {
        assertResponseWithEmptyData(() -> {
            CreateApprovalRequest createApprovalRequest = new CreateApprovalRequest();
            createApprovalRequest.setNodeID("alice");
            createApprovalRequest.setVoteType(VoteTypeEnum.NODE_ROUTE.name());
            NodeRouteVoteConfig nodeRouteVoteConfig = new NodeRouteVoteConfig();
            nodeRouteVoteConfig.setSrcNodeId("alice");
            nodeRouteVoteConfig.setDesNodeId("alice");
            nodeRouteVoteConfig.setSrcNodeAddr("http://127.0.0.1:8080");
            nodeRouteVoteConfig.setDesNodeAddr("http://127.0.0.1:8080");
            createApprovalRequest.setVoteConfig(nodeRouteVoteConfig);
            return MockMvcRequestBuilders.post(getMappingUrl(ApprovalController.class, "create", CreateApprovalRequest.class))
                    .content(JsonUtils.toJSONString(createApprovalRequest));
        });
    }

    private ProjectDO buildProjectDO() {
        return ProjectDO.builder().projectId(PROJECT_ID).name("project").build();
    }

    @Test
    public void createProjectCreate() throws Exception {
        assertResponseWithEmptyData(() -> {
            Mockito.when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(buildProjectDO()));
            String key = FileUtils.readFile2String("./config/certs/client.pem");
            key = Base64Utils.encode(key.getBytes());
            Certificate.GenerateKeyCertsResponse generateKeyCertsResponse = Certificate.GenerateKeyCertsResponse.newBuilder().setKey(key).setStatus(Common.Status.newBuilder().setCode(0).build()).addCertChain("a").addCertChain("b").build();
            Mockito.when(certificateService.generateCertByNodeID(Mockito.anyString())).thenReturn(generateKeyCertsResponse);
            CreateApprovalRequest createApprovalRequest = new CreateApprovalRequest();
            createApprovalRequest.setNodeID("alice");
            createApprovalRequest.setVoteType(VoteTypeEnum.PROJECT_CREATE.name());
            ProjectCreateApprovalConfig projectCreateApprovalConfig = new ProjectCreateApprovalConfig();
            projectCreateApprovalConfig.setNodes(Lists.newArrayList("alice", "bob"));
            projectCreateApprovalConfig.setProjectId(PROJECT_ID);
            createApprovalRequest.setVoteConfig(projectCreateApprovalConfig);
            return MockMvcRequestBuilders.post(getMappingUrl(ApprovalController.class, "create", CreateApprovalRequest.class))
                    .content(JsonUtils.toJSONString(createApprovalRequest));
        });
    }

    @Test
    public void pullStatus() throws Exception {
        assertResponse(() -> {
            PullStatusRequest pullStatusRequest = FakerUtils.fake(PullStatusRequest.class);
            pullStatusRequest.setResourceType("table");
            Mockito.when(projectJobRepository.findById(new ProjectJobDO.UPK(pullStatusRequest.getProjectID(), pullStatusRequest.getJobID()))).thenReturn(Optional.of(FakerUtils.fake(ProjectJobDO.class)));
            return MockMvcRequestBuilders.post(getMappingUrl(ApprovalController.class, "pullStatus", PullStatusRequest.class))
                    .content(JsonUtils.toJSONString(pullStatusRequest));
        });
    }
}
