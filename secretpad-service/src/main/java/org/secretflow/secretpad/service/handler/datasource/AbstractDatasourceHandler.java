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

package org.secretflow.secretpad.service.handler.datasource;

import org.secretflow.secretpad.common.dto.KusciaResponse;
import org.secretflow.secretpad.common.errorcode.ConcurrentErrorCode;
import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.service.model.datasource.CreateDatasourceRequest;
import org.secretflow.secretpad.service.model.datasource.CreateDatasourceVO;
import org.secretflow.secretpad.service.model.datasource.DeleteDatasourceRequest;
import org.secretflow.secretpad.service.util.HttpUtils;

import org.secretflow.v1alpha1.kusciaapi.Domaindatasource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author chenmingliang
 * @date 2024/05/31
 */
public abstract class AbstractDatasourceHandler implements DatasourceHandler {


    @Override
    public CreateDatasourceVO createDatasource(CreateDatasourceRequest createDatasourceRequest) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public void deleteDatasource(DeleteDatasourceRequest deleteDatasourceRequest) {
        throw new UnsupportedOperationException("unsupported operation");
    }

    public void serviceCheck(String endpoint) {
        if (!HttpUtils.detection(endpoint)) {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_ENDPOINT_CONNECT_FAIL);
        }
    }

    public void fetchResult(Map<String, String> failedDatasource, List<CompletableFuture<KusciaResponse<Domaindatasource.CreateDomainDataSourceResponse>>> completableFutures) {
        try {
            CompletableFuture
                    .allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
                    .get(5000, TimeUnit.MILLISECONDS);
            for (CompletableFuture<KusciaResponse<Domaindatasource.CreateDomainDataSourceResponse>> task : completableFutures) {

                KusciaResponse<Domaindatasource.CreateDomainDataSourceResponse> taskNow = task.get();
                if (taskNow.getData() == null && !failedDatasource.containsKey(taskNow.getNodeId())) {
                    failedDatasource.put(task.get().getNodeId(), "task failed or timeout");
                }
                //success to the async operation,but kuscia status is not success
                if (taskNow.getData().getStatus().getCode() != 0) {
                    failedDatasource.put(taskNow.getNodeId(), taskNow.getData().getStatus().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw SecretpadException.of(ConcurrentErrorCode.TASK_INTERRUPTED_ERROR, e);
        } catch (ExecutionException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_EXECUTION_ERROR, e);
        } catch (TimeoutException e) {
            throw SecretpadException.of(ConcurrentErrorCode.TASK_TIME_OUT_ERROR, e);
        }
    }
}
