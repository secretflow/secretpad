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

package org.secretflow.secretpad.service.model.approval;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Participant in vote.
 *
 * @author cml
 * @date 2023/09/21
 */
@Builder
@Data
public class Participant {

    /**
     * this id means the node who initiate a vote
     */
    private String nodeID;

    /**
     * the status by this vote
     * <p>
     * {@link org.secretflow.secretpad.service.enums.VoteStatusEnum}
     */
    private String status;

    private String nodeName;

    private List<VoteInfo> voteInfos;


    @Builder
    @Data
    public static class VoteInfo {
        private String voteID;

        /**
         * this id means the node who reply the vote to the initiate
         */
        private String nodeID;

        private String action;

        private String reason;
    }
}
