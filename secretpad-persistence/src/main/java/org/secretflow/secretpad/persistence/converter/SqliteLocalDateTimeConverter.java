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

package org.secretflow.secretpad.persistence.converter;

import com.google.common.base.Strings;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interconvert Sqlite between LocalDateTime and String
 *
 * @author yansi
 * @date 2023/5/30
 */
@Converter
public class SqliteLocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Convert Sqlite LocalDateTime to String
     *
     * @param localDateTime LocalDateTime value
     * @return String
     */
    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return SQLITE_DATETIME_FORMATTER.format(localDateTime);
    }

    /**
     * Convert String to Sqlite LocalDateTime
     *
     * @param s String value
     * @return LocalDateTime
     */
    @Override
    public LocalDateTime convertToEntityAttribute(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }
        return LocalDateTime.parse(s, SQLITE_DATETIME_FORMATTER);
    }
}
