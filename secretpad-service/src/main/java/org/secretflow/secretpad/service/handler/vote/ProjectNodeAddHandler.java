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

package org.secretflow.secretpad.service.handler.vote;

import org.secretflow.secretpad.persistence.entity.VoteRequestDO;
import org.secretflow.secretpad.persistence.repository.NodeRepository;
import org.secretflow.secretpad.persistence.repository.ProjectRepository;
import org.secretflow.secretpad.persistence.repository.VoteInviteRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;
import org.secretflow.secretpad.service.CertificateService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.approval.AbstractVoteConfig;
import org.secretflow.secretpad.service.model.message.AbstractVoteTypeMessage;
import org.secretflow.secretpad.service.model.message.MessageDetailVO;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author cml
 * @date 2023/11/24
 */
@Component
public class ProjectNodeAddHandler extends AbstractVoteTypeHandler {
    public ProjectNodeAddHandler(VoteInviteRepository voteInviteRepository, VoteRequestRepository voteRequestRepository, NodeRepository nodeRepository, EnvService envService, CertificateService certificateService, ProjectRepository projectRepository) {
        super(voteInviteRepository, voteRequestRepository, nodeRepository, envService, certificateService, projectRepository);
    }

    @Override
    public MessageDetailVO getVoteMessageDetail(Boolean isInitiator, String nodeID, String voteID) {
        return null;
    }

    @Override
    public List<VoteTypeEnum> supportTypes() {
        return Lists.newArrayList(VoteTypeEnum.PROJECT_NODE_ADD);
    }

    @Override
    public AbstractVoteTypeMessage getMessageListNecessaryInfo(String voteID) {
        return null;
    }

    @Override
    protected void preCheck(String nodeID, AbstractVoteConfig voteConfig) {

    }

    @Override
    protected void createVoteConfig(String voteID, String nodeID, AbstractVoteConfig voteConfig) {

    }

    @Override
    protected String getApprovedAction(String nodeID, AbstractVoteConfig voteConfig) {
        return null;
    }

    @Override
    protected String getRejectAction(String nodeID, AbstractVoteConfig voteConfig) {
        return null;
    }

    @Override
    protected List<String> getExecutors(String nodeId, AbstractVoteConfig voteConfig) {
        return null;
    }

    @Override
    protected List<String> getVoters(String nodeID, AbstractVoteConfig voteConfig) {
        return null;
    }

    @Override
    protected String getVoteType() {
        return null;
    }

    @Override
    protected String getRequestDesc(String nodeID, AbstractVoteConfig voteConfig) {
        return null;
    }

    @Override
    protected String getInviteDesc(String nodeID, AbstractVoteConfig voteConfig) {
        return null;
    }

    @Override
    public void doCallBackApproved(VoteRequestDO voteRequestDO) {

    }
}
