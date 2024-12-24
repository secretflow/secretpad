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

package org.secretflow.secretpad.service.listener;

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.kuscia.v1alpha1.event.RegisterKusciaEvent;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;
import org.secretflow.secretpad.manager.integration.job.AbstractJobManager;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;
import org.secretflow.secretpad.service.dataproxy.DataProxyService;

import jakarta.annotation.Resource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

/**
 * @author yutu
 * @date 2024/07/16
 */
@Async
@Slf4j
@Component
public class KusciaRegisterListener implements ApplicationListener<RegisterKusciaEvent> {

    @Setter
    @Value("${secretpad.kuscia-path:./config/kuscia/}")
    private String kusciaPath;

    @Value("${secretpad.platform-type}")
    private String plaformType;

    @Setter
    @Value("${secretpad.node-id}")
    private String nodeId;

    private PlatformTypeEnum getPlaformType() {
        return PlatformTypeEnum.valueOf(plaformType);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Lazy
    @Resource
    private DataProxyService dataProxyService;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(RegisterKusciaEvent event) {
        KusciaGrpcConfig config = event.getConfig();
        log.info("KusciaRegisterListener: {}", config);
        AbstractJobManager jobManager = applicationContext.getBean("jobManager", AbstractJobManager.class);
        switch (getPlaformType()) {
            case CENTER, EDGE:
                if (config.getDomainId().equals(nodeId)) {
                    jobManager.startSync(config.getDomainId());
                }
                break;
            case AUTONOMY:
                P2pDataSyncProducerTemplate.nodeIds.add(config.getDomainId());
                jobManager.startSync(config.getDomainId());
                // For newly registered nodes, modify the data source to dp proxy
                dataProxyService.updateDataSourceUseDataProxyByDomainId(config.getDomainId(), config.getDomainId());
                break;
        }
        serializableWrite(config);
    }


    public void serializableWrite(KusciaGrpcConfig config) {
        ObjectOutputStream os = null;
        try {
            File file = ResourceUtils.getFile(kusciaPath + config.getDomainId());
            if (!Files.exists(file.toPath().getParent())) {
                Files.createDirectories(file.toPath().getParent());
            }
            if (!Files.exists(file.toPath())) {
                Files.createFile(file.toPath());
            }
            os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(config);
        } catch (Exception e) {
            log.error("KusciaRegisterListener serializableWrite error: {}", e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }
}