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

import java.util.Set;

/**
 * Interconvert between Object and json string set
 *
 * @author cml
 * @date 2023/11/28
 */
@Converter
public class BaseObjectSetJsonConverter<T> implements AttributeConverter<Set<T>, String> {

    public Class<T> objectClass;

    public BaseObjectSetJsonConverter(Class<T> tClass) {
        this.objectClass = tClass;
    }

    public static <T> BaseObjectSetJsonConverter<T> newConverter(Class<T> tClass) {
        return new BaseObjectSetJsonConverter<T>(tClass);
    }

    /**
     * Convert target class list to json string
     *
     * @param t target class list
     * @return json string
     */
    @Override
    public String convertToDatabaseColumn(Set<T> t) {
        if (t == null) {
            return null;
        }
        return JsonUtils.toJSONString(t);
    }

    /**
     * Convert json string to target class list
     *
     * @param s json string
     * @return target class list
     */
    @Override
    public Set<T> convertToEntityAttribute(String s) {
        if (Strings.isNullOrEmpty(s)) {
            return null;
        }
        return JsonUtils.toJavaSet(s, objectClass);
    }
}
