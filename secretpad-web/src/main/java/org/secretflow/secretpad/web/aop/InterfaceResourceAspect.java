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

import org.secretflow.secretpad.common.annotation.resource.ApiResource;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.AuthErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.EnvService;
import org.secretflow.secretpad.service.auth.ApiResourceAuth;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author beiwei
 * @date 2023/9/11
 */
@Aspect
@Component
public class InterfaceResourceAspect {
    @Autowired
    private ApiResourceAuth apiResourceAuth;
    @Autowired
    private EnvService envService;


    @Pointcut("@annotation(org.secretflow.secretpad.common.annotation.resource.ApiResource)")
    public void pointCut() {
    }

    @Around("pointCut() && args(data) && @annotation(apiResource)")
    public Object check(ProceedingJoinPoint joinPoint, Object data, ApiResource apiResource) throws Throwable {
        if (PlatformTypeEnum.EDGE.equals(envService.getPlatformType())) {
            // ignore check for edge platform
            return joinPoint.proceed();
        }
        if (UserOwnerTypeEnum.CENTER.equals(UserContext.getUser().getOwnerType())) {
            // ignore check for center user
            return joinPoint.proceed();
        }
        if (!apiResourceAuth.check(apiResource.code())) {
            String err = String.format("No permission to access the interface(%s). owner_type(%s), owner_id(%s)",
                    apiResource.code(),
                    UserContext.getUser().getOwnerType(), UserContext.getUser().getOwnerId());
            throw SecretpadException.of(AuthErrorCode.AUTH_FAILED, err);
        }
        return joinPoint.proceed();
    }
}
