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

package org.secretflow.secretpad.service.model.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author chenmingliang
 * @date 2024/01/22
 */
@Getter
@Setter
public class CreateModelServingRequest {

    @NotBlank
    private String modelId;

    @NotBlank
    private String projectId;

    private @Valid List<PartyConfig> partyConfigs;

    @Setter
    @Getter
    public static class PartyConfig {

        @NotBlank
        private String nodeId;

        @NotBlank
        private String featureTableId;

        @NotNull
        private Boolean isMock;
        List<Feature> features;

        @Getter
        @Setter
        public static class Feature {
            String offlineName;

            String onlineName;
        }

    }
}
