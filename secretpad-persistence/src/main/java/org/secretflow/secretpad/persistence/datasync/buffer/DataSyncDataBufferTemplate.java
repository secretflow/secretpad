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

package org.secretflow.secretpad.persistence.datasync.buffer;

import org.secretflow.secretpad.common.util.UniqueLinkedBlockingQueue;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yutu
 * @date 2023/12/10
 */
public abstract class DataSyncDataBufferTemplate {

    protected static ConcurrentHashMap<String, UniqueLinkedBlockingQueue<EntityChangeListener.DbChangeEvent<BaseAggregationRoot>>>
            QUEUE_MAP = new ConcurrentHashMap<>(16);

    /**
     * push data at end of buffer
     */
    public abstract void push(EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event);

    /**
     * pop data at first of buffer
     */
    public abstract EntityChangeListener.DbChangeEvent<BaseAggregationRoot> peek(String nodeId) throws InterruptedException;


    /**
     * pop data at first of buffer
     */
    public abstract EntityChangeListener.DbChangeEvent<BaseAggregationRoot> poll(String nodeId) throws InterruptedException;

    public abstract int size(String nodeId);

    public abstract void commit(String nodeId, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> event);

    public abstract void endurance(String nodeId) throws IOException;

}