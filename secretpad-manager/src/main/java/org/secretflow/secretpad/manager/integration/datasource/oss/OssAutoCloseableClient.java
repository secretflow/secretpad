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

package org.secretflow.secretpad.manager.integration.datasource.oss;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * @author chenmingliang
 * @date 2024/05/27
 */
public final class OssAutoCloseableClient implements AutoCloseable {

    private static final ClientConfiguration
            clientConfiguration = new ClientConfiguration().withConnectionTimeout(500)
            .withProtocol(Protocol.HTTP)
            .withSocketTimeout(500);
    private final AmazonS3 amazonS3Client;

    private OssAutoCloseableClient(AmazonS3 amazonS3Client) {
        this.amazonS3Client = amazonS3Client;
    }

    public static OssAutoCloseableClient createClient(AwsOssConfig config) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(config.getAccessKeyId(), config.getSecretAccessKey())))
                .withClientConfiguration(clientConfiguration)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        config.getEndpoint(),
                        ""))
                .withPathStyleAccessEnabled(false)
                .withChunkedEncodingDisabled(true)
                .build();
        return new OssAutoCloseableClient(s3Client);
    }

    @Override
    public void close() throws Exception {
        if (amazonS3Client != null) {
            amazonS3Client.shutdown();
        }
    }

    public boolean doesBucketExistV2(String bucketName) {
        return amazonS3Client.doesBucketExistV2(bucketName);
    }

    public boolean doesObjectExist(String bucketName, String key) {
        return amazonS3Client.doesObjectExist(bucketName, key);
    }

}
