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

package org.secretflow.secretpad.persistence.datasync.buffer.center;

import org.secretflow.secretpad.common.util.UniqueLinkedBlockingQueue;
import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;

import java.io.IOException;

/**
 * @author yutu
 * @date 2023/12/10
 */
public class CenterDataSyncDataBufferTemplate extends DataSyncDataBufferTemplate {
    private static final UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent> QUEUE = new UniqueLinkedBlockingQueue<>();

    /**
     * push data at end of buffer
     *
     * @param event
     */
    @Override
    public void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {
        QUEUE.add(event);
    }

    /**
     * pop data at first of buffer
     */
    @Override
    public EntityChangeListener.DbChangeEvent<BaseAggregationRoot> peek(String nodeId) throws InterruptedException {
        return QUEUE.take();
    }

    /**
     * pop data at first of buffer
     *
     * @param nodeId
     */
    @Override
    public EntityChangeListener.DbChangeEvent<BaseAggregationRoot> poll(String nodeId) throws InterruptedException {
        return QUEUE.poll();
    }

    @Override
    public int size(String nodeId) {
        return QUEUE.size();
    }

    @Override
    public void commit(String nodeId, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event) {

    }

    @Override
    public void endurance(String nodeId) throws IOException {

    }
}