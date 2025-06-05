package org.secretflow.secretpad.service.model.datasource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.secretflow.secretpad.manager.integration.datasource.tdsql.TdsqlConfig;
import org.secretflow.secretpad.service.constant.Constants;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TdsqlDatasourceInfo extends DataSourceInfo{
    @NotBlank
    @Pattern(regexp = Constants.MYSQL_ENDPOINT_PATTEN, message = "The endpoint is invalid, it must be a standard top-level domain or IP address + port, such as '127.0.0.1:8888")
    private String endpoint;
    @NotBlank(message = "tdsql user cannot be null or empty")
    private String user;
    @NotBlank(message = "tdsql password cannot be null or empty")
    private String password;
    @NotBlank(message = "tdsql database cannot be null or empty")
    private String database;
    public TdsqlConfig toTdsqlConfig() {
        return TdsqlConfig.builder()
                .endpoint(endpoint)
                .user(user)
                .password(password)
                .database(database)
                .build();
    }
}
