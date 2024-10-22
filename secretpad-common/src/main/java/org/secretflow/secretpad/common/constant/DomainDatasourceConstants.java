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

package org.secretflow.secretpad.common.constant;


/**
 * @author chenmingliang
 * @date 2024/05/28
 */
public class DomainDatasourceConstants {
    public final static String DEFAULT_HTTP_DATASOURCE_ID = "http-data-source";
    public final static String DEFAULT_HTTP_DATASOURCE_NAME = "http-data-source";
    public final static String DEFAULT_LOCAL_DATASOURCE_TYPE = "LOCAL";
    public final static String DEFAULT_DATASOURCE = "default-data-source";
    public final static String DEFAULT_DATASOURCE_TYPE = "localfs";
    public static final String DEFAULT_OSS_DATASOURCE_TYPE = "OSS";
    public static final String DEFAULT_ODPS_DATASOURCE_TYPE = "ODPS";
    public static final String DEFAULT_MYSQL_DATASOURCE_TYPE = "MYSQL";
    public static final String DEFAULT_HTTP_DATASOURCE_TYPE = "HTTP";

    public static final String ODPS_DATASOURCE_PARTITION_TYPE_ODPS = "odps";
    public static final String ODPS_DATASOURCE_PARTITION_TYPE_PATH = "path";

    public final static String DATASOURCE_TYPE = "DatasourceType";
    public final static String DATASOURCE_NAME = "DatasourceName";

    public final static String DATASOURCE_ID_PREFIX = "oss-";
    public final static String DATASOURCE_ODPS_ID_PREFIX = "odps-";
    public final static String DATASOURCE_MYSQL_ID_PREFIX = "mysql-";
}
