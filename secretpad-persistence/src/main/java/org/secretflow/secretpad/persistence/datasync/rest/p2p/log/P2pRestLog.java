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

package org.secretflow.secretpad.persistence.datasync.rest.p2p.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

/**
 * @author yutu
 * @date 2024/12/02
 */
@Slf4j
public class P2pRestLog {
    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.debug("Request headers {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response status: {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) -> values.forEach(value -> log.debug("Response headers {}={}", name, value)));
            return Mono.just(clientResponse);
        });
    }
}