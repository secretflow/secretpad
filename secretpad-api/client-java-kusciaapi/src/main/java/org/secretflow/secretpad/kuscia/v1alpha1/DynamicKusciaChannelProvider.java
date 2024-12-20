/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.kuscia.v1alpha1;

import org.secretflow.secretpad.kuscia.v1alpha1.constant.KusciaProtocolEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.event.RegisterKusciaEvent;
import org.secretflow.secretpad.kuscia.v1alpha1.event.UnRegisterKusciaEvent;
import org.secretflow.secretpad.kuscia.v1alpha1.factory.KusciaApiChannelFactory;
import org.secretflow.secretpad.kuscia.v1alpha1.factory.impl.GrpcKusciaApiChannelFactory;
import org.secretflow.secretpad.kuscia.v1alpha1.model.DynamicKusciaGrpcConfig;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;

import io.grpc.stub.AbstractStub;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.secretflow.v1alpha1.kusciaapi.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author yutu
 * @date 2024/06/12
 */
@Slf4j
@Service
@SuppressWarnings({"unchecked"})
public class DynamicKusciaChannelProvider {

    private static final Map<String, KusciaApiChannelFactory> CHANNEL_FACTORIES = new ConcurrentHashMap<>();
    private final static int BLOCKING_TIMEOUT_MILLISECOND = 5000;
    private final static int FUTURE_TIMEOUT_MILLISECOND = 5000;
    private final static int StubSCRIPTION_TIMEOUT_DAY = 365;
    private final Object lock = new Object();
    private volatile boolean isInitialized = false;
    @Resource
    @Setter
    private DynamicKusciaGrpcConfig dynamicKusciaGrpcConfig;

    @Resource
    private ApplicationEventPublisher publisher;

    @Value("${secretpad.node-id}")
    @Setter
    private String nodeId;

    @Value("${secretpad.kuscia-path:./config/kuscia/}")
    private String kusciaPath;

    private static <T> @NotNull String getServiceName(Class<T> clazz) {
        String serviceName;
        try {
            serviceName = String.valueOf(clazz.getDeclaredField("SERVICE_NAME").get(null));
            Assert.notNull(serviceName, "SERVICE_NAME must not be null");
        } catch (Exception e) {
            throw new IllegalArgumentException("Unsupported class type: " + clazz.getName(), e);
        }
        return serviceName;
    }

    @PostConstruct
    public void init() {
        isInitialized = true;
        if (!CollectionUtils.isEmpty(dynamicKusciaGrpcConfig.getNodes())) {
            for (KusciaGrpcConfig config : dynamicKusciaGrpcConfig.getNodes()) {
                log.info("Init kuscia node, config={}", config);
                registerKuscia(config);
                log.info("Init kuscia node success, CHANNEL_FACTORIES={}", CHANNEL_FACTORIES);
            }
        }
        isInitialized = false;
        try {
            serializableKusciaConfigFileInit();
        } catch (Exception e) {
            log.error("Load kuscia config by config file error", e);
        }
    }

    @PreDestroy
    public void destroy() {
        CHANNEL_FACTORIES.forEach((key, value) -> value.shutdown());
    }

    public <T extends AbstractStub<T>> T currentStub(Class<T> clazz) {
        log.info("The nodeId received by kuscia is: {}", nodeId);
        return createStub(nodeId, clazz);
    }

    public void registerKuscia(KusciaGrpcConfig config) {
        Assert.notNull(config, "KusciaGrpcConfig must not be null");
        config.validateAndProcess();
        if (dynamicKusciaGrpcConfig.getNodes().contains(config)) {
            log.info("KusciaGrpcConfig already exists,unRegisterKuscia config={}", config);
            unRegisterKuscia(config);
        }
        if (isInitialized || dynamicKusciaGrpcConfig.getNodes().add(config)) {
            log.info("Register kuscia node success, config={}", config);
            synchronized (lock) {
                registerChannelFactory(config.getDomainId(), new GrpcKusciaApiChannelFactory(config));
                if (!ObjectUtils.isEmpty(publisher)) {
                    publisher.publishEvent(new RegisterKusciaEvent(this, config));
                }
            }
        }
    }

    public void unRegisterKuscia(KusciaGrpcConfig config) {
        Assert.notNull(config, "KusciaGrpcConfig must not be null");
        config.validateAndProcess();
        dynamicKusciaGrpcConfig.getNodes().remove(config);
        if (isInitialized || CHANNEL_FACTORIES.containsKey(config.getDomainId())) {
            log.info("Unregister kuscia node success, config={}", config);
            synchronized (lock) {
                KusciaApiChannelFactory remove = CHANNEL_FACTORIES.remove(config.getDomainId());
                if (remove != null) {
                    remove.shutdown();
                }
                if (!ObjectUtils.isEmpty(publisher)) {
                    publisher.publishEvent(new UnRegisterKusciaEvent(this, config));
                }
            }
        }
    }

    /**
     * equals may be wrong
     **/
    public void unRegisterKuscia(String domainId) {
        List<KusciaGrpcConfig> configs = dynamicKusciaGrpcConfig.getNodes().stream()
                .filter(node -> StringUtils.equals(node.getDomainId(), domainId))
                .toList();
        if (!CollectionUtils.isEmpty(configs)) {
            configs.forEach(this::unRegisterKuscia);
        }

    }

    private void registerChannelFactory(String name, KusciaApiChannelFactory channelFactory) {
        KusciaApiChannelFactory factory = CHANNEL_FACTORIES.put(name, channelFactory);
        if (factory != null) {
            log.warn("The channel factory {} has been registered, shutdown and replace", name);
            factory.shutdown();
        }
    }

