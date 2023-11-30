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

package org.secretflow.secretpad.service.sync.center;

import org.secretflow.secretpad.manager.integration.model.SyncDataDTO;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author yutu
 * @date 2023/10/23
 */
public interface SseServer {
    /**
     * sse server open by one sse client come in
     *
     * @param userId          sse client tag
     * @param syncDataDTOList sse client sync data tag
     * @return sseEmitter channel
     */
    @SuppressWarnings(value = {"rawtypes"})
    SseEmitter open(String userId, List<SyncDataDTO> syncDataDTOList);

    /**
     * sse server open by one sse client come in
     *
     * @param userId sse client tag
     * @return sseEmitter channel
     */
    SseEmitter open(String userId);


    /**
     * sse server send msg to client by client userId
     *
     * @param userId  sse client tag
     * @param content send msg
     * @return boolean
     */
    boolean send(String userId, SyncDataDTO<?> content);

    /**
     * sse server close by userId
     *
     * @param userId sse client tag
     * @return boolean
     */
    boolean close(String userId);

    /**
     * sse server send ping msg to client for heartbeat
     */
    void ping();
}