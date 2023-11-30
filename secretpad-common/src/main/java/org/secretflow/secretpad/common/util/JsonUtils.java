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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Json utils
 *
 * @author yansi
 * @date 2023/5/10
 */
public class JsonUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final ObjectMapper OM = new ObjectMapper();

    /**
     * Set ObjectMapper config
     */
    static {
        OM.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OM.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        OM.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        OM.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        OM.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        OM.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OM.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OM.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        OM.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        OM.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        OM.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DATE_FORMATTER));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FORMATTER));
        OM.registerModule(javaTimeModule);
        OM.setTimeZone(TimeZone.getDefault());
    }

    /**
     * Make java type with parametrized and parameterClasses
     *
     * @param parametrized
     * @param parameterClasses
     * @return JavaType
     */
    public static JavaType makeJavaType(Class<?> parametrized, Class<?>... parameterClasses) {
        return OM.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    /**
     * Make java type with rawType and parameterTypes
     *
     * @param rawType
     * @param parameterTypes
     * @return JavaType
     */
    public static JavaType makeJavaType(Class<?> rawType, JavaType... parameterTypes) {
        return OM.getTypeFactory().constructParametricType(rawType, parameterTypes);
    }

    /**
     * Convert object to json string
     *
     * @param value
     * @return String
     */
    public static String toString(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return toJSONString(value);
    }

    /**
     * Convert object to json string with writeValueAsString
     *
     * @param value
     * @return String
     * @throws RuntimeException
     */
    public static String toJSONString(Object value) {
        try {
            return OM.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            LOGGER.error("object to json failed, error is {}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert object to json string with writerWithDefaultPrettyPrinter
     *
     * @param value
     * @return String
     * @throws RuntimeException
     */
    public static String toPrettyString(Object value) {
        try {
            return OM.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert object to JsonNode
     *
     * @param value
     * @return JsonNode
     */
    public static JsonNode fromJavaObject(Object value) {
        JsonNode result = null;
        if (Objects.nonNull(value) && (value instanceof String)) {
            result = parseObject((String) value);
        } else {
            result = OM.valueToTree(value);
        }
        return result;
    }

    /**
     * Convert string to JsonNode
     *
     * @param content
     * @return JsonNode
     */
    public static JsonNode parseObject(String content) {
        try {
            return OM.readTree(content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get JsonNode from name
     *
     * @param node
     * @param name
     * @return JsonNode
     */
    public static JsonNode getJsonElement(JsonNode node, String name) {
        return node.get(name);
    }

    /**
     * Get JsonNode from index
     *
     * @param node
     * @param index
     * @return JsonNode
     */
    public static JsonNode getJsonElement(JsonNode node, int index) {
        return node.get(index);
    }

    /**
     * Convert TreeNode to java target class
     *
     * @param node
     * @param clazz
     * @param <T>
     * @return target class
     * @throws RuntimeException
     */
    public static <T> T toJavaObject(TreeNode node, Class<T> clazz) {
        try {
            return OM.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert TreeNode to java target class with javaType
     *
     * @param node
     * @param javaType
     * @param <T>
     * @return target class
     */
    public static <T> T toJavaObject(TreeNode node, JavaType javaType) {
        return OM.convertValue(node, javaType);
    }

    /**
     * Convert TreeNode to java target class with typeReference
     *
     * @param node
     * @param typeReference
     * @param <T>
     * @return target class
     */
    public static <T> T toJavaObject(TreeNode node, TypeReference<T> typeReference) {
        return OM.convertValue(node, typeReference);
    }

    /**
     * Convert TreeNode to java target class with type
     *
     * @param node
     * @param type
     * @param <T>
     * @return target class
     */
    public static <T> T toJavaObject(TreeNode node, Type type) {
        return toJavaObject(node, OM.constructType(type));
    }

    /**
     * Convert TreeNode to java target class list
     *
     * @param node
     * @param clazz
     * @param <E>
     * @return target class list
     */
    public static <E> List<E> toJavaList(TreeNode node, Class<E> clazz) {
        return toJavaObject(node, makeJavaType(List.class, clazz));
    }

    /**
     * Convert TreeNode to object list
     *
     * @param node
     * @return object list
     */
    public static List<Object> toJavaList(TreeNode node) {
        return toJavaObject(node, new TypeReference<List<Object>>() {
        });
    }

    /**
     * Convert TreeNode to java target class map
     *
     * @param node
     * @param clazz
     * @param <V>
     * @return target class map
     */
    public static <V> Map<String, V> toJavaMap(TreeNode node, Class<V> clazz) {
        return toJavaObject(node, makeJavaType(Map.class, String.class, clazz));
    }

    /**
     * Convert TreeNode to java target class map
     *
     * @param node
     * @return target class map
     */
    public static Map<String, Object> toJavaMap(TreeNode node) {
        return toJavaObject(node, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Convert string to java target class
     *
     * @param content
     * @param clazz
     * @param <T>
     * @return target class
     * @throws RuntimeException
     */
    public static <T> T toJavaObject(String content, Class<T> clazz) {
        try {
            return OM.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error("json to object failed, json is {}, error is {}", content, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert string to java target class with javaType
     *
     * @param content
     * @param javaType
     * @param <T>
     * @return target class
     * @throws RuntimeException
     */
    public static <T> T toJavaObject(String content, JavaType javaType) {
        try {
            return OM.readValue(content, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert string to java target class with typeReference
     *
     * @param content
     * @param typeReference
     * @param <T>
     * @return target class
     * @throws RuntimeException
     */
    public static <T> T toJavaObject(String content, TypeReference<T> typeReference) {
        try {
            return OM.readValue(content, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert string to java target class with type
     *
     * @param content
     * @param type
     * @param <T>
     * @return target class
     */
    public static <T> T toJavaObject(String content, Type type) {
        return toJavaObject(content, OM.constructType(type));
    }

    /**
     * Convert string to java target class list
     *
     * @param content
     * @param clazz
     * @param <E>
     * @return target class list
     */
    public static <E> List<E> toJavaList(String content, Class<E> clazz) {
        return toJavaObject(content, makeJavaType(List.class, clazz));
    }

    /**
     * Convert string to java target class list
     *
     * @param content
     * @return target class list
     */
    public static List<Object> toJavaList(String content) {
        return toJavaObject(content, new TypeReference<List<Object>>() {
        });
    }

    /**
     * Convert content to java target class map
     *
     * @param content
     * @param clazz
     * @param <V>
     * @return target class map
     */
    public static <V> Map<String, V> toJavaMap(String content, Class<V> clazz) {
        return toJavaObject(content, makeJavaType(Map.class, String.class, clazz));
    }

    /**
     * Convert content to java target class map
     *
     * @param content
     * @return target class map
     */
    public static Map<String, Object> toJavaMap(String content) {
        return toJavaObject(content, new TypeReference<Map<String, Object>>() {
        });
    }

    public static <E> List<E> deepCopyList(List<E> list, Class<E> clazz) {
        if (CollectionUtils.isEmpty(list)) return list;
        return toJavaObject(toJSONString(list), makeJavaType(List.class, clazz));
    }
}
