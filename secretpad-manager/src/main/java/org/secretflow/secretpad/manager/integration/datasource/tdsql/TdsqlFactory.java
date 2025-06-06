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

package org.secretflow.secretpad.manager.integration.datasource.tdsql;

import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author lufeng
 * @date 2024/8/20
 */
public final class TdsqlFactory {

    private TdsqlFactory() {
    }

    public static Connection buildTdSQLClient(TdsqlConfig tdsqlConfig) {
        Assert.notNull(tdsqlConfig, "tdsqlConfig must not be null");
        tdsqlConfig.validate();
        String url = "jdbc:mysql://" + tdsqlConfig.getEndpoint() + "/" + tdsqlConfig.getDatabase();
        Properties properties = new Properties();
        properties.setProperty("allowLoadLocalInfile", "false");
        properties.setProperty("allowUrlInLocalInfile", "false");
        properties.setProperty("allowLoadLocalInfileInPath", "");
        properties.setProperty("autoDeserialize", "false");

        properties.setProperty("user", tdsqlConfig.getUser());
        properties.setProperty("password", tdsqlConfig.getPassword());
        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Failed to connect to TdSQl: " + e.getMessage(), e);
        }
    }
}
