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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yutu
 * @date 2023/10/23
 */
@Slf4j
@SuppressWarnings(value = {"rawtypes"})
public class SseSession {
    public static final String SSE_PING_MSG = "ping";
    public static Map<String, SseEmitter> sessionMap = new ConcurrentHashMap<>();
    public static Map<String, List<SyncDataDTO>> sessionTableMap = new ConcurrentHashMap<>();

    public static void add(String sessionKey, SseEmitter sseEmitter, List<SyncDataDTO> syncDataDTOList) {
        add(sessionKey, sseEmitter);
        sessionTableMap.put(sessionKey, syncDataDTOList);
    }

    public static void add(String sessionKey, SseEmitter sseEmitter) {
        sessionMap.put(sessionKey, sseEmitter);
        log.info("received node :{}", sessionKey);
    }

    public static boolean exists(String sessionKey) {
        return sessionMap.get(sessionKey) != null;
    }

    public static boolean remove(String sessionKey) {
        return remove(sessionKey, true);
    }

    public static boolean remove(String sessionKey, boolean close) {
        SseEmitter sseEmitter = sessionMap.remove(sessionKey);
        if (close && sseEmitter != null) {
            sseEmitter.complete();
        }
        sessionTableMap.remove(sessionKey);
        return false;
    }

    public static void onError(String sessionKey, Throwable throwable) {
        sessionMap.remove(sessionKey);
        sessionTableMap.remove(sessionKey);
        log.error("sse connection error ", throwable);
    }

    public static void send(String sessionKey, SyncDataDTO<?> content) throws IOException {
        SseEmitter.SseEventBuilder build = SseEmitter.event().id(content.getTableName()).data(content);
        SseEmitter sseEmitter = sessionMap.get(sessionKey);
        if (ObjectUtils.isNotEmpty(sseEmitter)) {
            log.info("*** get data sync , sse send to {} , data is  {} ", sessionKey, content);
            sseEmitter.send(build);
        }
    }

    public static void ping(String sessionKey) throws IOException {
        SseEmitter.SseEventBuilder build = SseEmitter.event().id(SSE_PING_MSG).data("");
        SseEmitter sseEmitter = sessionMap.get(sessionKey);
        if (ObjectUtils.isNotEmpty(sseEmitter)) {
            sseEmitter.send(build);
        }
    }

    public static void sendAll(SyncDataDTO<?> content) {
        sessionMap.keySet().forEach(k -> {
            try {
                send(k, content);
            } catch (IOException e) {
                log.error("sse send data error {}", k, e);
            }
        });
    }

}