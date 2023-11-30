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
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.service.sync.JpaSyncDataService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.internal.sse.RealEventSource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yutu
 * @date 2023/10/23
 */
@Slf4j
@Service
@Profile(value = {SystemConstants.EDGE})
@RequiredArgsConstructor
@Configuration
@Data
public class EdgeDataSyncServiceImpl implements EdgeDataSyncService {

    @Value("${secretpad.gateway}")
    private String kusciaLiteGateway;
    @Value("${secretpad.center-platform-service}")
    private String routeHeader;
    private final EdgeEventSourceListener edgeEventSourceListener;

    private final JpaSyncDataService jpaSyncDataService;
    private final DataSyncConfig dataSyncConfig;
    private RealEventSource realEventSource;
    private OkHttpClient client;

    public static AtomicInteger sseSate = new AtomicInteger(-1);
    private final static String HTTP_PREFIX = "http://";

    @SuppressWarnings(value = {"rawtypes"})
    @Override
    public void start() {
        List<SyncDataDTO> params = log();
        client = new OkHttpClient.Builder()
                .connectTimeout(60L, TimeUnit.SECONDS)
                .readTimeout(0L, TimeUnit.DAYS)
                .writeTimeout(0L, TimeUnit.DAYS)
                .build();
        if (!kusciaLiteGateway.startsWith(HTTP_PREFIX)) {
            kusciaLiteGateway = HTTP_PREFIX + kusciaLiteGateway;
        }
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(kusciaLiteGateway + "/sync")).newBuilder();
        String s = JsonUtils.toJSONString(params);
        urlBuilder.addQueryParameter("p", s);
        String url = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .header("host", routeHeader)
                .build();
        realEventSource = new RealEventSource(request, edgeEventSourceListener);
        realEventSource.connect(client);
        EdgeDataSyncServiceImpl.sseSate.set(1);
    }

    @PreDestroy
    @Override
    public void close() {
        if (ObjectUtils.isNotEmpty(realEventSource)) {
            log.info("sse close...");
            realEventSource.cancel();
            client.dispatcher().executorService().shutdown();
        }
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