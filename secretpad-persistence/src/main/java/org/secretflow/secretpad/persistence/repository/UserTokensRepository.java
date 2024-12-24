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

import org.secretflow.secretpad.persistence.entity.TokensDO;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User tokens repository
 *
 * @author : xiaonan.fhn
 * @date 2023/5/25
 */
public interface UserTokensRepository extends BaseRepository<TokensDO, String> {

    /**
     * Query user information by user token
     *
     * @param token user token
     * @return user information
     */
    @Query("from TokensDO td where td.token=:token")
    Optional<TokensDO> findByToken(@Param("token") String token);

    /**
     * When a user is logged out, the token record of the user is deleted
     *
     * @param name  userName
     * @param token user token record
     */
    @Transactional
    @Modifying
    void deleteByNameAndToken(@Param("name") String name, @Param("token") String token);


    /**
     * the token record of the user is deleted
     *
     * @param name userName
     */
    @Modifying
    @Transactional
    void deleteByName(@Param("name") String name);

}
