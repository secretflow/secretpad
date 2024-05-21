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

package org.secretflow.secretpad.manager.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * @author guanxi
 * @date 2024/4/28
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PartyDTO {

    /**
     * Domain ID
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * App image
     */
    @JsonProperty("app_image")
    private String appImage;

    /**
     * Resource list
     */
    @JsonProperty(value = "resources", required = true)
    private List<Resource> resources;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Resource {
        /**
         * Min Cpu
         */
        @JsonProperty("min_cpu")
        private String minCPU;

        /**
         * Max Cpu
         */
        @JsonProperty("max_cpu")
        private String maxCPU;

        /**
         * Min Memory
         */
        @JsonProperty("min_memory")
        private String minMemory;

        /**
         * Max Memory
         */
        @JsonProperty("max_memory")
        private String maxMemory;
    }


}
