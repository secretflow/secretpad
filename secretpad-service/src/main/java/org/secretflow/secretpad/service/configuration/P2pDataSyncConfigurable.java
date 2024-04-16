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

package org.secretflow.secretpad.service.configuration;


import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.buffer.p2p.P2PDataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.producer.AbstractDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.datasync.producer.PaddingNodeService;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pPaddingNodeServiceImpl;
import org.secretflow.secretpad.persistence.datasync.rest.DataSyncRestTemplate;
import org.secretflow.secretpad.persistence.datasync.rest.p2p.P2pDataSyncRestService;
import org.secretflow.secretpad.persistence.datasync.rest.p2p.P2pDataSyncRestTemplate;
import org.secretflow.secretpad.persistence.datasync.retry.DataSyncRetryTemplate;
import org.secretflow.secretpad.persistence.datasync.retry.impl.ThrowDataSyncRetry;
import org.secretflow.secretpad.persistence.datasync.retry.impl.TryDataSyncRetry;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.persistence.repository.ProjectApprovalConfigRepository;
import org.secretflow.secretpad.persistence.repository.ProjectNodeRepository;
import org.secretflow.secretpad.persistence.repository.VoteRequestRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "secretpad.datasync", value = "p2p", matchIfMissing = false, havingValue = "true")
public class P2pDataSyncConfigurable {
    @Value("${secretpad.gateway}")
    private String kusciaLiteGateway;

    @Value("${secretpad.datasync.retry:fastFailedPolicy}")
    private String retry;

    @Bean
    public PaddingNodeService p2pPaddingNodeServiceImpl(ProjectNodeRepository projectNodeRepository, ProjectApprovalConfigRepository projectApprovalConfigRepository, VoteRequestRepository voteRequestRepository, CacheManager cacheManager) {
        return new P2pPaddingNodeServiceImpl(projectNodeRepository, projectApprovalConfigRepository, voteRequestRepository, cacheManager);
    }

    @Bean
    public DataSyncDataBufferTemplate dataSyncDataBufferTemplate(ApplicationEventPublisher applicationEventPublisher) {
        return new P2PDataSyncDataBufferTemplate(applicationEventPublisher);
    }

    @Bean
    public DataSyncRetryTemplate dataSyncRetryTemplate() {
        switch (retry) {
            case "retryPolicy":
                return new TryDataSyncRetry();
            default:
                return new ThrowDataSyncRetry();
        }
    }


    @Bean
    public DataSyncRestTemplate dataSyncRestTemplate() {
        return new P2pDataSyncRestTemplate();
    }

    @Bean("dataSyncProducerTemplate")
    public AbstractDataSyncProducerTemplate producerTemplate(DataSyncConfig dataSyncConfig,
                                                             DataSyncDataBufferTemplate dataSyncDataBufferTemplate,
                                                             PaddingNodeService p2pPaddingNodeServiceImpl) {
        return new P2pDataSyncProducerTemplate(dataSyncConfig, dataSyncDataBufferTemplate, p2pPaddingNodeServiceImpl);
    }


    @Bean
    public P2pDataSyncRestService p2pDataSyncRestService() {
        if (!kusciaLiteGateway.contains(":")) {
            kusciaLiteGateway = kusciaLiteGateway + ":80";
        }
        WebClient webClient = WebClient.builder()
                .baseUrl("http://" + kusciaLiteGateway)
                .build();
        HttpServiceProxyFactory proxyFactory
                = HttpServiceProxyFactory.builder(
                WebClientAdapter.forClient(webClient)).blockTimeout(Duration.ofSeconds(20)).build();
        return proxyFactory.createClient(P2pDataSyncRestService.class);
    }

    @Bean("dataSyncThreadPool")
    public Executor dataSyncThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(11);
        threadPoolTaskExecutor.setMaxPoolSize(20);
        threadPoolTaskExecutor.setQueueCapacity(10);
        threadPoolTaskExecutor.setThreadNamePrefix("DataSyncThreadPool-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}