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

package org.secretflow.secretpad.service.test;


import org.secretflow.secretpad.manager.integration.datasource.oss.AwsOssConfig;
import org.secretflow.secretpad.manager.integration.datasource.oss.OssAutoCloseableClient;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author chenmingliang
 * @date 2024/05/30
 */
public class OssAutoCloseableClientTest {

    @Test
    void testOssAutoCloseableClient() {
        OssAutoCloseableClient.createClient(AwsOssConfig.builder().endpoint("endpoint").accessKeyId("ak").secretAccessKey("sk").build());
        OssAutoCloseableClient mockClient = Mockito.mock(OssAutoCloseableClient.class);
        when(mockClient.doesBucketExistV2(anyString())).thenReturn(true);
        when(mockClient.doesObjectExist(anyString(), anyString())).thenReturn(true);
        mockClient.doesBucketExistV2("any");
        mockClient.doesObjectExist("any", "any");
        verify(mockClient).doesObjectExist(anyString(), anyString());
        verify(mockClient).doesBucketExistV2(anyString());
    }
}
