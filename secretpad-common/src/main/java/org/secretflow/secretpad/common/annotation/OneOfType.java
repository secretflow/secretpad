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

package org.secretflow.secretpad.common.annotation;

import org.secretflow.secretpad.common.validator.ApprovalTypeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * OneOfType.
 *
 * @author cml
 * @date 2023/09/19
 */
@Constraint(validatedBy = ApprovalTypeValidator.class)
@Documented

@Retention(RUNTIME)

@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})

public @interface OneOfType {

    String message() default "";

    String[] types() default {};


    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}


