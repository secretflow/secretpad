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

import org.secretflow.secretpad.common.errorcode.SystemErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author yutu
 * @date 2023/08/11
 */
@Slf4j
public class BasicErrorHandler extends BasicErrorController {

    public BasicErrorHandler(ErrorAttributes errorAttributes, ErrorProperties errorProperties,
                             List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = this.getStatus(request);
        log.error("BasicErrorController get error {}", status.toString());
        if (status.is4xxClientError()) {
            if (status.equals(HttpStatus.NOT_FOUND)) {
                throw SecretpadException.of(SystemErrorCode.HTTP_404_ERROR, status.toString());
            }
            throw SecretpadException.of(SystemErrorCode.HTTP_4XX_ERROR, status.toString());
        }
        throw SecretpadException.of(SystemErrorCode.HTTP_5XX_ERROR, status.toString());
    }
}
