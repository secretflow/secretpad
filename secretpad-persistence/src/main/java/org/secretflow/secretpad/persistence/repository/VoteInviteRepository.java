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

import org.secretflow.secretpad.persistence.entity.VoteInviteDO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VoteInviteRepository.
 *
 * @author cml
 * @date 2023/09/20
 */
@Repository
public interface VoteInviteRepository extends BaseRepository<VoteInviteDO, VoteInviteDO.UPK> {


    @Query("from VoteInviteDO d where d.upk.voteID=:voteID")
    List<VoteInviteDO> findByVoteID(@Param("voteID") String voteID);

    @Query(value = "select count(*) from VoteInviteDO d where d.upk.votePartitionID=:votePartitionID and action=:action")
    Long queryPendingCount(@Param("votePartitionID") String votePartitionID, @Param("action") String action);
}
