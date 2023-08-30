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

import org.secretflow.secretpad.common.util.JsonUtils;

import com.google.common.base.Strings;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Interconvert between Object and json string
 *
 * @author jiezi
 * @date 2023/5/9
 */
@Converter
public class BaseObjectJsonConverter<T> implements AttributeConverter<T, String> {

    public Class<T> objectClass;

    public BaseObjectJsonConverter(Class<T> tClass) {
        this.objectClass = tClass;
    }

    public static <T> BaseObjectJsonConverter<T> newConverter(Class<T> tClass) {
        return new BaseObjectJsonConverter<T>(tClass);
    }

    /**
     * Convert target class to json string
     *
     * @param t target class
     * @return json string
     */
    @Override
    public String convertToDatabaseColumn(T t) {
        return null == t ? "{}" : JsonUtils.toJSONString(t);
    }

    /**
     * Convert json string to target class
     *
     * @param s json string
     * @return target class
     */
    @Override
    public T convertToEntityAttribute(String s) {
        return Strings.isNullOrEmpty(s) ? null : JsonUtils.toJavaObject(s, objectClass);
    }

}
