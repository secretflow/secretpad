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

/**
 * Interconvert between Boolean and Integer
 *
 * @author yansi
 * @date 2023/5/9
 */
@Converter
public class Boolean2IntConverter implements AttributeConverter<Boolean, Integer> {

    /**
     * Convert Boolean to Integer
     *
     * @param attribute Boolean value
     * @return Integer
     */
    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null || !attribute) {
            return 0;
        }
        return 1;
    }

    /**
     * Convert Integer to Boolean
     *
     * @param dbData Integer value
     * @return Boolean
     */
    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        return dbData != null && dbData != 0;
    }
}
