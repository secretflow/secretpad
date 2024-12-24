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

import org.secretflow.secretpad.web.constant.AuthConstants;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * SecretPad application
 *
 * @author yansi
 * @date 2023/3/23
 */
@Slf4j
@ComponentScan(basePackages = {"org.secretflow.secretpad.*"})
@SpringBootApplication
@EnableAsync
@EnableCaching
public class SecretPadApplication {

    @Value("${server.http-port}")
    private Integer httpPort;
    @Value("${server.http-port-inner}")
    private Integer innerHttpPort;
    @Value("${server.compression.mime-types}")
    private String mimeTypes;
    @Value("${server.compression.min-response-size}")
    private Integer compressionMinResponseSize;

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext context = SpringApplication.run(SecretPadApplication.class, args);
        Environment environment = context.getBean(Environment.class);
        printEnvironment(environment);
    }

    private static void printEnvironment(Environment environment) throws UnknownHostException {
        log.info("SecretPad start success, http://{}:{} innerHttpPort:{} Profile:{}", InetAddress.getLocalHost().getHostAddress(), environment.getProperty("server.port"), environment.getProperty("server.http-port-inner"), environment.getActiveProfiles());
        String userName, password;
        try {
            userName = environment.getProperty("secretpad.auth.pad_name", String.class, AuthConstants.USER_NAME);
        } catch (Exception e) {
            log.debug("initUserAndPwd failed use default", e);
            userName = AuthConstants.USER_NAME;
        }
        try {
            password = environment.getProperty("secretpad.auth.pad_pwd", String.class, AuthConstants.getRandomPassword());
        } catch (Exception e) {
            log.debug("initUserAndPwd failed use default", e);
            password = AuthConstants.getRandomPassword();
        }
        log.info("userName:{} password:{}", userName, password);
    }

    /**
     * Build tomcat servlet webServer factory
     *
     * @return tomcat servlet webServer factory
     */
    @Bean
    public ServletWebServerFactory containerFactory() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        buildConnector(tomcat, httpPort);
        buildConnector(tomcat, innerHttpPort);
        tomcat.setUriEncoding(StandardCharsets.UTF_8);
        return tomcat;
    }

    private void buildConnector(TomcatServletWebServerFactory tomcat, Integer innerHttpPort) {
        Connector innerConnector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        innerConnector.setPort(innerHttpPort);
        innerConnector.setProperty(ConnectorCompression.COMPRESSION, "on");
        innerConnector.setProperty(ConnectorCompression.COMPRESSION_MIN_RESPONSE_SIZE, String.valueOf(compressionMinResponseSize));
        innerConnector.setProperty(ConnectorCompression.COMPRESSION_MIME_TYPES, mimeTypes);
        tomcat.addAdditionalTomcatConnectors(innerConnector);
    }

    private static class ConnectorCompression {
        static final String COMPRESSION_MIN_RESPONSE_SIZE = "compressionMinResponseSize";
        static final String COMPRESSION_MIME_TYPES = "compressionMimeTypes";
        static final String COMPRESSION = "compression";
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
