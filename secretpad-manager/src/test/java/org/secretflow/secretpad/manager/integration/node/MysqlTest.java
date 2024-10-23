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

import org.secretflow.secretpad.manager.integration.datasource.mysql.MysqlConfig;
import org.secretflow.secretpad.manager.integration.datasource.mysql.MysqlManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author lufeng
 * @date 2024/9/3
 */
public class MysqlTest {

    @Test
    void testMysqlBuild(){
        MysqlManager mysqlManager = new MysqlManager();
        Assertions.assertThrows(IllegalArgumentException.class, () -> mysqlManager.testConnection(null));
        MysqlConfig mysqlConfig = MysqlConfig.builder()
                .endpoint("localhost:3306")
                .user("root")
                .password("123456")
                .database("test")
                .build();
        Assertions.assertThrows(IllegalArgumentException.class, () -> mysqlManager.testConnection(mysqlConfig));
    }

}
