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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yutu
 * @date 2023/08/03
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaQuery {
    String propName() default "";

    Type type() default Type.EQUAL;

    String joinName() default "";

    Join join() default Join.LEFT;

    String blurry() default "";

    String or() default "";

    enum Type {
        /*
         * EQUAL
         */
        EQUAL,
        /*
         * GREATER_THAN
         */
        GREATER_THAN,
        /*
         * LESS_THAN
         */
        LESS_THAN,
        /*
         * INNER_LIKE
         */
        INNER_LIKE,
        /*
         * OR_INNER_LIKE
         */
        OR_INNER_LIKE,
        /*
         * LEFT_LIKE
         */
        LEFT_LIKE,
        /*
         * RIGHT_LIKE
         */
        RIGHT_LIKE,
        /*
         * LESS_THAN_NQ
         */
        LESS_THAN_NQ,
        /*
         * IN
         */
        IN,
        /*
         * NOT_IN
         */
        NOT_IN,
        /*
         * NOT_EQUAL
         */
        NOT_EQUAL,
        /*
         * BETWEEN
         */
        BETWEEN,
        /*
         * NOT_NULL
         */
        NOT_NULL,
        /*
         * IS_NULL
         */
        IS_NULL,
        // eg: SELECT * FROM table WHERE FIND_IN_SET('querytag', table.tags);
        FIND_IN_SET
    }

    enum Join {
        /*
         * LEFT
         */
        LEFT,
        /*
         * RIGHT
         */
        RIGHT,
        /*
         * INNER
         */
        INNER
    }
}