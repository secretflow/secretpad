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

package org.secretflow.secretpad.manager.configuration;

import org.secretflow.secretpad.manager.integration.data.AbstractDataManager;
import org.secretflow.secretpad.manager.integration.data.DataManager;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.datatable.DatatableManager;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.repository.*;

import org.secretflow.v1alpha1.kusciaapi.DomainDataServiceGrpc;
import org.secretflow.v1alpha1.kusciaapi.JobServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manager configuration init bean
 *
 * @author yansi
 * @date 2023/5/23
 */
@Configuration
public class ManagerConfiguration {

    /**
     * Create a new abstract datatable manager via domain data service blocking stub
     *
     * @param stub domain data service blocking stub
     * @return abstract datatable manager
     */
    @Bean
    AbstractDatatableManager datatableManager(
            DomainDataServiceGrpc.DomainDataServiceBlockingStub stub, FeatureTableRepository featureTableRepository
    ) {
        return new DatatableManager(stub, featureTableRepository);
    }

    /**
     * Create a new abstract job manager via repositories and stubs
     *
     * @param projectJobRepository
     * @param datatableManager
     * @param resultRepository
     * @param fedTableRepository
     * @param datatableRepository
     * @param ruleRepository
     * @param modelRepository
     * @param reportRepository
     * @param managementRepository
     * @param jobStub
     * @return abstract job manager
     */
    @Bean
    AbstractJobManager jobManager(
            ProjectJobRepository projectJobRepository,
            AbstractDatatableManager datatableManager,
            ProjectResultRepository resultRepository,
            ProjectFedTableRepository fedTableRepository,
            ProjectDatatableRepository datatableRepository,
            ProjectRuleRepository ruleRepository,
            ProjectModelRepository modelRepository,
            ProjectReportRepository reportRepository,
            TeeNodeDatatableManagementRepository managementRepository,
            JobServiceGrpc.JobServiceBlockingStub jobStub,
            ProjectReadDtaRepository readDtaRepository,
            ProjectJobTaskRepository taskRepository
    ) {
        return new JobManager(projectJobRepository, datatableManager, resultRepository, fedTableRepository, datatableRepository, ruleRepository, modelRepository, reportRepository, managementRepository, jobStub, readDtaRepository, taskRepository);
    }

    /**
     * Create a new abstract data manager via domain data service blocking stub
     *
     * @param dataStub
     * @return abstract data manager
     */
    @Bean
    AbstractDataManager dataManager(
            DomainDataServiceGrpc.DomainDataServiceBlockingStub dataStub
    ) {
        return new DataManager(dataStub);
    }

}

