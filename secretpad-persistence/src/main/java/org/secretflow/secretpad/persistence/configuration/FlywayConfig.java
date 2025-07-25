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


import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author yutu
 * @date 2024/08/23
 */
@Configuration
public class FlywayConfig {

    @Bean
    @ConfigurationProperties(prefix = "flyway.default")
    public FlywayProperties defaultFlywayProperties() {
        return new FlywayProperties();
    }

    @Bean
    public Flyway defaultFlyway(@Qualifier("defaultDataSource") DataSource dataSource) {
        FlywayProperties flywayProperties = defaultFlywayProperties();
        int size = flywayProperties.getLocations().size();
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(flywayProperties.getLocations().toArray(new String[size]))
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean
    @ConfigurationProperties(prefix = "flyway.quartz")
    public FlywayProperties quartzFlywayProperties() {
        return new FlywayProperties();
    }

    @Bean
    public Flyway quartzFlyway(@Qualifier("quartzDataSource") DataSource dataSource) {
        FlywayProperties flywayProperties = quartzFlywayProperties();
        int size = flywayProperties.getLocations().size();
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(flywayProperties.getLocations().toArray(new String[size]))
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}
