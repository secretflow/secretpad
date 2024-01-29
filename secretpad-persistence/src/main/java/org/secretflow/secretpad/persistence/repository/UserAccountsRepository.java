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

import org.secretflow.secretpad.persistence.entity.AccountsDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * User accounts repository
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
public interface UserAccountsRepository extends BaseRepository<AccountsDO, String> {

    /**
     * Query user information by userName
     *
     * @param name userName
     * @return user information
     */
    @Query("from AccountsDO nd where nd.name=:name")
    Optional<AccountsDO> findByName(@Param("name") String name);


    /**
     * Query user information by ownerId
     *
     * @param ownerId ownerId
     * @return user information
     */
    @Query("from AccountsDO nd where nd.ownerId=:ownerId")
    List<AccountsDO> findByOwnerId(@Param("ownerId") String ownerId);


    /**
     * Find all users that have been locked and limit 1
     *
     * @return {@link Optional }<{@link AccountsDO }>
     */

    @Query("from AccountsDO where lockedInvalidTime  is not null order by lockedInvalidTime desc limit 1")
    Optional<AccountsDO> findLockedUser();
}
