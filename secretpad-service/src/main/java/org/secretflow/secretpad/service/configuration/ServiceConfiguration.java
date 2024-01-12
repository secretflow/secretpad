/*
 * Copyright 2023 Ant Group Co., Ltd.
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

package org.secretflow.secretpad.service.configuration;

import org.secretflow.secretpad.service.enums.VoteTypeEnum;
import org.secretflow.secretpad.service.factory.JsonProtobufSourceFactory;
import org.secretflow.secretpad.service.graph.JobChain;
import org.secretflow.secretpad.service.graph.chain.AbstractJobHandler;
import org.secretflow.secretpad.service.handler.VoteTypeHandler;

import com.secretflow.spec.v1.CompListDef;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for service layer
 *
 * @author yansi
 * @date 2023/5/30
 */
@Configuration
public class ServiceConfiguration {
    /**
     * Load components from locations
     *
     * @return component list
     * @throws IOException
     */
    @Bean
    List<CompListDef> components(@Value("${component.spec.location:./config/components}") String componentLocation) throws IOException {
        JsonProtobufSourceFactory factory = new JsonProtobufSourceFactory(new String[]{componentLocation});
        return factory.load();
    }

    /**
     * Job chain for all job handlers
     *
     * @param jobHandlers all job handlers
     * @return job chain
     */
    @Bean
    JobChain jobChain(List<AbstractJobHandler> jobHandlers) {
        return new JobChain<>(jobHandlers);
    }

    @Bean
    public Map<VoteTypeEnum, VoteTypeHandler> voteTypeHandlerMap(List<VoteTypeHandler> voteTypeHandlers) {
        Map<VoteTypeEnum, VoteTypeHandler> voteTypeHandlerMap = new HashMap<>();
        voteTypeHandlers.forEach(handler -> {
            List<VoteTypeEnum> voteTypeEnums = handler.supportTypes();
            if (!CollectionUtils.isEmpty(voteTypeEnums)) {
                voteTypeEnums.forEach(enm -> voteTypeHandlerMap.put(enm, handler));
            }
        });
        return voteTypeHandlerMap;
    }
}
