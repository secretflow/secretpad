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

package org.secretflow.secretpad.persistence.datasync;

import org.secretflow.secretpad.persistence.datasync.buffer.DataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.buffer.center.CenterDataSyncDataBufferTemplate;
import org.secretflow.secretpad.persistence.datasync.listener.EntityChangeListener;
import org.secretflow.secretpad.persistence.entity.ProjectDO;
import org.secretflow.secretpad.persistence.model.DbChangeAction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author yutu
 * @date 2024/02/22
 */
class CenterDataSyncDataBufferTemplateTest {

    private final DataSyncDataBufferTemplate dataSyncDataBufferTemplate = new CenterDataSyncDataBufferTemplate();


    @Test
    void pushByNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> EntityChangeListener.DbChangeEvent.of(DbChangeAction.CREATE, null));
    }

    @Test
    void push() {
        dataSyncDataBufferTemplate.push(EntityChangeListener.DbChangeEvent.of(DbChangeAction.CREATE, buildProjectDO()));
        Assertions.assertEquals(1, dataSyncDataBufferTemplate.size(null));
    }

    @Test
    void peek() throws InterruptedException {
        dataSyncDataBufferTemplate.push(EntityChangeListener.DbChangeEvent.of(DbChangeAction.CREATE, buildProjectDO()));
        Assertions.assertEquals(1, dataSyncDataBufferTemplate.size(null));
        dataSyncDataBufferTemplate.peek(null);
        Assertions.assertEquals(0, dataSyncDataBufferTemplate.size(null));
    }

    @Test
    void noThing() throws IOException {
        dataSyncDataBufferTemplate.commit(null, null);
        dataSyncDataBufferTemplate.endurance(null);
    }

    private ProjectDO buildProjectDO() {
        return ProjectDO.builder().projectId("test").name("test").description("test").build();
    }
}