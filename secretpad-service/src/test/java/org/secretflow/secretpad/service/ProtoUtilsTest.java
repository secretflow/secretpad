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

package org.secretflow.secretpad.service;

import org.secretflow.secretpad.common.util.ProtoUtils;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;
import org.secretflow.proto.component.Comp;
import org.secretflow.proto.component.Data;
import org.secretflow.proto.component.ReportOuterClass;
import org.secretflow.proto.pipeline.Pipeline;

import java.io.IOException;

/**
 * ProtoUtils test
 *
 * @author yansi
 * @date 2023/6/1
 */
public class ProtoUtilsTest {
    @Test
    public void testStringConvert() throws IOException {
        String str = "{\"attr_paths\":[\"protocol\",\"receiver\",\"precheck_input\",\"sort\",\"broadcast_result\",\"bucket_size\",\"curve_type\",\"input/receiver_input/key\",\"input/sender_input/key\"],\"attrs\":[{\"s\":\"ECDH_PSI_2PC\"},{\"s\":\"alice\"},{\"b\":true},{\"b\":true},{\"b\":true},{\"i64\":1048576},{\"s\":\"CURVE_FOURQ\"},{\"ss\":[\"id1\"]},{\"ss\":[\"id2\"]}],\"domain\":\"psi\",\"name\":\"two_party_balanced_psi\",\"version\":\"0.0.1\"}";
        Pipeline.NodeDef.Builder nodeDefBuilder = Pipeline.NodeDef.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().merge(str, nodeDefBuilder);
        Pipeline.NodeDef nodeDef = nodeDefBuilder.build();
    }

    @Test
    public void testPrint() throws InvalidProtocolBufferException {
        ReportOuterClass.Table.HeaderItem headerItem = ReportOuterClass.Table.HeaderItem.newBuilder()
                .setName("table_column_name")
                .setType(Comp.AttrType.AT_STRING)
                .build();
        Comp.Attribute attribute = Comp.Attribute.newBuilder()
                .setS("id1")
                .build();
        ReportOuterClass.Table.Row row = ReportOuterClass.Table.Row.newBuilder()
                .addItems(0, attribute)
                .setName("table_column_name")
                .build();
        ReportOuterClass.Table table = ReportOuterClass.Table.newBuilder()
                .addHeaders(0, headerItem)
                .addRows(0, row)
                .build();
        ReportOuterClass.Div.Child child = ReportOuterClass.Div.Child.newBuilder()
                .setType("table")
                .setTable(table)
                .build();
        ReportOuterClass.Div div = ReportOuterClass.Div.newBuilder()
                .addChildren(0, child)
                .build();
        ReportOuterClass.Tab tab = ReportOuterClass.Tab.newBuilder()
                .addDivs(0, div)
                .build();
        ReportOuterClass.Report report = ReportOuterClass.Report.newBuilder()
                .setName("test")
                .setDesc("desc")
                .addTabs(0, tab)
                .build();
        Data.DistData distData = Data.DistData.newBuilder()
                .setType("sf.report")
                .setName("nkdz-ynshnjb3-node-4-output-0")
                .setMeta(Any.pack(report))
                .build();

        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder()
                .add(ReportOuterClass.Report.getDescriptor()).build();
        String content = ProtoUtils.toJsonString(distData, typeRegistry);
        Data.DistData.Builder distDataBuilder = Data.DistData.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().usingTypeRegistry(typeRegistry).merge(content, distDataBuilder);
        System.out.println(distDataBuilder.build().getMeta());
    }
}
