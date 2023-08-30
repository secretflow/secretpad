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

package org.secretflow.secretpad.service.graph;

import org.secretflow.secretpad.service.graph.chain.AbstractJobHandler;

import java.util.List;

/**
 * Job chain to deal job operation
 *
 * @author yansi
 * @date 2023/6/1
 */
public class JobChain<T> {
    private final AbstractJobHandler handler;

    /**
     * Job chain for all job handlers
     *
     * @param jobHandlers all job handlers
     * @return job chain
     */
    public JobChain(List<AbstractJobHandler> jobHandlers) {
        AbstractJobHandler.Builder builder = new AbstractJobHandler.Builder();
        jobHandlers.forEach(builder::addHandler);
        handler = builder.build();
    }

    public void proceed(T object) {
        handler.doHandler(object);
    }
}
