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

package org.secretflow.secretpad.service.constant;

/**
 * Constants
 *
 * @author : xiaonan.fhn
 * @date 2023/5/15
 */
public class Constants {
    public static final String NODE_NAME_PATTERN = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\_-]{1,32}$";
    public static final String IP_PORT_PATTERN =
            "^.{1,50}:([0-9]|[1-9]\\d|[1-9]\\d{2}|[1-9]\\d{3}|[1-5]\\d{4}|6[0-4]\\d{3}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5])$";

    public static final String TEE_PROJECT_MODE = "tee";

    public static final String HTTP_PREFIX_REG = "^http://.*";

    public static final String IP_PREFIX_REG = "^(http|https)://.*";

    public static final String MYSQL_ENDPOINT_PATTEN = "^(?:\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b|localhost):(\\d{1,5})$";


    /**
     * domain patten:
     * https://xxx.xxx.cn-xxx.com  or http://127.0.0.1:9000
     */
    public static final String DOMAIN_PATTEN = "(http://|https://)" +
            "((([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}|localhost)" +
            "|((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
            "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)))" +
            "(:[1-9][0-9]{0,3}|:[1-5][0-9]{4}|:6[0-4][0-9]{3}|:65[0-4][0-9]{2}|:655[0-2][0-9]|:6553[0-5])?" +
            "/?";

    public static final String BUCKET_PATTEN = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$";

    public static final String DEFAULT_REGION = "us-east-1";

    public static final String SCQL_INVALID_VALUE = "NULL";

    public static final String SCQL_REPORT = "scql_report";

}
