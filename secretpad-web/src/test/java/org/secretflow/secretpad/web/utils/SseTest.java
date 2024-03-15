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

package org.secretflow.secretpad.web.utils;

import org.secretflow.secretpad.web.controller.ControllerTest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * @author yutu
 * @date 2024/03/03
 */
@Slf4j
public class SseTest extends ControllerTest {


    void testSse() {

        WebClient webClient = WebClient.create("http://localhost:8080");

        Flux<ServerSentEvent<String>> eventStream = webClient.get()
                .uri("/sync?p=1")
                .header("kuscia-origin-source", "alice")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {
                });

        eventStream.subscribe(
                event -> System.out.println("Received SSE: " + event.data()),
                error -> System.out.println("Error receiving SSE: " + error),
                () -> System.out.println("SSE stream completed")
        );

    }
}