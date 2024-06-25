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

package org.secretflow.secretpad.web.init;

import org.secretflow.secretpad.web.init.config.UnregisterMappingConfig;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

/**
 * @author yutu
 * @date 2023/12/25
 */
@RequiredArgsConstructor
@Component
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class DynamicBeanRegisterInit implements ApplicationRunner {

    private final UnregisterMappingConfig unregisterMappingConfig;
    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<RequestMappingInfo> mappings = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .toList();
        log.info("all mvc mapping {}", mappings);
        List<String> path = unregisterMappingConfig.getPath();
        if (!CollectionUtils.isEmpty(path)) {
            for (String p : path) {
                mappings.forEach(m -> {
                    if (m.getDirectPaths().contains(p)) {
                        log.warn("{} this mvc mapping will be unregister", m);
                        requestMappingHandlerMapping.unregisterMapping(m);
                    }
                });
            }
        }
        mappings = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .toList();
        log.info("after unregister all mvc mapping {}", mappings);
    }
}