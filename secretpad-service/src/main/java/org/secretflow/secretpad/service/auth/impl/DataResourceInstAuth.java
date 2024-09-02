/*
 * Copyright 2024 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.service.auth.impl;

import org.secretflow.secretpad.service.InstService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author chenmingliang
 * @date 2024/07/01
 */
@Component
@Slf4j
public class DataResourceInstAuth {

    @Resource
    InstService instService;

    public boolean check(String instId, String resourceId) {
        log.info("isntId:{},resourceId:{}", instId, resourceId);
        return instService.checkNodeInInst(instId, resourceId);
    }

}
