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

package org.secretflow.secretpad.persistence.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author yutu
 * @date 2024/05/24
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "project_graph_domain_datasource")
public class ProjectGraphDomainDatasourceDO extends BaseAggregationRoot<ProjectGraphDomainDatasourceDO> {

    /**
     * The primary key of the data source is represented by the composite primary key UPK
     */
    @EmbeddedId
    private ProjectGraphDomainDatasourceDO.UPK upk;


    /**
     * the id of the data source cannot be empty
     */
    @Column(name = "data_source_id", nullable = false)
    private String dataSourceId;

    /**
     * the name of the data source cannot be empty
     */
    @Column(name = "data_source_name", nullable = false)
    private String dataSourceName;

    /**
     * whether it is editable or not it cannot be empty
     */
    @Column(name = "edit_enable", nullable = false)
    private Boolean editEnable;

    @JsonIgnore
    @Override
    public String getNodeId() {
        return this.upk.getDomainId();
    }

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        return super.getNodeIds();
    }

    @JsonIgnore
    @Override
    public String getProjectId() {
        return this.upk.getProjectId();
    }

    /**
     * A composite primary key class that is used to combine primary keys into one
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Builder
    public static class UPK implements Serializable {

        @Serial
        private static final long serialVersionUID = -6653511353225143749L;
        /**
         * the project id cannot be empty
         */
        @Column(name = "project_id", nullable = false)
        private String projectId;

        /**
         * graph_id cannot be empty
         */
        @Column(name = "graph_id", nullable = false)
        private String graphId;

        /**
         * domain_id cannot be empty
         */
        @Column(name = "domain_id", nullable = false)
        private String domainId;
    }
}