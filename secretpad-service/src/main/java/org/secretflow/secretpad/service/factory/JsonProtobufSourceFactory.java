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

package org.secretflow.secretpad.service.factory;

import org.secretflow.secretpad.service.constant.ComponentConstants;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.CompListDef;
import com.secretflow.spec.v1.ComponentDef;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Json protobuf source factory
 *
 * @author yansi
 * @date 2023/5/30
 */
public class JsonProtobufSourceFactory {
    private final String[] locations;

    public JsonProtobufSourceFactory(String[] locations) {
        this.locations = locations;
    }

    /**
     * Load components from locations
     *
     * @return component list
     * @throws IOException
     */
    public List<CompListDef> load() throws IOException {
        if (locations == null || locations.length <= 0) {
            throw new IllegalArgumentException("locations can not be null or empty");
        }
        List<CompListDef> items = new ArrayList<>();
        List<CompListDef> resp = new ArrayList<>();
        List<ComponentDef> secretpad = new ArrayList<>();
        List<ComponentDef> secretpad_tee = new ArrayList<>();
        List<ComponentDef> scql = new ArrayList<>();
        for (String location : locations) {
            File dir = ResourceUtils.getFile(location);
            File[] files = dir.listFiles();
            assert files != null;
            for (File file : files) {
                Message.Builder itemBuilder = CompListDef.newBuilder();
                JsonFormat.parser().ignoringUnknownFields().merge(new FileReader(file), itemBuilder);
                CompListDef compListDef = (CompListDef) itemBuilder.build();
                items.add(compListDef);
                if (compListDef.getName().equals(ComponentConstants.SECRETPAD)) {
                    secretpad = compListDef.getCompsList();
                }
                if (compListDef.getName().equals(ComponentConstants.SECRETPAD_TEE)) {
                    secretpad_tee = compListDef.getCompsList();
                }
                if (compListDef.getName().equals(ComponentConstants.SCQL)) {
                    scql = compListDef.getCompsList();
                }
            }
        }
        for (CompListDef item : items) {
            String name = item.getName();
            switch (name) {
                case ComponentConstants.SECRETPAD, ComponentConstants.SECRETPAD_TEE, ComponentConstants.SCQL:
                    resp.add(item);
                    break;
                case ComponentConstants.TRUSTEDFLOW:
                    resp.add(item.toBuilder().addAllComps(secretpad_tee).build());
                    break;
                case ComponentConstants.SECRETFLOW:
                    resp.add(item.toBuilder()
                            .addAllComps(secretpad)
                            .addAllComps(scql)
                            .build());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown component name: " + name);
            }
        }
        return resp;
    }
}
