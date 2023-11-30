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

package org.secretflow.secretpad.web.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.secretflow.secretpad.common.annotation.resource.DataResource;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.enums.DataResourceTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.auth.DataResourceAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author beiwei
 * @date 2023/9/11
 */
@Aspect
@Order(1)
@Component
@Slf4j
public class DataResourceAspect {

    @Autowired
    private DataResourceAuth dataResourceAuth;

    @Autowired
    private EnvService envService;

    @Pointcut("@annotation(org.secretflow.secretpad.common.annotation.resource.DataResource)")
    public void pointCut() {
    }

    @Around("pointCut() && args(data) && @annotation(dataResource)")
    public Object check(ProceedingJoinPoint joinPoint, Object data, DataResource dataResource) throws Throwable {
        if (PlatformTypeEnum.EDGE.equals(envService.getPlatformType()) && !DataResourceTypeEnum.NODE_ID.equals(dataResource.resourceType())){
            // ignore check for edge platform
            return joinPoint.proceed();
        }
        if (UserOwnerTypeEnum.CENTER.equals(UserContext.getUser().getOwnerType())){
            // ignore check for center user
            return joinPoint.proceed();
        }
        Object feildValue;
        try {
            Field nodeIdField = data.getClass().getDeclaredField(dataResource.field());
            nodeIdField.setAccessible(true);
            feildValue = ReflectionUtils.getField(nodeIdField, data);
        } catch (NoSuchFieldException e) {
            String err = String.format("Invalid field. The field(%s) does not exist.", dataResource.field());
            log.error(err);
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, err);
        }

        if (!dataResourceAuth.check(dataResource.resourceType(), (String) feildValue)) {
            String err = String.format("No permission to access the data %s(%s). owner_type(%s), owner_id(%s)",
                    dataResource.resourceType(), feildValue,
                    UserContext.getUser().getOwnerType(),UserContext.getUser().getOwnerId());
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, err);
        }
        return joinPoint.proceed();
    }

}
