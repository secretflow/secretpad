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

import org.secretflow.secretpad.kuscia.v1alpha1.event.UnRegisterKusciaEvent;
import org.secretflow.secretpad.kuscia.v1alpha1.model.KusciaGrpcConfig;
import org.secretflow.secretpad.persistence.datasync.producer.p2p.P2pDataSyncProducerTemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author yutu
 * @date 2024/07/16
 */
@Slf4j
@Component
public class KusciaUnRegisterListener implements ApplicationListener<UnRegisterKusciaEvent> {

    @Value("${secretpad.kuscia-path:./config/kuscia/}")
    private String kusciaPath;


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(UnRegisterKusciaEvent event) {
        KusciaGrpcConfig config = event.getConfig();
        log.info("KusciaUnRegisterListener: {}", config);
        P2pDataSyncProducerTemplate.nodeIds.remove(config.getDomainId());
        try {
            delSerializableFile(config);
        } catch (IOException e) {
            log.error("KusciaUnRegisterListener serializableWrite error: {}", e.getMessage(), e);
        }
    }

    public void delSerializableFile(KusciaGrpcConfig config) throws IOException {
        File file = ResourceUtils.getFile(kusciaPath + config.getDomainId());
        if (Files.exists(file.toPath())) {
            Files.delete(file.toPath());
        }
    }
}