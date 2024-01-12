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

package org.secretflow.secretpad.web.controller;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.enums.PlatformTypeEnum;
import org.secretflow.secretpad.common.enums.UserOwnerTypeEnum;
import org.secretflow.secretpad.common.errorcode.ErrorCode;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.UserContext;
import org.secretflow.secretpad.service.model.common.SecretPadResponse;
import org.secretflow.secretpad.service.util.DbSyncUtil;
import org.secretflow.secretpad.web.SecretPadApplication;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Basic controller test
 *
 * @author yansi
 * @date 2023/7/24
 */
@WebAppConfiguration
@ActiveProfiles(value = "test")
@AutoConfigureMockMvc
@SpringBootTest(classes = SecretPadApplication.class)
public class ControllerTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(ControllerTest.class);
    public static final String PROJECT_ID = "projectagdasvacaghyhbvscvyjnba";

    @Autowired
    private MockMvc mockMvc;

    public static MockedStatic<DbSyncUtil> pushToCenterUtilMockedStatic;

    @BeforeAll
    public static void setup() throws IOException, InterruptedException {
        //todo setup operations will replace with codes
        LOGGER.info("start to setup");
        ProcessBuilder pb = new ProcessBuilder("./config/setup.sh");
        Process process = pb.start();
        process.waitFor();
    }

    @BeforeEach
    public void initSession() {
        UserContext.setBaseUser(UserContextDTO.builder().ownerId("alice")
                .platformType(PlatformTypeEnum.CENTER)
                .ownerType(UserOwnerTypeEnum.CENTER)
                .projectIds(Set.of(PROJECT_ID)).build());
        pushToCenterUtilMockedStatic = Mockito.mockStatic(DbSyncUtil.class);
    }

    @AfterEach
    public void after() {
        pushToCenterUtilMockedStatic.close();
    }

    void assertResponse(MvcRequestFunction<MockHttpServletRequestBuilder> f) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(f.apply()
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        SecretPadResponse secretPadResponse = JsonUtils.toJavaObject(response.getContentAsString(), SecretPadResponse.class);
        Assertions.assertEquals(secretPadResponse.getStatus().getCode(), 0);
        Assertions.assertNotNull(secretPadResponse.getData());
    }

    void assertResponseWithEmptyData(MvcRequestFunction<MockHttpServletRequestBuilder> f) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(f.apply()
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        SecretPadResponse secretPadResponse = JsonUtils.toJavaObject(response.getContentAsString(), SecretPadResponse.class);
        Assertions.assertEquals(secretPadResponse.getStatus().getCode(), 0);
        Assertions.assertNull(secretPadResponse.getData());
    }

    void assertErrorCode(MvcRequestFunction<MockHttpServletRequestBuilder> f, ErrorCode errorCode) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(f.apply()
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        SecretPadResponse secretPadResponse = JsonUtils.toJavaObject(response.getContentAsString(), SecretPadResponse.class);
        Assertions.assertEquals(secretPadResponse.getStatus().getCode(), errorCode.getCode());
    }

    void assertResponseWithEmptyContent(MvcRequestFunction<MockHttpServletRequestBuilder> f) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(f.apply()
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        Assertions.assertEquals(response.getContentAsString(), "");
    }

    void assertMultipartResponse(MvcRequestFunction<MockHttpServletRequestBuilder> f) throws Exception {
        MockHttpServletResponse response = mockMvc.perform(f.apply()
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse();
        SecretPadResponse secretPadResponse = JsonUtils.toJavaObject(response.getContentAsString(), SecretPadResponse.class);
        Assertions.assertEquals(secretPadResponse.getStatus().getCode(), 0);
        Assertions.assertNotNull(secretPadResponse.getData());
    }

    String getMappingUrl(Class<?> clazz, String methodName, @Nullable Class<?>... paramTypes) {
        String url = "";
        Method method = ClassUtils.getMethod(clazz, methodName, paramTypes);
        Assert.isTrue(Objects.nonNull(method), "testMethod not exists");
        String regex = "^.*Mapping$";
        Pattern pattern = Pattern.compile(regex);
        for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
            Class<? extends Annotation> annotationType = declaredAnnotation.annotationType();
            String annotationName = annotationType.getSimpleName();
            Matcher matcher = pattern.matcher(annotationName);
            if (matcher.find()) {
                switch (annotationName) {
                    case "PostMapping" -> {
                        PostMapping annotation = (PostMapping) declaredAnnotation;
                        url = annotation.value()[0];
                    }
                    case "GetMapping" -> {
                        GetMapping annotation = (GetMapping) declaredAnnotation;
                        url = annotation.value()[0];
                    }
                    case "PutMapping" -> {
                        PutMapping annotation = (PutMapping) declaredAnnotation;
                        url = annotation.value()[0];
                    }
                    case "DeleteMapping" -> {
                        DeleteMapping annotation = (DeleteMapping) declaredAnnotation;
                        url = annotation.value()[0];
                    }
                    case "RequestMapping" -> {
                        RequestMapping annotation = (RequestMapping) declaredAnnotation;
                        url = annotation.value()[0];
                    }
                    default -> throw new RuntimeException("method does not match any mapping annotation");
                }
            }
        }
        Assert.isTrue(StringUtils.isNotBlank(url), "controller method url not found");

        for (Annotation classAnnotation : clazz.getDeclaredAnnotations()) {
            if (classAnnotation instanceof RequestMapping) {
                RequestMapping annotation = (RequestMapping) classAnnotation;
                String classUrlMapping = annotation.value()[0];
                url = classUrlMapping.concat(url);
            }
        }
        return url;
    }
}
