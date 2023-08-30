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

import org.secretflow.secretpad.common.util.ProtoUtils;

import com.google.protobuf.Message;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Interconvert between Message and String
 *
 * @author yansi
 * @date 2023/5/30
 */
@Converter
public class ProtoMessageConverter implements AttributeConverter<Message, String> {
    /**
     * Convert Message to String
     *
     * @param attribute Message value
     * @return String
     */
    @Override
    public String convertToDatabaseColumn(Message attribute) {
        if (attribute != null) {
            return ProtoUtils.toJsonString(attribute);
        }
        return null;
    }

    /**
     * Convert String to Message
     *
     * @param dbData String value
     * @return Message
     */
    @Override
    public Message convertToEntityAttribute(String dbData) {
        if (dbData != null && !dbData.isEmpty()) {
            return ProtoUtils.fromJsonString(dbData);
        }
        return null;
    }
}
