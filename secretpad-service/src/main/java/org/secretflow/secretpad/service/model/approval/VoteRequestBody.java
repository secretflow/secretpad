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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * VoteRequestBody.
 *
 * @author cml
 * @date 2023/10/12
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteRequestBody {
    @JsonProperty("vote_request_id")
    private String voteRequestID;


    private String type;

    private String initiator;

    @JsonProperty("vote_counter")
    private String voteCounter;

    private List<String> voters;

    private List<String> executors;

    @JsonProperty("approved_threshold")
    private Integer approvedThreshold;

    @JsonProperty("approved_action")
    private String approvedAction;

    @JsonProperty("rejected_action")
    private String rejectedAction;
}
