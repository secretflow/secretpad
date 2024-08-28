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

import org.secretflow.secretpad.common.enums.DataSourceTypeEnum;

/**
 * @author lufeng
 * @date 2024/6/4
 */
public class DomainDataConstants {
    public final static String DEFAULT_LOCAL_DATASOURCE_TYPE = DataSourceTypeEnum.LOCAL.name();
    public final static String DEFAULT_LOCAL_DATASOURCE_NAME = "default-data-source";

    public final static String DEFAULT_DATATABLE_TYPE = "CSV";

    public static final String HTTP_DATATABLE_TYPE = DomainDatasourceConstants.DEFAULT_HTTP_DATASOURCE_TYPE;

    public static final String NULL_STRS = "NullStrs";
}
