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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.service.DispatchService;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.VoteSyncService;
import org.secretflow.secretpad.service.model.datasync.vote.VoteSyncRequest;
import org.secretflow.secretpad.service.util.DbSyncUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cml
 * @date 2023/11/28
 */
@Service
public class MessageDispatchService implements DispatchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatchService.class);

    private final VoteSyncService voteSyncService;

    private final EnvService envService;

    public MessageDispatchService(VoteSyncService voteSyncService, EnvService envService) {
        this.voteSyncService = voteSyncService;
        this.envService = envService;
    }

    @Override
    public void dispatch(String source, List<String> destinations, VoteSyncRequest voteSyncRequest) {
        for (String destination : destinations) {
            try {
                if (inSameDomain(source, destination)) {
                    voteSyncService.sync(voteSyncRequest.getDbSyncRequests());
                } else {
                    if (envService.isEmbeddedNode(destination)) {
                        DbSyncUtil.dbDataSyncToCenter(voteSyncRequest);
                        continue;
                    }
                    DbSyncUtil.dbDataSyncToNode(voteSyncRequest, destination);
                }
            } catch (Exception e) {
                LOGGER.error("dispatch to node -> {} error,e ={}", destination, e);
            }

        }
    }

    private boolean inSameDomain(String source, String target) {
        return envService.isEmbeddedNode(source) && envService.isEmbeddedNode(target);
    }
}
