/*
 *   Copyright 2023 Ant Group Co., Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.secretflow.secretpad.web.utils;

/**
 * Faker utils, mock data for test
 *
 * @author cml
 * @date 2023/07/27
 * @since 4.3
 */

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class FakerUtils {

    @SuppressWarnings("unchecked")
    public static <T> T fake(T object) throws Exception {
        return fakeInternal((Class<T>) object.getClass(), object.getClass().getGenericSuperclass(), object, new HashSet<>());
    }

    public static <T> T fake(Class<T> clazz) throws Exception {
        return fakeInternal(clazz, clazz.getGenericSuperclass(), null, new HashSet<>());
    }

    @SuppressWarnings("unchecked")
    public static <T> T fake(Type type) throws Exception {
        return fakeInternal((Class<T>) ClazzUtils.getClassFromType(type), type, null, new HashSet<>());
    }

    @SuppressWarnings("unchecked")
    public static <T> T fakePrimitiveOrWrapper(Class<T> clazz) {
        Class clz = ClassUtils.primitiveToWrapper(clazz);
        long rand = DataUtils.fakeLong(10000000) + 10000000;
        switch (clz.getSimpleName()) {
            case "Boolean":
                return (T) Boolean.valueOf(true);
            case "Short":
                return (T) Short.valueOf((short) rand);
            case "Integer":
                return (T) Integer.valueOf((int) rand);
            case "Long":
                return (T) Long.valueOf(rand);
            case "Float":
                return (T) Float.valueOf(rand);
            case "Double":
                return (T) Double.valueOf(rand);
            case "Byte":
                return (T) Byte.valueOf((byte) rand);
            case "Character":
                return (T) Character.valueOf((char) rand);
            default:
                return null;
        }
    }

    public static <T> T fakeEnum(Class<T> clazz) {
        T[] constants = clazz.getEnumConstants();
        int index = DataUtils.fakeInt(constants.length);
        return constants[index];
    }

    public static Date fakeDate() {
        return DataUtils.fakeDate(1000000, TimeUnit.SECONDS);
    }

    public static <T> T instantiate(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T fakeArrayInternal(Class<T> clazz, Set<Class> createdClass) throws Exception {
        Class componentType = clazz.getComponentType();
        Object result = Array.newInstance(componentType, 1);
        Array.set(result, 0, fakeInternal(componentType, null, null, createdClass));
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T fakeListInternal(Class<T> clazz, Type parameterizedType, Set<Class> createdClass) throws Exception {
        T result;
        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())) {
            result = (T) new ArrayList<>();
        } else {
            result = instantiate(clazz);
        }
        while (parameterizedType instanceof Class) {
            parameterizedType = ((Class) parameterizedType).getGenericSuperclass();
        }
        if (null == parameterizedType) {
            return result;
        }
        ParameterizedType pType = (ParameterizedType) parameterizedType;
        Type[] types = pType.getActualTypeArguments();
        if (types.length < 1) {
            return result;
        }

        Class typeClass = ClazzUtils.getClassFromType(types[0]);
        ((List) result).add(fakeInternal(typeClass, types[0], null, createdClass));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T fakeMapInternal(Class<T> clazz, Type parameterizedType, Set<Class> createdClass) throws Exception {
        T result;
        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())) {
            result = (T) new HashMap<>();
        } else {
            result = instantiate(clazz);
        }
        while (parameterizedType instanceof Class) {
            parameterizedType = ((Class) parameterizedType).getGenericSuperclass();
        }
        if (null == parameterizedType) {
            return result;
        }
        ParameterizedType pType = (ParameterizedType) parameterizedType;
        Type[] types = pType.getActualTypeArguments();
        if (types.length < 2) {
            return result;
        }

        Class keyClass = ClazzUtils.getClassFromType(types[0]);
        Class valueClass = ClazzUtils.getClassFromType(types[1]);

        Object key = fakeInternal(keyClass, types[0], null, createdClass);
        Object value = fakeInternal(valueClass, types[1], null, createdClass);

        ((Map) result).put(key, value);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T fakeSetInternal(Class<T> clazz, Type parameterizedType, Set<Class> createdClass) throws Exception {
        T result;
        if (Modifier.isInterface(clazz.getModifiers()) || Modifier.isAbstract(clazz.getModifiers())) {
            result = (T) new HashSet<>();
        } else {
            result = instantiate(clazz);
        }
        while (parameterizedType instanceof Class) {
            parameterizedType = ((Class) parameterizedType).getGenericSuperclass();
        }
        if (null == parameterizedType) {
            return result;
        }
        ParameterizedType pType = (ParameterizedType) parameterizedType;
        Type[] types = pType.getActualTypeArguments();
        if (types.length < 1) {
            return result;
        }

        Class typeClass = ClazzUtils.getClassFromType(types[0]);
        ((Set) result).add(fakeInternal(typeClass, types[0], null, createdClass));
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T fakeObjectInternal(Class<T> clazz, Type parameterizedType, Object object, Set<Class> createdClass)
            throws Exception {
        Class clz = null;
        Type pType = null;
        if (parameterizedType instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) parameterizedType).getActualTypeArguments();
            if (actualTypeArguments.length > 1) {
                return null;
            }
            clz = ClazzUtils.getClassFromType(actualTypeArguments[0]);
            pType = actualTypeArguments[0];
        }

        if (createdClass.contains(clazz)) {
            // recursive initial
            return null;
        }
        createdClass.add(clazz);

        T result = (T) object;
        if (null == result) {
            result = instantiate(clazz);
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (!method.getName().startsWith("set")) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            Class propertyType = method.getParameters()[0].getType();

            Object value;
            Type[] pts = method.getGenericParameterTypes();
            if (null != clz && propertyType.equals(Object.class)
                    && pts.length == 1 && (pts[0] instanceof TypeVariable)) {
                value = fakeInternal(clz, pType, null, createdClass);
            } else {
                Type type = method.getParameters()[0].getParameterizedType();
                value = fakeInternal(propertyType, type, null, createdClass);
            }
            method.invoke(result, value);
        }

        createdClass.remove(clazz);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T fakeInternal(Class<T> clazz, Type parameterizedType, Object object, Set<Class> createdClass) throws Exception {
        if (Object.class.equals(clazz)) {
            return null;
        } else if (clazz.isAssignableFrom(String.class)) {
            return (T) DataUtils.fakeHex(10);
        } else if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            return fakePrimitiveOrWrapper(clazz);
        } else if (clazz.isArray()) {
            return fakeArrayInternal(clazz, createdClass);
        } else if (clazz.isEnum()) {
            return fakeEnum(clazz);
        } else if (List.class.isAssignableFrom(clazz)) {
            return fakeListInternal(clazz, parameterizedType, createdClass);
        } else if (Map.class.isAssignableFrom(clazz)) {
            return fakeMapInternal(clazz, parameterizedType, createdClass);
        } else if (Set.class.isAssignableFrom(clazz)) {
            return fakeSetInternal(clazz, parameterizedType, createdClass);
        } else if (clazz.isAssignableFrom(Date.class)) {
            return (T) fakeDate();
        } else if (ClazzUtils.isJavaClass(clazz)) {
            // TODO: other build-in classes, not implemented
            return null;
        } else if (clazz.isInterface()) {
            // TODO: interface type, can not instantiate
            return null;
        } else {
            return fakeObjectInternal(clazz, parameterizedType, object, createdClass);
        }
    }
}
