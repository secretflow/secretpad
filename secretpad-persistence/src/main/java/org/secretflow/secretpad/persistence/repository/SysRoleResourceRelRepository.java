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

import org.secretflow.secretpad.persistence.entity.SysRoleResourceRelDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author beiwei
 * @date 2023/9/13
 */
public interface SysRoleResourceRelRepository extends JpaRepository<SysRoleResourceRelDO, SysRoleResourceRelDO.UPK> {

    @Query("from SysRoleResourceRelDO rrr where rrr.upk.roleCode in (:roleCodes)")
    List<SysRoleResourceRelDO> findByRoleCodes(@Param("roleCodes") List<String> roleCodes);
}
