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

import org.secretflow.secretpad.service.model.common.AbstractPageResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MessageListVO.
 *
 * @author cml
 * @date 2023/09/22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageListVO extends AbstractPageResponse {

    List<MessageVO> messages;

    public static MessageListVO newInstance(List<MessageVO> messages, Integer pageNum, Integer pageSize, Long total) {
        MessageListVO messageListVO = new MessageListVO();
        messageListVO.setMessages(messages);
        messageListVO.setPageNum(pageNum);
        messageListVO.setPageSize(pageSize);
        messageListVO.setTotal(total);
        messageListVO.setTotalPage(total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        return messageListVO;
    }
}
