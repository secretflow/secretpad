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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.manager.integration.model.SyncDataDTO;
import org.secretflow.secretpad.service.sync.JpaSyncDataService;
import org.secretflow.secretpad.service.sync.center.SseServer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author yutu
 * @date 2023/10/23
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Profile(value = {SystemConstants.DEFAULT})
public class CenterDataSyncController {
    private final SseServer sseServer;
    private final JpaSyncDataService jpaSyncDataService;

    @GetMapping(value = "/sync", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sync(@RequestHeader("kuscia-origin-source")  String nodeId, @RequestParam String p) {
        @SuppressWarnings(value = {"rawtypes"})
        List<SyncDataDTO> syncDataDTOList = JsonUtils.toJavaList(p, SyncDataDTO.class);
        SseEmitter sseEmitter = sseServer.open(nodeId, syncDataDTOList);
        jpaSyncDataService.syncByLastUpdateTime(nodeId);
        return sseEmitter;
    }

    @Scheduled(initialDelay = 1000, fixedRate = 1000 * 10 * 3)
    public void ping() {
        sseServer.ping();
    }
}