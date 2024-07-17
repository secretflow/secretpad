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

package org.secretflow.secretpad.manager.integration.node;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author beiwei
 * @date 2023/9/11
 */
class NodeManagerTest {

    @Test
    void genDomainId() {
        NodeManager nodeManager = new NodeManager(null, null,
                null, null, null,
                null, null, null,
                null);
        String s = nodeManager.genDomainId();
        Assertions.assertThat(s).hasSize(8);
    }
}