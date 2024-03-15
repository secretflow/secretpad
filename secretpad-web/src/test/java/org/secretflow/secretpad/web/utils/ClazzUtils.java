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
 * Class util, avoid conflict with org.apache.commons.lang3.ClassUtils
 *
 * @author cml
 * @date 2023/07/27
 * @since 4.3
 */

import org.springframework.aop.framework.Advised;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClazzUtils {
    public static Class<?> getTargetClass(Object object) {
        if (!(object instanceof Advised)) {
            return object.getClass();
        } else if (((Advised) object).getProxiedInterfaces().length > 0) {
            return ((Advised) object).getProxiedInterfaces()[0];
        }
        return null;
    }

    public static Class<?> getClassFromType(Type type) throws ClassNotFoundException {
        if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        } else if (type instanceof Class) {
            return (Class) type;
        }
        return Class.forName(type.getTypeName());
    }

    public static Object unwrap(Object bean, Class<?> clazz) throws Exception {
        if (clazz.isInstance(bean)) {
            return bean;
        }
        while (bean instanceof Advised advised) {
            if (null == advised.getTargetSource() || null == advised.getTargetSource().getTarget()) {
                return null;
            }
            if (clazz.isAssignableFrom(advised.getTargetClass())) {
                return advised.getTargetSource().getTarget();
            }
            bean = advised.getTargetSource().getTarget();
        }
        return null;
    }

    public static boolean isJavaClass(Class clazz) {
        String name = clazz.getName();
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("sun.")
                || name.startsWith("oracle.")
                || name.startsWith("com.sun")
                || name.startsWith("com.oracle")
                || name.startsWith("org.xml");
    }
}
