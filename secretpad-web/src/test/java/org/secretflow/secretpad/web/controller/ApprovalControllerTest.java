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

import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.CreateApprovalRequest;
import org.secretflow.secretpad.service.model.approval.NodeRouteVoteConfig;
import org.secretflow.secretpad.service.model.approval.PullStatusRequest;
import org.secretflow.secretpad.web.utils.FakerUtils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

    @Test

    public void create() throws Exception {
        assertResponseWithEmptyData(() -> {
            CreateApprovalRequest createApprovalRequest = new CreateApprovalRequest();
            createApprovalRequest.setNodeID("alice");
            createApprovalRequest.setVoteType(VoteTypeEnum.NODE_ROUTE.name());
            NodeRouteVoteConfig nodeRouteVoteConfig = new NodeRouteVoteConfig();
            nodeRouteVoteConfig.setSrcNodeId("alice");
            nodeRouteVoteConfig.setDesNodeId("alice");
            nodeRouteVoteConfig.setSrcNodeAddr("bob");
            nodeRouteVoteConfig.setDesNodeAddr("bob");
            createApprovalRequest.setVoteConfig(nodeRouteVoteConfig);
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
