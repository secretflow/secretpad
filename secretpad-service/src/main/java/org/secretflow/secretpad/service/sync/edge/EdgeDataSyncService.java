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

package org.secretflow.secretpad.service.sync.edge;

import org.secretflow.secretpad.common.dto.SyncDataDTO;

import java.util.List;

/**
 * @author yutu
 * @date 2023/10/23
 */
public interface EdgeDataSyncService {
    /**
     * sse client start
     */
    void start();

    /**
     * sse client close
     */
    void close();

    /**
     * sse client start log do lastUpdateTime by db
     *
     * @return db log info
     */
    @SuppressWarnings(value = {"rawtypes"})
    List<SyncDataDTO> log();
}