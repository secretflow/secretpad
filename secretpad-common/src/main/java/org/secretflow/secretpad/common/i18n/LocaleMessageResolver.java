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

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * Locale message resolver
 *
 * @author yansi
 * @date 2023/5/10
 */
public class LocaleMessageResolver implements MessageResolver {
    /**
     * Message source interface
     */
    private final MessageSource messageSource;

    /**
     * Fill local message resolver
     *
     * @param messageSource
     */
    public LocaleMessageResolver(MessageSource messageSource) {
        Assert.notNull(messageSource, "messageSource must not be null");
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(ErrorCode errorCode, Locale locale, String... args) {
        Assert.notNull(errorCode, "errorCode must not be null");
        return getMessage(locale, errorCode.getMessageKey(), args);
    }

    @Override
    public String getMessage(ErrorCode errorCode, String... args) {
        return getMessage(errorCode, LocaleContextHolder.getLocale(), args);
    }

    @Override
    public String getMessage(String key, String... args) {
        return getMessage(LocaleContextHolder.getLocale(), key, args);
    }

    @Override
    public String getMessage(Locale locale, String key, String... args) {
        return messageSource.getMessage(key, args, getSupportedLocale(locale));
    }

    /**
     * Get supported locale
     *
     * @param locale
     * @return Supported locale
     */
    private Locale getSupportedLocale(Locale locale) {
        if (locale == null) {
            locale = LocaleContextHolder.getLocale();
        }
        if (locale == null) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }
        String lang = locale.getLanguage();
        if (Locale.SIMPLIFIED_CHINESE.getLanguage().equals(locale)) {
            return Locale.SIMPLIFIED_CHINESE;
        } else if (Locale.US.getLanguage().equals(lang)) {
            return Locale.US;
        }
        return Locale.SIMPLIFIED_CHINESE;
    }
}
