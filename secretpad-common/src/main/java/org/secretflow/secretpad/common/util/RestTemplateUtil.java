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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * REST Util
 *
 * @author beiwei
 * @date 2023-11-17
 */
@Slf4j
@Service
public class RestTemplateUtil {
    private static RestTemplate REST_TEMPLATE;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        RestTemplateUtil.REST_TEMPLATE = restTemplate;
    }

    public static <T> T sendPostJson(String url, Object objReq, Map<String, String> headMap, Class<T> clazz) {
        ResponseEntity<String> stringResponseEntity = sendPostJson(url, objReq, headMap);
        return JsonUtils.toJavaObject(stringResponseEntity.getBody(), clazz);
    }

    public static ResponseEntity<String> sendPostJson(String url, Object objReq, Map<String, String> headMap) {
        log.info("RestTemplateUtils.sendPostJson request: url={}, reqObj={}, headMap={}.", url, JsonUtils.toJSONString(objReq),
                JsonUtils.toJSONString(headMap));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (null != headMap) {
            for (Map.Entry<String, String> entry : headMap.entrySet()) {
                httpHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtils.toJSONString(objReq), httpHeaders);
        ResponseEntity<String> tResponseEntity = REST_TEMPLATE.postForEntity(url, httpEntity, String.class);
        log.info("RestTemplateUtils.sendPostJson result: {}", tResponseEntity);
        return tResponseEntity;
    }


}
