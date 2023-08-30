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

import org.secretflow.secretpad.common.util.UUIDUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * Project data object
 *
 * @author jiezi
 * @date 2023/5/25
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Setter
@Getter
@Table(name = "project")
@SQLDelete(sql = "update project set is_deleted = 1 where project_id = ?")
@Where(clause = "is_deleted = 0")
public class ProjectDO extends BaseAggregationRoot<ProjectDO> {

    /**
     * Project id
     */
    @Id
    @Column(name = "project_id", unique = true, nullable = false, length = 64)
    private String projectId;

    /**
     * Project name
     */
    @Column(name = "name", nullable = false, length = 256)
    private String name;

    /**
     * Project description
     */
    @Column(name = "description", nullable = true, columnDefinition = "text")
    private String description;


    /**
     * computeMode pipeline: ,hub:
     */
    private String computeMode;

    /**
     * Create a new project DO class
     */
    public static class Factory {
        public static ProjectDO newProject(String name, String desc, String computeMode) {
            return ProjectDO.builder().projectId(UUIDUtils.random(8)).name(name).description(desc)
                    .computeMode(computeMode).build();
        }
    }

}
