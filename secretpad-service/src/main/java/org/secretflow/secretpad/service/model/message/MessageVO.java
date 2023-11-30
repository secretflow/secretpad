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

package org.secretflow.secretpad.service.model.message;

import lombok.Builder;
import lombok.Data;

/**
 * MessageVO.
 *
 * @author cml
 * @date 2023/09/22
 */
@Data
@Builder
public class MessageVO {

    /**
     * see {@link org.secretflow.secretpad.service.enums.VoteTypeEnum}
     */
    private String type;

    /**
     * see {@link org.secretflow.secretpad.service.enums.VoteStatusEnum}
     * approved/rejected/null
     * <p>
     * for indicator this status id decided by multi-party
     * all party approve,it is approved
     * one party reject,it is rejected
     * no party reject,some party approve and some party not voted,it is waiting approved
     * <p>
     * for vote receiver,the status is only its own status
     */
    private String status;

    private AbstractInitiatingTypeMessage initiatingTypeMessage;

    private AbstractVoteTypeMessage voteTypeMessage;

    /**
     * messageName
     */
    private String messageName;

    /**
     * create time
     */
    private String createTime;

    /**
     * vote id
     */
    private String voteID;


}
