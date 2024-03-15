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

package org.secretflow.secretpad.web.exception;

import org.secretflow.secretpad.common.errorcode.ErrorCode;
import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.i18n.MessageResolver;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;

import jakarta.annotation.Resource;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * SecretPad exception handler
 *
 * @author yansi
 * @date 2023/5/10
 */
@RestControllerAdvice
public class SecretpadExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretpadExceptionHandler.class);

    @Resource
    private MessageResolver messageResolver;

    /**
     * Catch SecretPadException and build SecretPadResponse with errorCode and message
     *
     * @param ex SecretPadException
     * @return SecretPadResponse with errorCode and message
     */
    @ExceptionHandler(value = SecretpadException.class)
    public SecretPadResponse<Object> handlerSecretpadException(SecretpadException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String message = messageResolver.getMessage(errorCode, ex.getArgs());
        LOGGER.error("find SecretpadException error: {}, message: {} ", ex.getErrorCode(), ex.getMessage(), ex);
        return new SecretPadResponse<>(new SecretPadResponse.SecretPadResponseStatus(errorCode.getCode(), message),
                null);
    }

    /**
     * Catch MethodArgumentNotValidException and build SecretPadResponse with validation error and message
     *
     * @param ex MethodArgumentNotValidException
     * @return SecretPadResponse with validation error and message
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Object handlerValidation(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<String> collect = new ArrayList<>();
        for (FieldError fieldError : fieldErrors) {
            String defaultMessage = fieldError.getDefaultMessage();
            collect.add(defaultMessage);
        }
        LOGGER.error("handler argument check error", ex);
        return handlerSecretpadException(SecretpadException.of(SystemErrorCode.VALIDATION_ERROR, collect.get(0)));
    }

    /**
     * Catch Exception and build SecretPadResponse with unknown error and message
     *
     * @param ex Exception
     * @return SecretPadResponse with unknown error and message
     */
    @ExceptionHandler(value = Exception.class)
    public Object handleException(Exception ex) {
        LOGGER.error("handler Exception error", ex);
        return handlerSecretpadException(SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, ex.getMessage()));
    }

    /**
     * Catch FileSizeLimitExceededException and build SecretPadResponse with unknown error and message
     *
     * @param ex FileSizeLimitExceededException
     * @return SecretPadResponse with unknown error and message
     */
    @ExceptionHandler(value = FileSizeLimitExceededException.class)
    public Object handleFileSizeException(Exception ex) {
        LOGGER.error("handler FileSizeLimitExceededException error", ex);
        return handlerSecretpadException(SecretpadException.of(SystemErrorCode.UNKNOWN_ERROR, ex.getMessage()));
    }

}
