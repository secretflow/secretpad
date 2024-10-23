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

package org.secretflow.secretpad.service.impl;

import org.secretflow.secretpad.common.errorcode.DatasourceErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.manager.integration.datasource.oss.AwsOssConfig;
import org.secretflow.secretpad.manager.integration.datasource.oss.OssClientFactory;
import org.secretflow.secretpad.service.OssService;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * @author chenmingliang
 * @date 2024/06/03
 */

@Service
@AllArgsConstructor
@Slf4j
public class OssServiceImpl implements OssService {

    private final OssClientFactory ossClientFactory;

    @Override
    public void checkBucketExists(AwsOssConfig awsOssConfig, String bucketName) {
        try (var s3Client = ossClientFactory.getOssClient(buildAwsOssConfig(awsOssConfig))) {
            //try to check bucket exist
            log.info("start check bucket");
            if (!s3Client.doesBucketExistV2(bucketName)) {
                throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_BUCKET_NOT_EXIST);
            }
        } catch (AmazonS3Exception e) {
            log.error("There was an error response from S3. This might indicate that the credentials are invalid.");
            log.error("Error Message:    " + e.getErrorResponseXml());
            if ("PermanentRedirect".equalsIgnoreCase(e.getErrorCode())) {
                throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_BUCKET_NOT_MATCH_ENDPOINT, e);
            }
            if ("InvalidAccessKeyId".equalsIgnoreCase(e.getErrorCode()) || "SignatureDoesNotMatch".equalsIgnoreCase(e.getErrorCode())) {
                throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_CREDENTIALS_INVALID, e);
            }
            if (HttpStatus.BAD_REQUEST.value() == e.getStatusCode()) {
                throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_ENDPOINT_API_PORT_ERROR, e);
            }
            throw SecretpadException.of(DatasourceErrorCode.DATASOURCE_UNKNOWN_EXCEPTION, e);
        } catch (SecretpadException e) {
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_BUCKET_NOT_EXIST, e);
        } catch (Exception e) {
            log.error("Unknown exception", e);
            throw SecretpadException.of(DatasourceErrorCode.DATA_SOURCE_CREATE_FAIL, e);
        }
    }


    @Override
    public boolean checkObjectExists(AwsOssConfig awsOssConfig, String bucketName, String objectKey) {
        try (var s3Client = ossClientFactory.getOssClient(buildAwsOssConfig(awsOssConfig))) {
            //try to check bucket exist
            log.info("start check objectKey");
            return s3Client.doesObjectExist(bucketName, objectKey);
        } catch (Exception e) {
            log.error("Unknown exception", e);
            return false;
        }
    }


    private AwsOssConfig buildAwsOssConfig(AwsOssConfig awsOssConfig) {
        return AwsOssConfig.builder()
                .accessKeyId(awsOssConfig.getAccessKeyId())
                .secretAccessKey(awsOssConfig.getSecretAccessKey())
                .endpoint(awsOssConfig.getEndpoint())
                .build();
    }
}
