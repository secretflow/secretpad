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

package org.secretflow.secretpad.persistence.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Configuration for persistence layer
 *
 * @author yansi
 * @date 2023/5/9
 */
@Slf4j
@EntityScan(basePackages = "org.secretflow.secretpad.persistence.*")
@EnableJpaRepositories(basePackages = {"org.secretflow.secretpad.persistence.*"})
@Configuration
public class PersistenceConfiguration {

    @Bean
    public DataSourceInitializer dataSourceInitializer(@Qualifier("defaultDataSource") DataSource dataSource) {
        log.info("making sure database is WAL mode");
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator((Connection connection) -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=WAL;");
            }
        });
        return initializer;
    }

}
