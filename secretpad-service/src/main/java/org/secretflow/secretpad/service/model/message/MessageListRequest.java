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

import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.model.common.SecretPadPageRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * MessageListRequest.
 *
 * @author cml
 * @date 2023/09/22
 */
@Getter
@Setter
public class MessageListRequest extends SecretPadPageRequest {

    /**
     * if i am initiator
     */
    private Boolean isInitiator;

    /**
     * requester nodeID
     */
    @NotBlank
    private String nodeID;

    /**
     * if the message has been processed
     */
    private Boolean isProcessed;

    /**
     * vote type
     * {@link VoteTypeEnum}
     */
    private String type;

    /**
     * fuzzy search
     */
    private String keyWord;
}
