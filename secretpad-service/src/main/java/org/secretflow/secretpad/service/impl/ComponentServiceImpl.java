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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.GraphErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.FileUtils;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.service.ComponentService;
import org.secretflow.secretpad.service.constant.ComponentConstants;
import org.secretflow.secretpad.service.model.graph.CompListVO;
import org.secretflow.secretpad.service.model.graph.ComponentKey;
import org.secretflow.secretpad.service.model.graph.ComponentSummaryDef;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;

import org.secretflow.proto.component.Comp;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Component service implementation class
 *
 * @author yansi
 * @date 2023/6/7
 */
@Service
public class ComponentServiceImpl implements ComponentService {

    @Value("${component.i18n.location:./config/i18n}")
    private String i18nLocation;

    @Autowired
    private List<Comp.CompListDef> components;

    @Override
    public CompListVO listComponents() {
        CompListVO compListVO = CompListVO.builder().name("secretflow").comps(new ArrayList<>()).build();
        components.stream().forEach(compListDef -> {
            List<Comp.ComponentDef> comps = compListDef.getCompsList();
            if (!CollectionUtils.isEmpty(comps)) {
                compListVO.getComps().addAll(comps.stream().map(componentDef ->
                                ComponentSummaryDef.builder()
                                        .domain(componentDef.getDomain())
                                        .name(componentDef.getName())
                                        .version(componentDef.getVersion())
                                        .desc(componentDef.getDesc())
                                        .build())
                        .collect(Collectors.toList()));
            }
        });
        return compListVO;
    }

    @Override
    public Comp.ComponentDef getComponent(ComponentKey key) {
        return batchGetComponent(List.of(key)).get(0);
    }

    @Override
    public List<Comp.ComponentDef> batchGetComponent(List<ComponentKey> keys) {
        List<Comp.ComponentDef> result = new ArrayList<>();
        Map<ComponentKey, Comp.ComponentDef> componentMap = new HashMap<>();
        components.stream().filter(compListDef -> !CollectionUtils.isEmpty(compListDef.getCompsList())).forEach(compListDef -> {
            compListDef.getCompsList().stream().forEach(componentDef -> {
                componentMap.put(new ComponentKey(componentDef.getDomain(), componentDef.getName()), componentDef);
            });
        });
        if (!CollectionUtils.isEmpty(keys)) {
            keys.stream().forEach(key -> {
                if (!componentMap.containsKey(key)) {
                    throw SecretpadException.of(GraphErrorCode.COMPONENT_NOT_EXISTS, key.toString());
                }
                result.add(componentMap.get(key));
            });
        }
        return result;
    }

    @Override
    public Object listComponentI18n() {
        Map<String, Object> config = new HashMap<>();
        try {
            File dir = ResourceUtils.getFile(i18nLocation);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String str = FileUtils.readFile2String(file);
                    Map<String, Object> content = JsonUtils.toJavaMap(str, Object.class);
                    if (!CollectionUtils.isEmpty(content)) {
                        config.putAll(content);
                    }
                }
            }
        } catch (IOException e) {
            throw SecretpadException.of(GraphErrorCode.COMPONENT_18N_ERROR, e);
        }
        return config;
    }

    @Override
    public boolean isSecretpadComponent(GraphNodeInfo node) {
        Pipeline.NodeDef pipelineNodeDef;
        if (node.getNodeDef() instanceof Pipeline.NodeDef) {
            pipelineNodeDef = (Pipeline.NodeDef) node.getNodeDef();
        } else {
            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            pipelineNodeDef = (Pipeline.NodeDef) ProtoUtils.fromJsonString(JsonUtils.toJSONString(node.getNodeDef()), nodeDefBuilder);
        }
        String domain = pipelineNodeDef.getDomain();
        String name = pipelineNodeDef.getName();
        return ComponentConstants.READ_DATA.equals(domain) && ComponentConstants.DATA_TABLE.equals(name);
    }

}
