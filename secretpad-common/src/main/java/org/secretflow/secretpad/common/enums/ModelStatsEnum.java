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

package org.secretflow.secretpad.common.enums;

/**
 * @author chenmingliang
 * @date 2024/01/18
 */
public enum ModelStatsEnum {

    INIT(0, "waiting for publish"),
    PUBLISHING(1, "in publishing"),
    PUBLISHED(2, "published"),


    OFFLINE(3, "offline"),
    PUBLISH_FAIL(4, "publish fail"),
    DISCARDED(5, "discarded"),
    ;

    private final Integer code;

    private final String desc;

    ModelStatsEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String parse(Integer code) {
        for (ModelStatsEnum value : ModelStatsEnum.values()) {
            if (value.code.equals(code)) {
                return value.name();
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }
}
