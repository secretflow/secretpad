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

package org.secretflow.secretpad.common.i18n;

import org.secretflow.secretpad.common.errorcode.ErrorCode;

import java.util.Locale;

/**
 * Message resolver interface
 *
 * @author yansi
 * @date 2023/5/10
 */
public interface MessageResolver {
    /**
     * Get message with current configuration language via error code, locale and args.
     *
     * @param errorCode
     * @param locale
     * @param args
     * @return Error message
     */
    String getMessage(ErrorCode errorCode, Locale locale, String... args);

    /**
     * Get message with current configuration language via error code and args
     *
     * @param errorCode
     * @param args
     * @return Error message
     */
    String getMessage(ErrorCode errorCode, String... args);

    /**
     * Get message with current configuration language via key and args
     *
     * @param key
     * @param args
     * @return Error message
     */
    String getMessage(String key, String... args);

    /**
     * Get message with current configuration language via locale, key and args
     *
     * @param locale
     * @param key
     * @param args
     * @return Error message
     */
    String getMessage(Locale locale, String key, String... args);
}
