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

import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TeeDownLoadAuditConfigDO.
 *
 * @author cml
 * @date 2023/09/19
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tee_download_approval_config")
public class TeeDownLoadAuditConfigDO extends BaseAggregationRoot<TeeDownLoadAuditConfigDO> {

    @Id
    @Column(name = "vote_id", nullable = false, length = 64)
    private String voteID;

    @Column(name = "job_id", nullable = false, length = 64)
    private String jobId;


    @Column(name = "task_id", nullable = false, length = 64)
    private String taskID;

    @Column(name = "resource_id", nullable = false, length = 64)
    private String resourceID;

    @Column(name = "resource_type", nullable = false, length = 16)
    private String resourceType;

    @Column(name = "project_id", nullable = false, length = 64)
    private String projectId;

    @Column(name = "graph_id", nullable = false, length = 64)
    private String graphId;

    @Column(name = "all_participants")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> allParticipants;

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        return allParticipants;
    }
}
