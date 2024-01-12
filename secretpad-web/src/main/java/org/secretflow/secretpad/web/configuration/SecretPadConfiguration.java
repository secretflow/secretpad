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

package org.secretflow.secretpad.web.configuration;

import org.secretflow.secretpad.common.i18n.LocaleMessageResolver;
import org.secretflow.secretpad.common.i18n.MessageResolver;
import org.secretflow.secretpad.web.exception.BasicErrorHandler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

/**
 * Configuration for secretPad
 *
 * @author yansi
 * @date 2023/3/24
 */
@Configuration
@EnableAutoConfiguration
public class SecretPadConfiguration {

    /**
     * Create a new bean for message source
     *
     * @return a new message source bean
     */
    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * Create a new bean for message resolver
     *
     * @param messageSource message source
     * @return a new message resolver bean
     */
    @Bean
    public MessageResolver messageResolver(MessageSource messageSource) {
        return new LocaleMessageResolver(messageSource);
    }

    @Bean
    public BasicErrorHandler basicErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties,
                                                  ObjectProvider<List<ErrorViewResolver>> errorViewResolversProvider) {
        return new BasicErrorHandler(errorAttributes, serverProperties.getError(),
                errorViewResolversProvider.getIfAvailable());
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(3));
        restTemplateBuilder.setReadTimeout(Duration.ofSeconds(3));
        return restTemplateBuilder.build();
    }
}
