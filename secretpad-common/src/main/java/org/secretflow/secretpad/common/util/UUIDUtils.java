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

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Locale;

/**
 * UUID utils
 *
 * @author jiezi
 * @date 2023/06/27
 */
public final class UUIDUtils {

    private UUIDUtils() {
    }

    /**
     * Get new uuid string
     *
     * @return uuid string
     */
    public static String newUUID() {
        String s = java.util.UUID.randomUUID().toString();
        return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
    }

    /**
     * Random target count
     *
     * @param count target count
     * @return random string
     */
    public static String random(int count) {
        return RandomStringUtils.random(count, true, false).toLowerCase(Locale.ENGLISH);
    }
}
