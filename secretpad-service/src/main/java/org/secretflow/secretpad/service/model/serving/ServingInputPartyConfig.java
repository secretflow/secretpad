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

package org.secretflow.secretpad.service.model.serving;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author chenmingliang
 * @date 2024/01/22
 */
@Getter
@Setter
public class ServingInputPartyConfig {

    private Map<String, PartyConfig> partyConfigs;

    @Getter
    @Setter
    public static class PartyConfig {
        private ServingConfig servingConfig;

        private ModelConfig modelConfig;

        private ChannelDesc channelDesc;

        private FeatureSourceConfig featureSourceConfig;

        @Getter
        @Setter
        public static class ServingConfig {
            private Map<String, String> featureMapping;
        }

        @Getter
        @Setter
        public static class ModelConfig {

            private String modelId;

            private String basePath;

            private String sourcePath;

            private String sourceType;

        }

        @Getter
        @Setter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class FeatureSourceConfig {

            private Map<String, Object> httpOpts;

            private Map<String, Object> mockOpts;
        }

        @Getter
        @Setter
        public static class ChannelDesc {
            private String protocol = "http";
        }
    }
}
