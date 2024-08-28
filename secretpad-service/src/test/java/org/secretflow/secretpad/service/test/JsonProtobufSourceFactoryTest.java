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

package org.secretflow.secretpad.service.test;

import org.secretflow.secretpad.service.factory.JsonProtobufSourceFactory;

import com.secretflow.spec.v1.CompListDef;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author yutu
 * @date 2024/08/05
 */
@Slf4j
public class JsonProtobufSourceFactoryTest {

    @Test
    void testJsonProtobufSourceFactory() throws IOException {
        String componentLocation = "../config/components";
        JsonProtobufSourceFactory jsonProtobufSourceFactory = new JsonProtobufSourceFactory(new String[]{componentLocation});
        List<CompListDef> load = jsonProtobufSourceFactory.load();
        log.info("load:{}", load);
    }
}