    public <T extends AbstractStub<T>> T createStub(String domainId, Class<T> clazz) {
        checkChannelFactoryExist(domainId);
        String serviceName = getServiceName(clazz.getEnclosingClass());
        AbstractStub<?> t = null;

        switch (serviceName) {
            case DomainServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(DomainServiceGrpc.DomainServiceBlockingStub.class)) {
                    t = DomainServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(DomainServiceGrpc.DomainServiceStub.class)) {
                    t = DomainServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(DomainServiceGrpc.DomainServiceFutureStub.class)) {
                    t = DomainServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            case DomainDataServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(DomainDataServiceGrpc.DomainDataServiceBlockingStub.class)) {
                    t = DomainDataServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(DomainDataServiceGrpc.DomainDataServiceStub.class)) {
                    t = DomainDataServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(DomainDataServiceGrpc.DomainDataServiceFutureStub.class)) {
                    t = DomainDataServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            case DomainRouteServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(DomainRouteServiceGrpc.DomainRouteServiceBlockingStub.class)) {
                    t = DomainRouteServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(DomainRouteServiceGrpc.DomainRouteServiceStub.class)) {
                    t = DomainRouteServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(DomainRouteServiceGrpc.DomainRouteServiceFutureStub.class)) {
                    t = DomainRouteServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }
            case DomainDataSourceServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(DomainDataSourceServiceGrpc.DomainDataSourceServiceBlockingStub.class)) {
                    t = DomainDataSourceServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(DomainDataSourceServiceGrpc.DomainDataSourceServiceStub.class)) {
                    t = DomainDataSourceServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(DomainDataSourceServiceGrpc.DomainDataSourceServiceFutureStub.class)) {

                    t = DomainDataSourceServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }
            case DomainDataGrantServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(DomainDataGrantServiceGrpc.DomainDataGrantServiceBlockingStub.class)) {

                    t = DomainDataGrantServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(DomainDataGrantServiceGrpc.DomainDataGrantServiceStub.class)) {
                    t = DomainDataGrantServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(DomainDataGrantServiceGrpc.DomainDataGrantServiceFutureStub.class)) {
                    t = DomainDataGrantServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            case JobServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(JobServiceGrpc.JobServiceBlockingStub.class)) {
                    t = JobServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(JobServiceGrpc.JobServiceStub.class)) {
                    t = JobServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel());
                }
                if (clazz.equals(JobServiceGrpc.JobServiceFutureStub.class)) {
                    t = JobServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            case ServingServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(ServingServiceGrpc.ServingServiceBlockingStub.class)) {
                    t = ServingServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(ServingServiceGrpc.ServingServiceStub.class)) {
                    t = ServingServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(ServingServiceGrpc.ServingServiceFutureStub.class)) {
                    t = ServingServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            case HealthServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(HealthServiceGrpc.HealthServiceBlockingStub.class)) {
                    t = HealthServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(HealthServiceGrpc.HealthServiceStub.class)) {
                    t = HealthServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(HealthServiceGrpc.HealthServiceFutureStub.class)) {
                    t = HealthServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            case CertificateServiceGrpc.SERVICE_NAME -> {
                if (clazz.equals(CertificateServiceGrpc.CertificateServiceBlockingStub.class)) {
                    t = CertificateServiceGrpc.newBlockingStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(BLOCKING_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
                if (clazz.equals(CertificateServiceGrpc.CertificateServiceStub.class)) {
                    t = CertificateServiceGrpc.newStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(StubSCRIPTION_TIMEOUT_DAY, TimeUnit.DAYS);
                }
                if (clazz.equals(CertificateServiceGrpc.CertificateServiceFutureStub.class)) {
                    t = CertificateServiceGrpc.newFutureStub(CHANNEL_FACTORIES.get(domainId).getChannel())
                            .withDeadlineAfter(FUTURE_TIMEOUT_MILLISECOND, TimeUnit.MILLISECONDS);
                }
            }

            default -> throw new IllegalArgumentException("Unsupported class type: " + clazz.getName());
        }
        return (T) t;
    }

    /**
     * check before invoke or exception appear
     */
    public boolean isChannelExist(String domainId) {
        return CHANNEL_FACTORIES.containsKey(domainId);
    }


    private void checkChannelFactoryExist(String domainId) {
        if (!CHANNEL_FACTORIES.containsKey(domainId)) {
            throw new IllegalArgumentException("No such kuscia instance domain id: " + domainId);
        }
    }

    public String getProtocolByDomainId(String domainId) {
        String protocol = KusciaProtocolEnum.TLS.name().toLowerCase(Locale.ROOT);
        if (CollectionUtils.isEmpty(dynamicKusciaGrpcConfig.getNodes())) {
            return protocol;
        }
        for (KusciaGrpcConfig node : dynamicKusciaGrpcConfig.getNodes()) {
            if (node.getDomainId().equals(domainId)) {
                protocol = node.getProtocol().name().toLowerCase(Locale.ROOT);
            }
        }
        return protocol;
    }

    public void serializableKusciaConfigFileInit() throws IOException, ClassNotFoundException {
        ObjectInputStream in = null;
        KusciaGrpcConfig config;
        File file = ResourceUtils.getFile(kusciaPath);
        if (Files.exists(file.toPath())) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                in = new ObjectInputStream(new FileInputStream(f));
                config = (KusciaGrpcConfig) in.readObject();
                log.info("Load kuscia config by config file, config={}", config);
                registerKuscia(config);
            }
        }
        IOUtils.closeQuietly(in);
    }

}