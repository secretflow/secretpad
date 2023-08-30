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

package org.secretflow.secretpad.common.util;

import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Proto utils test
 *
 * @author yansi
 * @date 2023/6/1
 */
public class ProtoUtilsTest {
    @Test
    public void testStringConvert() throws IOException {
        String str = "{\"attr_paths\":[\"protocol\",\"receiver\",\"precheck_input\",\"sort\",\"broadcast_result\",\"bucket_size\",\"curve_type\",\"input/receiver_input/key\",\"input/sender_input/key\"],\"attrs\":[{\"s\":\"ECDH_PSI_2PC\"},{\"s\":\"alice\"},{\"b\":true},{\"b\":true},{\"b\":true},{\"i64\":1048576},{\"s\":\"CURVE_FOURQ\"},{\"ss\":[\"id1\"]},{\"ss\":[\"id2\"]}],\"domain\":\"psi\",\"inputs\":[\"alice_input\",\"bob_input\"],\"name\":\"two_party_balanced_psi\",\"outputs\":[\"psi_output\"],\"version\":\"0.0.1\"}";
        Message message = ProtoUtils.fromJsonString(str);
    }
}
