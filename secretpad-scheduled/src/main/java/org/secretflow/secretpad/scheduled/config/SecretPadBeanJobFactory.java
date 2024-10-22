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

package org.secretflow.secretpad.scheduled.config;

import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

/**
 * @author yutu
 * @date 2024/08/30
 */
@Component
public class SecretPadBeanJobFactory extends SpringBeanJobFactory {

    @Resource
    private ApplicationContext applicationContext;

    @NotNull
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) {
        return applicationContext.getBean(bundle.getJobDetail().getJobClass());
    }
}