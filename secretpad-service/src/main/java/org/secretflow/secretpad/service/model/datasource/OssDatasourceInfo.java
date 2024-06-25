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

package org.secretflow.secretpad.service.model.datasource;

import org.secretflow.secretpad.service.constant.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenmingliang
 * @date 2024/05/24
 */
@Getter
@Setter
public class OssDatasourceInfo extends DataSourceInfo {

    @NotBlank
    @Pattern(regexp = Constants.DOMAIN_PATTEN, message = "The endpoint is invalid, it must be a standard top-level domain or IP address + port, such as 'https://127.0.0.1:8888")
    private String endpoint;

    @NotBlank
    @Pattern(regexp = Constants.BUCKET_PATTEN, message = "The bucket name invalid")
    private String bucket;

    private String prefix;
    @NotBlank(message = "Access key cannot be empty")

    private String ak;
    @NotBlank(message = "Secret key cannot be empty")
    private String sk;

    private String storageType;

    private Boolean virtualhost;

    public static OssDatasourceInfo create(String endpoint, String bucket, String prefix, String ak, String sk, String storageType, Boolean virtualhost) {
        OssDatasourceInfo ossDatasourceInfo = new OssDatasourceInfo();
        ossDatasourceInfo.setEndpoint(endpoint);
        ossDatasourceInfo.setBucket(bucket);
        ossDatasourceInfo.setPrefix(prefix);
        ossDatasourceInfo.setAk(ak);
        ossDatasourceInfo.setSk(sk);
        ossDatasourceInfo.setVirtualhost(virtualhost);
        ossDatasourceInfo.setStorageType(storageType);
        return ossDatasourceInfo;
    }
}
