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

package org.secretflow.secretpad.service.test;


import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.entity.ProjectInfoDO;
import org.secretflow.secretpad.service.graph.GraphContext;
import org.secretflow.secretpad.service.graph.converter.KusciaTrustedFlowJobConverter;
import org.secretflow.secretpad.service.model.graph.ProjectJob;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.secretflow.v1alpha1.kusciaapi.Job;

/**
 * @author yutu
 * @date 2024/06/17
 */

public class KusciaTrustedFlowJobConverterTest {


    @Test
    public void testConverter_NormalCase() {
        ProjectJob projectJob = new ProjectJob();
        projectJob.setJobId("test");
        projectJob.setMaxParallelism(2);
        GraphContext.set(ProjectDO.builder()
                        .computeMode("tee")
                        .projectInfo(ProjectInfoDO.builder()
                                .teeDomainId("tee")
                                .build()).build(),
                new GraphContext.GraphParties());
        KusciaTrustedFlowJobConverter converter = new KusciaTrustedFlowJobConverter();
        Job.CreateJobRequest request = converter.converter(projectJob);
        Assertions.assertEquals(request.getMaxParallelism(), projectJob.getMaxParallelism());
    }
}
