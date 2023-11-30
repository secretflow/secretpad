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

package org.secretflow.secretpad.web;

import com.google.common.collect.Lists;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;

/**
 * SecretPad application
 *
 * @author yansi
 * @date 2023/3/23
 */
@ComponentScan(basePackages = "org.secretflow.secretpad.*")
@SpringBootApplication
public class SecretPadApplication {

    @Value("${server.http-port}")
    private Integer httpPort;
    @Value("${server.http-port-inner}")
    private Integer innerHttpPort;

    public static void main(String[] args) {
        SpringApplication.run(SecretPadApplication.class, args);
    }

    /**
     * Build tomcat servlet webServer factory
     *
     * @return tomcat servlet webServer factory
     */
    @Bean
    public ServletWebServerFactory containerFactory() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        tomcat.addAdditionalTomcatConnectors(connector);

        Connector innerConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        innerConnector.setPort(innerHttpPort);
        tomcat.addAdditionalTomcatConnectors(innerConnector);
        return tomcat;
    }

    /**
     * Build a new http message converters
     *
     * @return a new http message converters
     */
    @Bean
    public HttpMessageConverters protobufHttpMessageConverter() {
        ProtobufHttpMessageConverter protobufHttpMessageConverter = new ProtobufHttpMessageConverter();
        protobufHttpMessageConverter.setSupportedMediaTypes(Lists.newArrayList(MediaType.APPLICATION_JSON, MediaType.parseMediaType(MediaType.TEXT_PLAIN_VALUE + ";charset=ISO-8859-1")));
        return new HttpMessageConverters(protobufHttpMessageConverter);
    }
}
