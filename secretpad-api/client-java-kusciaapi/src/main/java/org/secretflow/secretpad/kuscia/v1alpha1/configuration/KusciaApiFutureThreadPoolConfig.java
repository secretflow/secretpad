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

package org.secretflow.secretpad.kuscia.v1alpha1.configuration;

import org.secretflow.secretpad.common.dto.UserContextDTO;
import org.secretflow.secretpad.common.util.UserContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author yutu
 * @date 2024/06/24
 */
@Configuration
@Slf4j
public class KusciaApiFutureThreadPoolConfig {

    /*@Bean(name = "kusciaApiFutureThreadPool")
    public Executor kusciaApiFutureThreadPool() {
        int corePoolSize = 10;
        int maxPoolSize = 20;
        long keepAliveTime = 60;
        int queueCapacity = 100;

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }*/

    @Bean(name = "kusciaApiFutureTaskThreadPool")
    public ThreadPoolTaskExecutor kusciaApiFutureTaskThreadPool() {
        int corePoolSize = 10;
        int maxPoolSize = 20;
        int keepAliveTime = 60;
        int queueCapacity = 100;

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveTime);
        executor.setThreadFactory(Executors.defaultThreadFactory());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setTaskDecorator(userContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskDecorator userContextTaskDecorator() {
        return runnable -> {
            UserContextDTO user = UserContext.getUserOrNotExist();
            if (user == null) {
                log.info("KusciaApiFutureThreadPoolConfig user is null");
                return runnable;
            }
            return () -> {
                try {
                    UserContext.setBaseUser(user);

                    runnable.run();
                } finally {
                    UserContext.remove();
                }
            };
        };
    }

}