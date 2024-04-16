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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VoteInviteDO.
 *
 * @author cml
 * @date 2023/09/20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "vote_invite")
public class VoteInviteDO extends BaseAggregationRoot<VoteInviteDO> {

    /**
     * unique union id
     */
    @EmbeddedId
    VoteInviteDO.UPK upk;

    /**
     * vote initiator
     */
    @Column(name = "initiator", nullable = false, length = 64)
    private String initiator;
    /**
     * vote msg
     */
    @Column(name = "vote_msg", nullable = false)
    private String voteMsg;

    /**
     * vote reply,Approved or Rejected
     */
    @Column(name = "action", nullable = false, length = 16)
    private String action;

    /**
     * vote type,ex:TEE_DOWN_LOAD,NODE_ROUTE
     */
    @Column(name = "type", nullable = false, length = 16)
    private String type;

    /**
     * the reason why rejected
     */
    @Column(name = "reason", nullable = false, length = 64)
    private String reason;

    /**
     * vote desc
     */
    @Column(name = "description", nullable = false, length = 64)
    private String desc;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class UPK implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;

        @Column(name = "vote_id", nullable = false, length = 64)
        private String voteID;

        @Column(name = "vote_participant_id", nullable = false, length = 64)
        private String votePartitionID;


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            VoteInviteDO.UPK that = (VoteInviteDO.UPK) o;
            return this.voteID.equals(that.voteID)
                    && this.votePartitionID.equals(that.votePartitionID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(voteID, votePartitionID);
        }
    }

    @Override
    @JsonIgnore
    public String getNodeId() {
        return this.upk.votePartitionID;
    }

    @Override
    @JsonIgnore
    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(initiator);
        nodeIds.add(this.upk.votePartitionID);
        return nodeIds;
    }

    public String obtainVoteID() {
        return this.upk.getVoteID();
    }
}
