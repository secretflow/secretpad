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

package org.secretflow.secretpad.service.graph;

import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;

import com.secretflow.spec.v1.Attribute;
import org.secretflow.proto.pipeline.Pipeline;

import java.util.List;

/**
 * Component tools
 *
 * @author yansi
 * @date 2023/6/6
 */
public class ComponentTools {
    /**
     * Get datatableId from graph node information
     *
     * @param nodeInfo graph node information
     * @return datatableId
     */
    public static String getDataTableId(GraphNodeInfo nodeInfo) {
        Pipeline.NodeDef nodeDef;
        if (nodeInfo.getNodeDef() instanceof Pipeline.NodeDef) {
            nodeDef = (Pipeline.NodeDef) nodeInfo.getNodeDef();
        } else {
            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            nodeDef = (Pipeline.NodeDef) ProtoUtils.fromObject(nodeInfo.getNodeDef(), nodeDefBuilder);
        }
        List<Attribute> attributes = nodeDef.getAttrsList();
        String tableId = "";
        if (!attributes.isEmpty()) {
            tableId = attributes.get(0).getS();
        }
        return tableId;
    }
}
