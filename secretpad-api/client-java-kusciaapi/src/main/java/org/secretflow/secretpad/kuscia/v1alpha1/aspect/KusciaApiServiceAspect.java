/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.kuscia.v1alpha1.aspect;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.secretflow.v1alpha1.common.Common;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * @author yutu
 * @date 2024/06/18
 */
@Slf4j
@Aspect
@Component
public class KusciaApiServiceAspect {

    @Pointcut("execution(public * org.secretflow.secretpad.kuscia.v1alpha1.service.impl..*(..))")
    public void serviceLayerExecution() {
    }

    @Around("serviceLayerExecution()")
    public Object aroundServiceMethod(ProceedingJoinPoint joinPoint) {
        checkArgs(joinPoint);
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            log.error("An exception occurred while executing method: {}", joinPoint.getSignature().getName(), e);
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class<?> returnType = signature.getReturnType();
            return buildErrorResponse(e, returnType);
        }
    }

    public void checkArgs(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1 && ObjectUtils.isEmpty(args[0])) {
            throw new IllegalArgumentException("Invalid arguments.");
        }
        if (args.length == 2 && (ObjectUtils.isEmpty(args[0]) || ObjectUtils.isEmpty(args[1]))) {
            throw new IllegalArgumentException("Invalid arguments.");
        }
    }

    public Object buildErrorResponse(Throwable e, Class<?> type) {
        log.error("[kuscia] buildErrorResponse ", e);
        int code = -1;
        String message = e.getMessage();
        if (e instanceof StatusRuntimeException) {
            code = ((StatusRuntimeException) e).getStatus().getCode().value();
            message = ((StatusRuntimeException) e).getStatus().getDescription();
        } else if (e instanceof StatusException) {
            code = ((StatusException) e).getStatus().getCode().value();
            message = ((StatusException) e).getStatus().getDescription();
        }
        try {
            Object instance = createInstance(type);
            Common.Status status = Common.Status.newBuilder().setCode(code).setMessage(message).build();
            Field statusField = instance.getClass().getDeclaredField("status_");
            statusField.setAccessible(true);
            statusField.set(instance, status);
            log.info("[kuscia] buildErrorResponse {}", instance);
            return instance;
        } catch (Exception ex) {
            log.error("Failed to create instance of type: {}", type.getName(), ex);
            return null;
        }
    }


    public static Object createInstance(Class<?> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}