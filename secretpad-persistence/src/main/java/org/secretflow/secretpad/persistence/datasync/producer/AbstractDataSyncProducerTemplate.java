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

package org.secretflow.secretpad.persistence.datasync.producer;

import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;
import org.secretflow.secretpad.persistence.model.DataSyncConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * DataSyncProducerTemplate
 *
 * @author yutu
 * @date 2023/12/06
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDataSyncProducerTemplate {
    @Value("${secretpad.platform-type}")
    public String platformType;
    public final DataSyncConfig dataSyncConfig;
    public final DataSyncDataBufferTemplate dataSyncDataBufferTemplate;
    public final PaddingNodeService p2pPaddingNodeServiceImpl;

    /**
     * if return ture ignore db event to sync data to remote storage
     *
     * @param event db change event
     * @return if return true ignore db event to sync data to remote storage
     */
    public abstract boolean filter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event);

    /**
     * push db change event to queue
     *
     * @param event db change event
     */
    public abstract void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event);

    /**
     * push db change event to queue which not filter
     *
     * @param event db change event
     */
    public abstract void pushIgnoreFilter(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event);
}