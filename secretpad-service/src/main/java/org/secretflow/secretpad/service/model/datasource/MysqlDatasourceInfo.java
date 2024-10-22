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

package org.secretflow.secretpad.service.model.datasource;

import org.secretflow.secretpad.manager.integration.datasource.mysql.MysqlConfig;
import org.secretflow.secretpad.service.constant.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

/**
 * @author lufeng
 * @date 2024/9/3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class MysqlDatasourceInfo extends DataSourceInfo{
    @NotBlank
    @Pattern(regexp = Constants.MYSQL_ENDPOINT_PATTEN, message = "The endpoint is invalid, it must be a standard top-level domain or IP address + port, such as '127.0.0.1:8888")
    private String endpoint;
    @NotBlank(message = "mysql user cannot be null or empty")
    private String user;
    @NotBlank(message = "mysql password cannot be null or empty")
    private String password;
    @NotBlank(message = "mysql database cannot be null or empty")
    private String database;
    public MysqlConfig toMysqlConfig() {
        return MysqlConfig.builder()
                .endpoint(endpoint)
                .user(user)
                .password(password)
                .database(database)
                .build();
    }
}