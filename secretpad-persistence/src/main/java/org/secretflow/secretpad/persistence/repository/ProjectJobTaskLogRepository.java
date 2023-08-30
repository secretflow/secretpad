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

package org.secretflow.secretpad.persistence.repository;

import org.secretflow.secretpad.persistence.entity.ProjectJobTaskLogDO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Project job task log repository
 *
 * @author yansi
 * @date 2023/5/31
 */
@Repository
public interface ProjectJobTaskLogRepository extends JpaRepository<ProjectJobTaskLogDO, String> {

    /**
     * Query project job task log results by jobId and taskId
     *
     * @param jobId  target jobId
     * @param taskId target taskId
     * @return project job task log results
     */
    @Query("from ProjectJobTaskLogDO d where d.jobId=:jobId and d.taskId=:taskId order by d.gmtCreate asc")
    List<ProjectJobTaskLogDO> findAllByJobTaskId(@Param("jobId") String jobId, @Param("taskId") String taskId);
}
