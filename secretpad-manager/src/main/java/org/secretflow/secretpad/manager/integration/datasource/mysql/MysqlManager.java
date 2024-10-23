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

package org.secretflow.secretpad.manager.integration.datasource.mysql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;

/**
 * @author lufeng
 * @date 2024/8/20
 */
@Slf4j
@Service
public class MysqlManager {
    private final static String MYSQL_TEST_QUERY = "SELECT 1;";
    private final static String MYSQL_TEST_TABLE = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?;";

    public boolean testConnection(MysqlConfig mysqlConfig) {
        try (Connection connection = MysqlFactory.buildMySQLClient(mysqlConfig)) {
            Statement statement = connection.createStatement();
            return statement.execute(MYSQL_TEST_QUERY); // Connection is successful if we can execute the query
        } catch (SQLException e) {
            log.error("Test MySQL connection failed, error: {}", e.getMessage(), e);
            throw new RuntimeException("MySQL test connection failed: " + e.getMessage(), e);
        }
    }

    public boolean testTableExists(MysqlConfig mysqlConfig, String databaseName, String tableName) {
        try (Connection connection = MysqlFactory.buildMySQLClient(mysqlConfig)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(MYSQL_TEST_TABLE)) {
                preparedStatement.setString(1, databaseName);
                preparedStatement.setString(2, tableName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Table check failed, error: {}", e.getMessage(), e);
            return false;
        }
        return false;
    }

}
