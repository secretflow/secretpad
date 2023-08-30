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

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Proto utils
 *
 * @author yansi
 * @date 2023/5/30
 */
public class ProtoUtils {
    /**
     * Convert proto to map
     *
     * @param proto
     * @return proto map
     */
    @NotNull
    public static Map<String, Object> protoToMap(Message proto) {
        final Map<Descriptors.FieldDescriptor, Object> allFields = proto.getAllFields();
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : allFields.entrySet()) {
            final Descriptors.FieldDescriptor fieldDescriptor = entry.getKey();
            final Object requestVal = entry.getValue();
            final Object mapVal = convertVal(proto, fieldDescriptor, requestVal);
            if (mapVal != null) {
                final String fieldName = fieldDescriptor.getName();
                map.put(fieldName, mapVal);
            }
        }
        return map;
    }

    /**
     * Convert proto to list map
     *
     * @param protos
     * @return list map
     */
    @NotNull
    public static List<Map<String, Object>> protosToListMap(List<? extends Message> protos) {
        if (!CollectionUtils.isEmpty(protos)) {
            return protos.stream().map(proto -> protoToMap(proto)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Convert message to json string
     *
     * @param message
     * @return json string
     * @throws RuntimeException
     */
    public static String toJsonString(Message message) {
        try {
            return JsonFormat.printer().preservingProtoFieldNames().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert message to json string using registry
     *
     * @param message
     * @param registry
     * @return json string
     * @throws RuntimeException
     */
    public static String toJsonString(Message message, JsonFormat.TypeRegistry registry) {
        try {
            return JsonFormat.printer().usingTypeRegistry(registry).preservingProtoFieldNames().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert string to Message
     *
     * @param json
     * @return converted message
     * @throws RuntimeException
     */
    public static Message fromJsonString(String json) {
        Struct.Builder structBuilder = Struct.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, structBuilder);
            return structBuilder.build();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert string to Message with messageBuilder
     *
     * @param json
     * @param messageBuilder
     * @return converted message
     * @throws RuntimeException
     */
    public static <T extends Message.Builder> Message fromJsonString(String json, T messageBuilder) {
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, messageBuilder);
            return messageBuilder.build();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert object to Message with messageBuilder
     *
     * @param object
     * @param messageBuilder
     * @param <T>
     * @return converted message
     */
    public static <T extends Message.Builder> Message fromObject(Object object, T messageBuilder) {
        return fromJsonString(JsonUtils.toJSONString(object), messageBuilder);
    }

    /**
     * Batch convert from protoVal
     *
     * @param proto
     * @param fieldDescriptor
     * @param protoVal
     * @return converted object
     */
    @Nullable
    private static Object convertVal(@NotNull Message proto, @NotNull Descriptors.FieldDescriptor fieldDescriptor, @Nullable Object protoVal) {
        Object result = null;
        if (protoVal != null) {
            if (fieldDescriptor.isRepeated()) {
                if (proto.getRepeatedFieldCount(fieldDescriptor) > 0) {
                    final List originals = (List) protoVal;
                    final List copies = new ArrayList(originals.size());
                    for (Object original : originals) {
                        copies.add(convertAtomicVal(fieldDescriptor, original));
                    }
                    result = copies;
                }
            } else {
                result = convertAtomicVal(fieldDescriptor, protoVal);
            }
        }
        return result;
    }


    /**
     * Convert atomicVal javaType from protoVal
     *
     * @param fieldDescriptor
     * @param protoVal
     * @return converted object
     */
    @Nullable
    private static Object convertAtomicVal(@NotNull Descriptors.FieldDescriptor fieldDescriptor, @Nullable Object protoVal) {
        Object result = null;
        if (protoVal != null) {
            switch (fieldDescriptor.getJavaType()) {
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOLEAN:
                case STRING:
                    result = protoVal;
                    break;
                case BYTE_STRING:
                case ENUM:
                    result = protoVal.toString();
                    break;
                case MESSAGE:
                    result = protoToMap((Message) protoVal);
                    break;
                default:
                    throw new IllegalArgumentException("not supported javaType");
            }
        }
        return result;
    }
}
