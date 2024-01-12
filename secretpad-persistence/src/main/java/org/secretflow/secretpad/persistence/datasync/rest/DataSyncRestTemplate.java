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

package org.secretflow.secretpad.persistence.datasync.rest;

import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.rest.p2p.P2pDataSyncRestService;
import org.secretflow.secretpad.persistence.datasync.retry.DataSyncRetryTemplate;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;

import jakarta.annotation.Resource;

/**
 * @author yutu
 * @date 2023/12/10
 */
public abstract class DataSyncRestTemplate {
    @Resource
    protected P2pDataSyncRestService p2pDataSyncRestService;
    @Resource
    protected DataSyncRetryTemplate dataSyncRetryTemplate;
    @Resource
    protected DataSyncDataBufferTemplate dataSyncDataBufferTemplate;

    public abstract EntityChangeListener.DbChangeEvent<BaseAggregationRoot> send(String node) throws InterruptedException;

    public abstract void onError(String node, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> syncDataDTO);

    public abstract void onSuccess(String node, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> syncDataDTO);
}