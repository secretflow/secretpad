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

package org.secretflow.secretpad.service.graph.chain;

import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.model.GraphJobStatus;
import org.secretflow.secretpad.persistence.repository.ProjectJobRepository;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.graph.converter.KusciaJobConverter;
import org.secretflow.secretpad.service.graph.converter.KusciaTrustedFlowJobConverter;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import lombok.extern.slf4j.Slf4j;
import org.secretflow.v1alpha1.kusciaapi.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Submit job handler
 *
 * @author yansi
 * @date 2023/6/8
 */
@Slf4j
@Component
public class JobSubmittedHandler extends AbstractJobHandler<ProjectJob> {
    @Autowired
    private AbstractJobManager jobManager;
    @Autowired
    private KusciaJobConverter jobConverter;
    @Autowired
    private KusciaTrustedFlowJobConverter trustedFlowJobConverter;
    @Autowired
    private ProjectJobRepository jobRepository;

    @Override
    public int getOrder() {
        return 3;
    }

    /**
     * Save project job data and create a new job
     *
     * @param job target job
     */
    @Override
    public void doHandler(ProjectJob job) {
        if (CollectionUtils.isEmpty(job.getTasks())) {
            ProjectJobDO projectJobDO = ProjectJob.toDO(job);
            projectJobDO.setStatus(GraphJobStatus.SUCCEED);
            jobRepository.save(projectJobDO);
            return;
        }
        Job.CreateJobRequest request;
        if (GraphContext.isTee()) {
            request = trustedFlowJobConverter.converter(job);
        } else {
            request = jobConverter.converter(job);
        }
        log.info("kuscia job  request :{}", request);
        jobManager.createJob(request);
        if (next != null) {
            next.doHandler(job);
        }
    }
}
