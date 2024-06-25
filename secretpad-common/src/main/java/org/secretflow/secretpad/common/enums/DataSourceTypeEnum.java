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

package org.secretflow.secretpad.common.enums;

import java.util.Locale;

/**
 * @author chenmingliang
 * @date 2024/02/02
 */
public enum DataSourceTypeEnum {
    HTTP(false),
    OSS(true),
    LOCAL(true),
    MYSQL(true),
    ;

    /**
     * true : the datasource is manager by kuscia
     * false: the datasource is manager by secretpad or other
     */
    private final boolean isKusciaControl;

    DataSourceTypeEnum(boolean isKusciaControl) {
        this.isKusciaControl = isKusciaControl;
    }

    public boolean isKusciaControl() {
        return isKusciaControl;
    }

    public static DataSourceTypeEnum fromString(String str) {
        return switch (str.toLowerCase(Locale.ROOT)) {
            case "localfs" -> LOCAL;
            case "oss" -> OSS;
            case "mysql" -> MYSQL;
            default -> throw new IllegalArgumentException("Invalidate DataSource type: " + str);
        };
    }


    public static String kuscia2platform(String str) {
        return fromString(str).name();
    }
}
