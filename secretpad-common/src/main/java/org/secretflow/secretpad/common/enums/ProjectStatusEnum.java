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

import lombok.Getter;

/**
 * @author cml
 * @date 2023/11/30
 */
@Getter
public enum ProjectStatusEnum {
    REVIEWING(0),
    APPROVED(1),
    ARCHIVED(2),
    ;
    private final Integer code;


    ProjectStatusEnum(Integer code) {
        this.code = code;
    }

    public static String parse(Integer code) {
        for (ProjectStatusEnum value : ProjectStatusEnum.values()) {
            if (value.code.equals(code)) {
                return value.name();
            }
        }
        return null;
    }
}
