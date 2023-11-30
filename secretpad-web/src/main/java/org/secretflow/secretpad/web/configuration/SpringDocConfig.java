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

import org.secretflow.secretpad.web.constant.AuthConstants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * @author yutu
 * @date 2023/08/08
 * @see <a href="https://swagger.io/docs/specification/authentication/cookie-authentication/"/>
 */
@OpenAPIDefinition(
        info = @Info(title = "Secretpad Restful API", version = "0.0.1-SNAPSHOT", license = @License(name = "Apache 2.0")),
        security = @SecurityRequirement(name = AuthConstants.TOKEN_NAME))
@SecurityScheme(name = AuthConstants.TOKEN_NAME, type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.COOKIE)
@Configuration
public class SpringDocConfig {
}