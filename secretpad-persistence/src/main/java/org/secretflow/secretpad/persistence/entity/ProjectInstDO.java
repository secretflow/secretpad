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

package org.secretflow.secretpad.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Project institution data object
 *
 * @author yansi
 * @date 2023/5/30
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@Table(name = "project_inst")
@SQLDelete(sql = "update project_inst set is_deleted = 1 where inst_id = ? and project_id = ? ")
@Where(clause = "is_deleted = 0")
public class ProjectInstDO extends BaseAggregationRoot<ProjectInstDO> {
    /**
     * Project institution unique primary key
     */
    @EmbeddedId
    private UPK upk;

    public static class Factory {
        /**
         * Create a new project institution DO via projectId and instId
         *
         * @param projectId project id
         * @param instId    project institution id
         * @return a new project institution DO
         */
        public static ProjectInstDO newProjectInst(String projectId, String instId) {
            return ProjectInstDO.builder().upk(new UPK(projectId, instId)).build();
        }

        /**
         * Create a new project institution DO list via projectId and instIds
         *
         * @param projectId project id
         * @param instIds   project institution id list
         * @return new project institution DO list
         */
        public static List<ProjectInstDO> newProjectInstList(String projectId, List<String> instIds) {
            List<ProjectInstDO> projectInsts = new ArrayList<>();
            if (CollectionUtils.isEmpty(instIds)) {
                return projectInsts;
            }
            for (String instId : instIds) {
                projectInsts.add(ProjectInstDO.builder().upk(new UPK(projectId, instId)).build());
            }
            return projectInsts;
        }
    }

    /**
     * Project institution unique primary key
     */
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UPK implements Serializable {
        /**
         * Project id
         */
        @Column(name = "project_id", nullable = false, length = 64)
        public String projectId;
        /**
         * Institution id
         */
        @Column(name = "inst_id", nullable = false, length = 64)
        public String instId;
    }

    @Override
    public String getProjectId() {
        return this.upk.projectId;
    }
}
