/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author yutu
 * @date 2024/02/25
 */
@Slf4j
class UniqueLinkedBlockingQueueTest {

    @Test
    void test() throws InterruptedException {
        UniqueLinkedBlockingQueue<String> queue = new UniqueLinkedBlockingQueue<>();
        queue.put("alice");
        queue.put("bob");
        queue.put("alice");
        log.info(queue.toString());
        assert queue.size() == 2;
    }
}