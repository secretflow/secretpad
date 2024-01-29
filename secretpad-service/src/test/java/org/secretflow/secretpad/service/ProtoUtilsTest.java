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
import org.secretflow.secretpad.common.util.UniqueLinkedBlockingQueue;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.secretflow.spec.v1.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.secretflow.proto.pipeline.Pipeline;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * ProtoUtils test
 *
 * @author yansi
 * @date 2023/6/1
 */
@Slf4j
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
        Table.HeaderItem headerItem = Table.HeaderItem.newBuilder()
                .setName("table_column_name")
                .setType(String.valueOf(AttrType.AT_STRING))
                .build();
        Attribute attribute = Attribute.newBuilder()
                .setS("id1")
                .build();
        Table.Row row = Table.Row.newBuilder()
                .addItems(0, attribute)
                .setName("table_column_name")
                .build();
        Table table = Table.newBuilder()
                .addHeaders(0, headerItem)
                .addRows(0, row)
                .build();
        Div.Child child = Div.Child.newBuilder()
                .setType("table")
                .setTable(table)
                .build();
        Div div = Div.newBuilder()
                .addChildren(0, child)
                .build();
        Tab tab = Tab.newBuilder()
                .addDivs(0, div)
                .build();
        Report report = Report.newBuilder()
                .setName("test")
                .setDesc("desc")
                .addTabs(0, tab)
                .build();
        DistData distData = DistData.newBuilder()
                .setType("sf.report")
                .setName("nkdz-ynshnjb3-node-4-output-0")
                .setMeta(Any.pack(report))
                .build();

        JsonFormat.TypeRegistry typeRegistry = JsonFormat.TypeRegistry.newBuilder()
                .add(Report.getDescriptor()).build();
        String content = ProtoUtils.toJsonString(distData, typeRegistry);
        DistData.Builder distDataBuilder = DistData.newBuilder();
        JsonFormat.parser().ignoringUnknownFields().usingTypeRegistry(typeRegistry).merge(content, distDataBuilder);
        System.out.println(distDataBuilder.build().getMeta());
    }

    @Test
    @Disabled
    public void showSyncFileData() throws IOException {
        serializableRead("li-test");
    }

    public UniqueLinkedBlockingQueue serializableRead(String nodeId) throws IOException {
        ObjectInputStream in = null;
        UniqueLinkedBlockingQueue queue = new UniqueLinkedBlockingQueue();
        File file = ResourceUtils.getFile("/Users/zhangwenxu/cx/" + nodeId);
        if (!file.exists()) {
            return null;
        }
        try {
            in = new ObjectInputStream(new FileInputStream(file));
            queue = (UniqueLinkedBlockingQueue) in.readObject();
        } catch (Exception e) {
            log.error("serializableRead error ", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        log.info("serializableRead ---{} {}", nodeId, queue.size());
        return queue;
    }
}
