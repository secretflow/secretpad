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

import org.secretflow.secretpad.kuscia.v1alpha1.service.impl.KusciaGrpcClientAdapter;
import org.secretflow.secretpad.manager.integration.data.AbstractDataManager;
import org.secretflow.secretpad.manager.integration.data.DataManager;
import org.secretflow.secretpad.manager.integration.datasource.AbstractDatasourceManager;
import org.secretflow.secretpad.manager.integration.datasource.DatasourceManager;
import org.secretflow.secretpad.manager.integration.datatable.AbstractDatatableManager;
import org.secretflow.secretpad.manager.integration.datatable.DatatableManager;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.manager.integration.job.JobManager;
import org.secretflow.secretpad.persistence.repository.*;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

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
     * @param kusciaGrpcClientAdapter domain data service blocking stub
     * @return abstract datatable manager
     */
    @Bean
    AbstractDatatableManager datatableManager(
            KusciaGrpcClientAdapter kusciaGrpcClientAdapter, FeatureTableRepository featureTableRepository
    ) {
        return new DatatableManager(kusciaGrpcClientAdapter, featureTableRepository);
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
     * @return abstract job manager
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
            ProjectReadDtaRepository readDtaRepository,
            ProjectJobTaskRepository taskRepository
    ) {
        return new JobManager(projectJobRepository, datatableManager, resultRepository, fedTableRepository, datatableRepository, ruleRepository, modelRepository, reportRepository, managementRepository, readDtaRepository, taskRepository);
    }

    /**
     * Create a new abstract data manager via domain data service blocking stub
     *
     * @param kusciaGrpcClientAdapter
     * @return abstract data manager
     */
    @Bean
    AbstractDataManager dataManager(
            KusciaGrpcClientAdapter kusciaGrpcClientAdapter
    ) {
        return new DataManager(kusciaGrpcClientAdapter);
    }

    /**
     * Create a new abstract datasource manager via domain data source service blocking stub
     *
     * @param kusciaGrpcClientAdapter
     * @return
     */
    @Bean
    AbstractDatasourceManager datasourceManager(
            KusciaGrpcClientAdapter kusciaGrpcClientAdapter
    ) {
        return new DatasourceManager(kusciaGrpcClientAdapter);
    }
}

