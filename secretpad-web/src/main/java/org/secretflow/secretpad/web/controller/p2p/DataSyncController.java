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

package org.secretflow.secretpad.web.controller.p2p;

import org.secretflow.secretpad.common.dto.SecretPadResponse;
import org.secretflow.secretpad.common.dto.SyncDataDTO;
import org.secretflow.secretpad.common.util.JsonUtils;
import org.secretflow.secretpad.service.sync.p2p.DataSyncConsumerTemplate;

import com.fasterxml.jackson.databind.JavaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yutu
 * @date 2023/12/10
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1alpha1/data")
public class DataSyncController {

    private final DataSyncConsumerTemplate consumerTemplate;

    @PostMapping("sync")
    public SecretPadResponse<SyncDataDTO> sync(@RequestHeader("kuscia-origin-source") String nodeId, @RequestBody String p) throws ClassNotFoundException {
        SyncDataDTO syncDataDTO = JsonUtils.toJavaObject(p, SyncDataDTO.class);
        String id = syncDataDTO.getTableName();
        Class<?> cls = Class.forName(id);
        JavaType javaType = JsonUtils.makeJavaType(SyncDataDTO.class, cls);
        syncDataDTO = JsonUtils.toJavaObject(p, javaType);
        return SecretPadResponse.success(consumerTemplate.consumer(nodeId, syncDataDTO));
    }
}