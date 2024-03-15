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

package org.secretflow.secretpad.persistence.datasync.producer.center;

import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.producer.AbstractDataSyncProducerTemplate;
import org.secretflow.secretpad.persistence.datasync.producer.PaddingNodeService;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.entity.ProjectJobDO;
import org.secretflow.secretpad.persistence.entity.ProjectNodesInfo;
import org.secretflow.secretpad.persistence.entity.ProjectTaskDO;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;
import org.secretflow.secretpad.persistence.model.DbChangeAction;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
public class CenterDataSyncProducerTemplate extends AbstractDataSyncProducerTemplate {

    public CenterDataSyncProducerTemplate(DataSyncConfig dataSyncConfig, DataSyncDataBufferTemplate dataSyncDataBufferTemplate, PaddingNodeService p2pPaddingNodeServiceImpl) {
        super(dataSyncConfig, dataSyncDataBufferTemplate, p2pPaddingNodeServiceImpl);
    }


    @Override
    public boolean filter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        log.debug("CenterDataSyncProducerTemplate push filter {}", event);
        List<String> sync = dataSyncConfig.getSync();
        String dType = event.getDType();
        if (!sync.contains(dType)) {
            return true;
        }
        ProjectNodesInfo source = event.getSource();
        if (source instanceof ProjectTaskDO || source instanceof ProjectJobDO) {
            return DbChangeAction.UPDATE.getVal().equals(event.getAction());
        }
        return false;
    }

    @Override
    public void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        if (PlatformTypeEnum.valueOf(platformType).equals(PlatformTypeEnum.CENTER) && !filter(event)) {
            log.debug("CenterDataSyncProducerTemplate push {}", event);
            dataSyncDataBufferTemplate.push(event);
        }
    }

    @Override
    public void pushIgnoreFilter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {

    }
}