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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.persistence.entity.ProjectGraphDomainDatasourceDO;
import org.secretflow.secretpad.service.model.project.ProjectGraphDomainDataSourceVO;

import java.util.List;
import java.util.Set;

/**
 * ProjectGraphDomainDatasourceService
 *
 * @author yutu
 * @date 2024/05/24
 */
public interface ProjectGraphDomainDatasourceService {

    /**
     * Get a ProjectGraphDomainDatasource by its id
     *
     * @param projectId The id of the ProjectGraphDomainDatasource to retrieve
     * @param graphId   The id of the ProjectGraphDomainDatasource to retrieve
     * @param domainId  The id of the ProjectGraphDomainDatasource to retrieve
     * @return The ProjectGraphDomainDatasource with the specified id, or null if it doesn't exist
     */
    ProjectGraphDomainDatasourceDO getById(String projectId, String graphId, String domainId);

    /**
     * Save a ProjectGraphDomainDatasource
     *
     * @param projectGraphDomainDatasource The ProjectGraphDomainDatasource to save
     * @return The saved ProjectGraphDomainDatasource
     */
    ProjectGraphDomainDatasourceDO save(ProjectGraphDomainDatasourceDO projectGraphDomainDatasource);


    /**
     * Delete a ProjectGraphDomainDatasource by its id
     *
     * @param projectId The id of the ProjectGraphDomainDatasource to delete
     * @param graphId   The id of the ProjectGraphDomainDatasource to delete
     * @param domainId  The id of the ProjectGraphDomainDatasource to delete
     */
    void deleteById(String projectId, String graphId, String domainId);

    /**
     * Update a ProjectGraphDomainDatasource
     *
     * @param projectGraphDomainDatasource The ProjectGraphDomainDatasource to update
     * @return The updated ProjectGraphDomainDatasource
     */
    ProjectGraphDomainDatasourceDO update(ProjectGraphDomainDatasourceDO projectGraphDomainDatasource);

    /**
     * Get all ProjectGraphDomainDatasources
     *
     * @return A list of all ProjectGraphDomainDatasources
     */
    List<ProjectGraphDomainDatasourceDO> getAll();

    Set<ProjectGraphDomainDataSourceVO.DataSource> getDomainDataSources(String domainId);
}
