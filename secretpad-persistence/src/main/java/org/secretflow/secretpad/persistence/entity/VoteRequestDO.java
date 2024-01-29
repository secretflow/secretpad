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

import org.secretflow.secretpad.persistence.converter.PartyVoteInfoSetJsonConverter;
import org.secretflow.secretpad.persistence.converter.StringListJsonConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * VoteRequestDO.
 *
 * @author cml
 * @date 2023/09/19
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "vote_request")
@ToString
public class VoteRequestDO extends BaseAggregationRoot<VoteRequestDO> {

    /**
     * unique vote id
     */
    @Id
    @Column(name = "vote_id", nullable = false, length = 64)
    private String voteID;

    /**
     * vote initiator
     */
    @Column(name = "initiator", nullable = false, length = 64)
    private String initiator;

    /**
     * vote invitor
     */
    @Column(name = "voters", nullable = false)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> voters;

    /**
     * vote type
     */
    @Column(name = "type", nullable = false, length = 16)
    private String type;

    /**
     * vote counter,always 'center node'
     */
    @Column(name = "vote_counter", nullable = false, length = 64)
    private String voteCounter;

    /**
     * the executors while vote success who do callback
     */
    @Convert(converter = StringListJsonConverter.class)
    private List<String> executors;

    /**
     * The number of votes needed to succeed
     */
    @Column(name = "approved_threshold", nullable = false)
    private Integer approvedThreshold;

    /**
     * vote msg
     */
    @Column(name = "request_msg")
    private String requestMsg;

    /**
     * vote status
     */
    @Column(name = "status", nullable = false, length = 1)
    private Integer status;

    /**
     * while vote success,need call back,the executeStatus means call back status
     */
    @Column(name = "execute_status", nullable = false, length = 16)
    private String executeStatus;

    /**
     * error msg
     */
    @Column(name = "msg")
    private String msg;

    /**
     * vote desc
     */
    @Column(name = "desc", nullable = false, length = 64)
    private String desc;

    @Column(name = "party_vote_info")
    @Convert(converter = PartyVoteInfoSetJsonConverter.class)
    private Set<PartyVoteInfo> partyVoteInfos;

    @JsonIgnore
    @Override
    public List<String> getNodeIds() {
        List<String> nodeIds = new ArrayList<>();
        nodeIds.add(initiator);
        nodeIds.addAll(voters);
        return nodeIds;
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PartyVoteInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 291568296509217011L;

        private String nodeId;
        private String action;
        private String reason;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
                return false;
            }
            PartyVoteInfo that = (PartyVoteInfo) o;
            return this.nodeId.equals(that.nodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId);
        }
    }
}
