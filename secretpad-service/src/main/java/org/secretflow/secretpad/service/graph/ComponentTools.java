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

import org.secretflow.secretpad.common.constant.ComponentConstants;
import org.secretflow.secretpad.common.util.ProtoUtils;
import org.secretflow.secretpad.service.model.graph.GraphNodeInfo;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.IndividualTable;
import lombok.extern.slf4j.Slf4j;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Component tools
 *
 * @author yansi
 * @date 2023/6/6
 */
@Slf4j
public class ComponentTools {

    /**
     *  background: check uniform or self-define node description
     *
     *  make sure nodeDef is not null
     *  common use like :
     *  GraphNodeInfo graphNodeInfo = task.getNode();
     *  Object nodeDef = graphNodeInfo.getNodeDef();
     */
    public static Pipeline.NodeDef getNodeDef(Object nodeDef){
        Pipeline.NodeDef pipelineNodeDef;
        if(nodeDef instanceof Pipeline.NodeDef){
            pipelineNodeDef = (Pipeline.NodeDef) nodeDef;
        } else{
            Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
            pipelineNodeDef = (Pipeline.NodeDef) ProtoUtils.fromObject(nodeDef, nodeDefBuilder);
        }
        return pipelineNodeDef;
    }

    /**
     * Get datatableId from graph node information
     *
     * @param nodeInfo graph node information
     * @return datatableId
     */
    public static String getDataTableId(GraphNodeInfo nodeInfo) {
        Pipeline.NodeDef nodeDef = getNodeDef(nodeInfo.getNodeDef());
        List<Struct> attrsList = nodeDef.getAttrsList();
        String tableId = "";
        if (!CollectionUtils.isEmpty(attrsList)) {
            tableId = attrsList.get(0).getFieldsOrDefault(ComponentConstants.ATTRIBUTE_S, Value.newBuilder().build()).getStringValue();
        }
        return tableId;
    }

    /**now  only for unbalance psi */
    public static String getHiddenPartyId(Object nodeDefObj) {
        Pipeline.NodeDef nodeDef = getNodeDef(nodeDefObj);
        List<Struct> attrsList = nodeDef.getAttrsList();
        String partyId = "";
        if (!CollectionUtils.isEmpty(attrsList)) {
            partyId = attrsList.get(1).getFieldsOrDefault(ComponentConstants.ATTRIBUTE_SS, Value.newBuilder().build()).getListValue().getValues(0).getStringValue();
        }
        return partyId;
    }


    public static String getDataTablePartition(GraphNodeInfo nodeInfo) {
        Pipeline.NodeDef nodeDef = getNodeDef(nodeInfo.getNodeDef());
        List<Struct> attrsList = nodeDef.getAttrsList();
        String datatable_partition = "";
        if (!CollectionUtils.isEmpty(attrsList) && attrsList.size() == 2) {
            datatable_partition = attrsList.get(1).getFieldsOrDefault(ComponentConstants.ATTRIBUTE_S, Value.newBuilder().setStringValue("").build()).getStringValue();
        }
        return datatable_partition;
    }

    /**
     * cover
     * @return nodeDef
     */
    public static Pipeline.NodeDef coverAttrByCustomAttr(GraphNodeInfo graphNodeInfo) {
        Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
        ProtoUtils.fromObject(graphNodeInfo.getNodeDef(), nodeDefBuilder);
        Pipeline.NodeDef nodeDef = nodeDefBuilder.build();

        List<Struct> attrsList = new ArrayList<>(nodeDef.getAttrsList());
        if (!attrsList.isEmpty()) {
            for (int i = 0; i < attrsList.size(); i++) {
                Struct struct = attrsList.get(i);
                if (struct.containsFields(ComponentConstants.CUSTOM_PROTOBUF_CLS)) {
                    JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder().add(IndividualTable.getDescriptor()).build();
                    String custom_value = ProtoUtils.toJsonString(struct.getFieldsOrThrow(ComponentConstants.CUSTOM_VALUE).getStructValue(), typeRegistry);
                    Struct s = Struct.newBuilder()
                            .putFields(
                                    ComponentConstants.ATTRIBUTE_S,
                                    Value.newBuilder().setStringValue(custom_value).build())
                            .build();
                    attrsList.set(i, s);
                }
            }
        }
        nodeDef = nodeDef.toBuilder().clearAttrs().addAllAttrs(attrsList).build();
        return nodeDef;
    }
}
