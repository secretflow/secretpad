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
import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.service.sync.JpaSyncDataService;

import com.fasterxml.jackson.databind.JavaType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.secretflow.secretpad.service.sync.center.SseSession.SSE_PING_MSG;

/**
 * @author yutu
 * @date 2023/10/23
 */
@Slf4j
@Service
@Profile(value = {SystemConstants.EDGE, SystemConstants.TEST})
@RequiredArgsConstructor
@Configuration
@Data
public class EdgeDataSyncServiceImpl implements EdgeDataSyncService {

    @Value("${secretpad.gateway}")
    private String kusciaLiteGateway;
    @Value("${secretpad.center-platform-service}")
    private String routeHeader;

    private final JpaSyncDataService jpaSyncDataService;
    private final DataSyncConfig dataSyncConfig;

    public static AtomicInteger sseSate = new AtomicInteger(-1);
    private final static String HTTP_PREFIX = "http://";

    @SuppressWarnings(value = {"rawtypes"})
    @Override
    public void start() {
        List<SyncDataDTO> params = log();
        if (!kusciaLiteGateway.startsWith(HTTP_PREFIX)) {
            kusciaLiteGateway = HTTP_PREFIX + kusciaLiteGateway;
        }
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(kusciaLiteGateway + "/sync")).newBuilder();
        String s = JsonUtils.toJSONString(params);
        urlBuilder.addQueryParameter("p", s);
        String url = urlBuilder.build().toString();
        useWebClientSse(url);
        EdgeDataSyncServiceImpl.sseSate.set(1);
    }

    private void useWebClientSse(String url) {
        WebClient webClient = WebClient.create(url);
        Flux<ServerSentEvent<String>> eventStream = webClient.get()
                .header("host", routeHeader)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                })
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)));
        eventStream.subscribe(
                event -> {
                    log.info("id :{} ,data: {}", event.id(), event.data());
                    String id = event.id();
                    String data = event.data();
                    if (!SSE_PING_MSG.equals(id)) {
                        log.info("sync data DO - {}  Data - {}", id, data);
                        try {
                            Class<?> cls = Class.forName(id);
                            JavaType javaType = JsonUtils.makeJavaType(SyncDataDTO.class, cls);
                            @SuppressWarnings(value = {"rawtypes"})
                            SyncDataDTO o = JsonUtils.toJavaObject(data, javaType);
                            jpaSyncDataService.syncData(o);
                        } catch (Exception e) {
                            log.error("sse onEvent sync error {} ", id, e);
                        }
                    }
                },
                error -> {
                    log.error("Error receiving SSE: {}", error.getMessage(), error.getCause());
                    EdgeDataSyncServiceImpl.sseSate.set(-1);
                },
                () -> {
                    log.info("Completed!!!");
                    EdgeDataSyncServiceImpl.sseSate.set(-1);
                }
        );
    }

    @PreDestroy
    @Override
    public void close() {
        EdgeDataSyncServiceImpl.sseSate.set(-1);
    }

    @SuppressWarnings(value = {"rawtypes"})
    @Override
    public List<SyncDataDTO> log() {
        List<String> sync = dataSyncConfig.getSync();
        List<SyncDataDTO> syncDataDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(sync)) {
            sync.forEach(t -> {
                Object s = jpaSyncDataService.logTableLastUpdateTime(t);
                syncDataDTOList.add(SyncDataDTO.builder().tableName(t).lastUpdateTime(s.toString()).build());
            });
        }
        return syncDataDTOList;
    }
}