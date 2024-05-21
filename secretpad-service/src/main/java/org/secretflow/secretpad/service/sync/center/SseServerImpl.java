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

import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author yutu
 * @date 2023/10/23
 */
@Slf4j
@Service
@SuppressWarnings(value = {"rawtypes"})
public class SseServerImpl implements SseServer {
    @Override
    public SseEmitter open(String userId, List<SyncDataDTO> syncDataDTOList) {
        checkClient(userId);
        if (SseSession.exists(userId)) {
            SseSession.remove(userId);
            SseSession.sessionTableMap.remove(userId);
        }
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitter.onError((err) -> {
            log.error("type: SseSession Error, msg: {} session Id : {}", err.getMessage(), userId);
            SseSession.onError(userId, err);
            SseSession.remove(userId);
            SseSession.sessionTableMap.remove(userId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("type: SseSession Timeout, session Id : {}", userId);
            SseSession.remove(userId);
            SseSession.sessionTableMap.remove(userId);
        });
        sseEmitter.onCompletion(() -> {
            log.info("type: SseSession Completion, session Id : {}", userId);
            SseSession.remove(userId);
            SseSession.sessionTableMap.remove(userId);
        });
        SseSession.add(userId, sseEmitter, syncDataDTOList);
        return sseEmitter;
    }

    @Override
    public SseEmitter open(String userId) {
        if (SseSession.exists(userId)) {
            SseSession.remove(userId);
        }
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitter.onError((err) -> {
            log.error("sse open by userId, type: SseSession Error, msg: {} session Id : {}", err.getMessage(), userId);
            SseSession.onError(userId, err);
            SseSession.remove(userId);
        });
        sseEmitter.onTimeout(() -> {
            log.info("sse open by userId, type: SseSession Timeout, session Id : {}", userId);
            SseSession.remove(userId);
        });
        sseEmitter.onCompletion(() -> {
            log.info("sse open by userId, type: SseSession Completion, session Id : {}", userId);
            SseSession.remove(userId);
        });
        SseSession.add(userId, sseEmitter);
        return sseEmitter;
    }

    @Override
    public boolean send(String userId, SyncDataDTO<?> content) {
        if (SseSession.exists(userId)) {
            try {
                log.info("sync nodeId:{} data:{}", userId, content);
                SseSession.send(userId, content);
                return true;
            } catch (IOException exception) {
                log.error("type: SseSession send Error:IOException, msg: {} session Id : {}", exception.getMessage(), userId);
            }
        } else {
            throw SecretpadException.of(SystemErrorCode.SSE_ERROR, "User Id " + userId + " not Found");
        }
        return false;
    }

    @Override
    public boolean close(String userId) {
        log.info("type: SseSession Close, session Id : {}", userId);
        return SseSession.remove(userId);
    }

    @Override
    public void ping() {
        Set<String> keySet = SseSession.sessionMap.keySet();
        keySet.forEach(k -> {
            try {
                SseSession.ping(k);
            } catch (IOException e) {
                SseSession.remove(k, true);
                SseSession.sessionTableMap.remove(k);
                log.error("ping sse client error {} close it", k, e);
            }
        });
    }

    public void checkClient(String userId) {
        if (StringUtils.isEmpty(userId)) {
            throw SecretpadException.of(SystemErrorCode.SSE_ERROR, "Unknown client");
        }
    }
}