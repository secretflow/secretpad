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

import org.secretflow.secretpad.persistence.entity.VoteRequestDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VoteRequestRepository.
 *
 * @author cml
 * @date 2023/09/19
 */
@Repository
public interface VoteRequestRepository extends BaseRepository<VoteRequestDO, String> {

    Optional<VoteRequestDO> findByVoteID(String voteID);

    @Query(value = "select * from vote_request v where v.initiator=:initiator and v.vote_id in :voteIDs and v.type=:type order by id desc limit 1", nativeQuery = true)
    Optional<VoteRequestDO> findFirstByInitiatorAndVoteIDInOrderByGmtCreateDESC(@Param("initiator") String initiator, @Param("voteIDs") List<String> voteIDs, @Param("type") String type);

    List<VoteRequestDO> findByStatusAndExecuteStatus(Integer status, String executedStatus);

    List<VoteRequestDO> findByStatus(Integer status);
}
