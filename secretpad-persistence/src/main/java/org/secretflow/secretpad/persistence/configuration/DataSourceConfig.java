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

package org.secretflow.secretpad.persistence.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author yutu
 * @date 2024/08/23
 */
@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "defaultDataSource")
    @ConfigurationProperties("spring.datasource.default")
    public DataSource defaultDataSource() {
        HikariDataSource hikariDataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        hikariDataSource.setMaximumPoolSize(1);
        hikariDataSource.setMinimumIdle(1);
        hikariDataSource.setConnectionTimeout(20000);
        hikariDataSource.setIdleTimeout(60000);
        return hikariDataSource;
    }

    @Bean(name = "quartzDataSource")
    @ConfigurationProperties("spring.datasource.quartz")
    public DataSource quartzDataSource() {
        HikariDataSource hikariDataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        hikariDataSource.setMaximumPoolSize(100);
        hikariDataSource.setMinimumIdle(10);
        hikariDataSource.setConnectionTimeout(20000);
        hikariDataSource.setIdleTimeout(60000);
        return hikariDataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("defaultDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public JdbcTemplate quartzJdbcTemplate(@Qualifier("quartzDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
