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

package org.secretflow.secretpad.service.graph.chain;

import org.springframework.core.Ordered;

/**
 * Abstract job handler to deal job params in order
 *
 * @author yansi
 * @date 2023/5/30
 */
public abstract class AbstractJobHandler<T> implements Ordered {
    protected AbstractJobHandler<T> next = null;

    /**
     * Deal job handler method
     *
     * @param job target job
     */
    public abstract void doHandler(T job);

    public void next(AbstractJobHandler<T> handler) {
        this.next = handler;
    }

    /**
     * Abstract job handler builder
     *
     * @param <T>
     */
    public static class Builder<T> {
        private AbstractJobHandler<T> head;
        private AbstractJobHandler<T> tail;

        /**
         * Add job handler via build head and tail
         *
         * @param handler
         * @return
         */
        public Builder<T> addHandler(AbstractJobHandler handler) {
            if (this.head == null) {
                this.head = handler;
            } else {
                this.tail.next = handler;
            }
            this.tail = handler;
            return this;
        }

        public AbstractJobHandler build() {
            return this.head;
        }
    }
}
