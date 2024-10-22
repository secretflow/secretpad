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

package org.secretflow.secretpad.common.util;

import org.secretflow.secretpad.common.dto.KusciaResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author chenmingliang
 * @date 2024/07/04
 */
@Component
@Slf4j
public class AsyncTaskExecutionUtils {

    private static final int DEFAULT_SINGLE_TASK_TIME_OUT = 5000;
    private static final TimeUnit DEFAULT_TIME_OUT_UNIT = TimeUnit.MILLISECONDS;
    private static Executor kusciaApiFutureTaskThreadPool;

    public static <Request, Response> CompletableFuture<KusciaResponse<Response>> executeDecoratedOperation(
            Request request,
            Function<Request, KusciaResponse<Response>> asyncOperation,
            String nodeId,
            Map<String, String> failedRecords) {

        return CompletableFuture.supplyAsync(() -> asyncOperation.apply(request), kusciaApiFutureTaskThreadPool)
                .completeOnTimeout(null, DEFAULT_SINGLE_TASK_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .handle((response, ex) -> {
                    if (ex != null) {
                        String errorMsg = String.format("nodeId:%s execute async operation failed, error:%s", nodeId, ex.getMessage());
                        log.error("nodeId:{} execute async operation failed, error:{}", nodeId, ex.getMessage(), ex);
                        failedRecords.put(nodeId, errorMsg);
                        return KusciaResponse.of(null, nodeId);
                    } else if (response == null) {
                        log.info("operation timeout, nodeId: {}", nodeId);
                        failedRecords.put(nodeId, "Timeout");
                        return KusciaResponse.of(null, nodeId);
                    }
                    return response;
                });
    }

    public static <Request, Response> CompletableFuture<Response> executeUnDecoratedOperation(
            Request request,
            Function<Request, Response> asyncOperation,
            String nodeId,
            Map<String, String> failedRecords) {

        return CompletableFuture.supplyAsync(() -> asyncOperation.apply(request), kusciaApiFutureTaskThreadPool)
                .completeOnTimeout(null, DEFAULT_SINGLE_TASK_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .handle((response, ex) -> {
                    if (ex != null) {
                        String errorMsg = String.format("nodeId:%s execute async operation failed, error:%s", nodeId, ex.getMessage());
                        log.error("nodeId:{} execute async operation failed, error:{}", nodeId, ex.getMessage(), ex);
                        failedRecords.put(nodeId, errorMsg);
                        return null;
                    } else if (response == null) {
                        log.info("operation timeout, nodeId: {}", nodeId);
                        failedRecords.put(nodeId, "Timeout");
                        return null;
                    }
                    return response;
                });
    }

    public static <Request> CompletableFuture<Void> executeUnDecoratedOperation(
            Request request,
            Consumer<Request> asyncOperation,
            String nodeId,
            Map<String, String> failedRecords) {

        return CompletableFuture.runAsync(() -> asyncOperation.accept(request), kusciaApiFutureTaskThreadPool)
                .completeOnTimeout(null, DEFAULT_SINGLE_TASK_TIME_OUT, DEFAULT_TIME_OUT_UNIT)
                .handle((Void unused, Throwable ex) -> {
                    if (ex != null) {
                        String errorMsg = String.format("nodeId:%s execute async operation failed, error:%s", nodeId, ex.getMessage());
                        log.error("nodeId:{} execute async operation failed, error:{}", nodeId, ex.getMessage(), ex);
                        failedRecords.put(nodeId, errorMsg);
                    }
                    return null;
                });
    }

    @Autowired
    public void setRestTemplate(Executor kusciaApiFutureTaskThreadPool) {
        AsyncTaskExecutionUtils.kusciaApiFutureTaskThreadPool = kusciaApiFutureTaskThreadPool;
    }
}
