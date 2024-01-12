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

package org.secretflow.secretpad.common.exception;

import org.secretflow.secretpad.common.errorcode.ErrorCode;

/**
 * SecretPad Exception
 *
 * @author yansi
 * @date 2023/5/10
 */
public final class SecretpadException extends RuntimeException {
    /**
     * error code
     */
    private final ErrorCode errorCode;
    /**
     * error args
     */
    private final String[] args;

    /**
     * Fill SecretPad Exception
     *
     * @param errorCode
     * @param cause
     * @param args
     */
    private SecretpadException(ErrorCode errorCode, Throwable cause, String... args) {
        super(args != null && args.length > 0 ? args[0] : "", cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * Build SecretPad Exception with args
     *
     * @param errorCode
     * @param args
     * @return SecretPad exception
     */
    public static SecretpadException of(ErrorCode errorCode, String... args) {
        return new SecretpadException(errorCode, null, args);
    }

    /**
     * Build SecretPad Exception with args
     *
     * @param errorCode
     * @param args
     * @return SecretPad exception
     */
    public static SecretpadException of(ErrorCode errorCode, Throwable cause, String... args) {
        return new SecretpadException(errorCode, cause, args);
    }

    /**
     * Build SecretPad Exception with cause
     *
     * @param errorCode
     * @param cause
     * @return SecretPad exception
     */
    public static SecretpadException of(ErrorCode errorCode, Throwable cause) {
        return new SecretpadException(errorCode, cause, cause.getMessage());
    }

    /**
     * @return error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * @return error args
     */
    public String[] getArgs() {
        return args;
    }
}