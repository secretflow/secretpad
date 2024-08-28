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

package org.secretflow.secretpad.service.util;

import org.secretflow.secretpad.common.errorcode.FeatureTableErrorCode;
import org.secretflow.secretpad.common.exception.SecretpadException;
import org.secretflow.secretpad.common.util.SpringContextUtil;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class HttpUtils {

    private static OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(false)
            .connectTimeout(100, TimeUnit.MILLISECONDS)
            .build();


    private HttpUtils() {
    }

    public static String get(String url) throws IOException {
        if (!isSecureUrl(url)) {
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_IP_FILTER, url);
        }
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    public static String post(String url, Map<String, String> formParams) throws IOException {
        if (!isSecureUrl(url)) {
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_IP_FILTER, url);
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            formBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBodyBuilder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }
    }

    public static boolean detection(String url) {
        if (!isSecureUrl(url)) {
            throw SecretpadException.of(FeatureTableErrorCode.FEATURE_TABLE_IP_FILTER, url);
        }
        Request request = new Request.Builder()
                .url(url)
                .head()
                .build();
        log.info("detection url {}", url);
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful() || response.code() != HttpStatus.INTERNAL_SERVER_ERROR.value();
        } catch (Exception e) {
            log.error("HttpUtils detection fail", e);
            return false;
        }
    }

    public static Boolean isSecureUrl(String urlString) {
        try {
            new URL(urlString);
        } catch (Exception e) {
            return false;
        }
        IpFilterUtil ipFilterUtil = SpringContextUtil.getBean(IpFilterUtil.class);
        return !ipFilterUtil.urlIsIpInRange(urlString);
    }
}
