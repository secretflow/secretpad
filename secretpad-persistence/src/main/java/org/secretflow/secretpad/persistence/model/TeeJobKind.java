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

package org.secretflow.secretpad.persistence.model;

import lombok.Getter;

/**
 * Tee job kind enum
 *
 * @author yansi
 * @date 2023/5/30
 */
@Getter
public enum TeeJobKind {
    /**
     * Get authorization to push data to tee node
     */
    PushAuth("pushAuth"),
    /**
     * Push data to tee node
     */
    Push("push"),
    /**
     * Authorize data to tee project
     */
    Auth("auth"),
    /**
     * CancelAuth data from tee project
     */
    CancelAuth("cancelAuth"),
    /**
     * Pull data from tee node
     */
    Pull("pull"),
    /**
     * Delete data from tee node
     */
    Delete("delete");

    private final String name;

    TeeJobKind(String name) {
        this.name = name;
    }

}
