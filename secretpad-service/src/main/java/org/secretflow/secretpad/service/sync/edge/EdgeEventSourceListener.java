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

package org.secretflow.secretpad.service.sync.edge;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.model.SyncDataDTO;
import org.secretflow.secretpad.service.sync.JpaSyncDataService;

import com.fasterxml.jackson.databind.JavaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static org.secretflow.secretpad.service.sync.center.SseSession.SSE_PING_MSG;

/**
 * @author yutu
 * @date 2023/10/24
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile(value = {SystemConstants.EDGE})
public class EdgeEventSourceListener extends EventSourceListener {

    private final JpaSyncDataService jpaSyncDataService;

    @Override
    public void onOpen(@NotNull final EventSource eventSource, @NotNull final Response response) {
        log.info("edge sse open...resp {}", response);
        EdgeDataSyncServiceImpl.sseSate.set(1);
    }

    @Override
    public void onEvent(@NotNull final EventSource eventSource, final String id, final String type, @NotNull final String data) {
        if (SSE_PING_MSG.equals(id)) {
            return;
        }
        log.info("sync data DO - {}  Data - {}", id, data);
        try {
            Class<?> cls = Class.forName(id);
            JavaType javaType = JsonUtils.makeJavaType(SyncDataDTO.class, cls);
            @SuppressWarnings(value = {"rawtypes"})
            SyncDataDTO o = JsonUtils.toJavaObject(data, javaType);
            jpaSyncDataService.syncData(o);
        } catch (ClassNotFoundException e) {
            log.error("sse sync ClassNotFoundException {} ", id, e);
        }
    }

    @Override
    public void onClosed(@NotNull final EventSource eventSource) {
        log.info("sse close...");
        EdgeDataSyncServiceImpl.sseSate.set(-1);
    }

    @Override
    public void onFailure(@NotNull final EventSource eventSource, final Throwable t, final Response response) {
        log.error("sse exception: resp：{} ex: {}", response, t);
        EdgeDataSyncServiceImpl.sseSate.set(-1);
    }
}