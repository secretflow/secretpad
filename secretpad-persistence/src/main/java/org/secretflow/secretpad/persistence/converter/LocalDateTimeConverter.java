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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Interconvert between LocalDateTime and String
 *
 * @author yansi
 * @date 2023/5/9
 */
@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    /**
     * Convert LocalDateTime to String
     *
     * @param attribute LocalDateTime value
     * @return String
     */
    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return Timestamp.valueOf(attribute).toString();
    }

    /**
     * Convert String to LocalDateTime
     *
     * @param dbData String value
     * @return LocalDateTime
     */
    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        Timestamp timestamp = Timestamp.valueOf(dbData);
        return timestamp.toLocalDateTime();
    }
}
