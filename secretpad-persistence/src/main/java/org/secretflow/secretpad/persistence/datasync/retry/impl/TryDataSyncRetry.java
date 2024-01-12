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

package org.secretflow.secretpad.persistence.datasync.retry.impl;

import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.datasync.retry.DataSyncRetryTemplate;
import org.secretflow.secretpad.persistence.entity.BaseAggregationRoot;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yutu
 * @date 2023/12/14
 */
@Slf4j
public class TryDataSyncRetry extends DataSyncRetryTemplate {
    private static final int RETRY_SIZE = 3;

    @Override
    public int retry(String node, EntityChangeListener.DbChangeEvent<BaseAggregationRoot> syncDataDTO) {
        log.info("********---------- retry try {}, for data {}  - {}", RETRY_SIZE, node, syncDataDTO);
        return RETRY_SIZE;
    }
}