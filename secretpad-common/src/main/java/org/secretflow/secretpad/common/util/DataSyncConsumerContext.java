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

package org.secretflow.secretpad.common.util;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yutu
 * @date 2023/08/09
 */
@Setter
@Getter
@ToString
public final class DataSyncConsumerContext {
    private static final ThreadLocal<String> content = new ThreadLocal<>();
    private static final String SYNC_CONSUMER = "sync";

    private DataSyncConsumerContext() {
    }


    public static boolean sync() {
        String consumer = content.get();
        return StringUtils.isNotEmpty(consumer) && StringUtils.equals(consumer, SYNC_CONSUMER);
    }

    public static void setConsumerSync() {
        content.set(SYNC_CONSUMER);
    }

    public static void remove() {
        content.remove();
    }
}