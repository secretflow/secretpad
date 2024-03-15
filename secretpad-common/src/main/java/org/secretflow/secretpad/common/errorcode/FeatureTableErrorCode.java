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

package org.secretflow.secretpad.common.errorcode;

/**
 * @author chenmingliang
 * @date 2024/01/26
 */
public enum FeatureTableErrorCode implements ErrorCode {
    FEATURE_TABLE_NOT_EXIST(202012301),
    FEATURE_TABLE_IP_FILTER(202012302),

    FEATURE_TABLE_IP_NOT_KNOWN(202012303),
    ;


    private final int code;

    FeatureTableErrorCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return "featuretable." + this.name();
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
