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

package org.secretflow.secretpad.service.model.project;

import org.secretflow.secretpad.persistence.entity.ProjectResultDO;
import org.secretflow.secretpad.persistence.model.ResultKind;

import com.google.common.collect.Lists;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Merged project result
 *
 * @author yansi
 * @date 2023/5/25
 */
@Getter
@Setter
@Builder
public class MergedProjectResult {
    /**
     * Start time
     */
    LocalDateTime gmtCreate;
    /**
     * Update time
     */
    LocalDateTime gmtModified;
    /**
     * Project id
     */
    private String projectId;
    /**
     * Result kind enum
     */
    private ResultKind kind;
    /**
     * Project result relative nodeId list
     */
    private List<String> nodeIds;
    /**
     * Ref id, domain data id in ApiLite
     */
    private String refId;
    /**
     * Project result relative jobId
     */
    private String jobId;
    /**
     * Project result relative taskId
     */
    private String taskId;

    /**
     * Batch build merged project result list from project result data object list
     *
     * @param results project result data object list
     * @return merged project result list
     */
    public static List<MergedProjectResult> of(List<ProjectResultDO> results) {
        Map<MergedProjectResultId, List<ProjectResultDO>> maps = results.stream().collect(Collectors.groupingBy(it ->
                new MergedProjectResult.MergedProjectResultId(it.getUpk().getProjectId(), it.getUpk().getKind(), it.getUpk().getRefId())
        ));
        List<MergedProjectResult> mergedResult = Lists.newArrayList();
        maps.forEach((key, value) -> mergedResult.add(
                MergedProjectResult.builder()
                        .projectId(key.getProjectId())
                        .kind(key.getKind())
                        .refId(key.getRefId())
                        .jobId(value.get(0).getJobId()) // must have least one element.
                        .taskId(value.get(0).getTaskId())
                        .gmtCreate(value.get(0).getGmtCreate())
                        .gmtModified(value.get(0).getGmtModified())
                        .nodeIds(value.stream().map(r -> r.getUpk().getNodeId()).collect(Collectors.toList()))
                        .build()
        ));
        return mergedResult;
    }

    /**
     * Merged project result id class
     */
    @Data
    @AllArgsConstructor
    public static class MergedProjectResultId {
        /**
         * Project id
         */
        private String projectId;
        /**
         * Result kind enum
         */
        private ResultKind kind;
        /**
         * Ref id, domain data id in ApiLite
         */
        private String refId;
    }
}
