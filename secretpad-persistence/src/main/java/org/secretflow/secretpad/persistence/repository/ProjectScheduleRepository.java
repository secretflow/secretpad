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

package org.secretflow.secretpad.persistence.repository;

import org.secretflow.secretpad.persistence.entity.ProjectScheduleDO;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yutu
 * @date 2024/08/27
 */
@Repository
public interface ProjectScheduleRepository extends BaseRepository<ProjectScheduleDO, String> {


    @Modifying
    @Transactional
    @Query(value = "update project_schedule set status = :status where schedule_id = :scheduleId and is_deleted = 0", nativeQuery = true)
    void updateStatusByScheduleId(String scheduleId, String status);
}