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
package org.secretflow.secretpad.web.init;

import org.secretflow.secretpad.common.constant.SystemConstants;
import org.secretflow.secretpad.service.NodeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Initializer node data for p2p mode
 *
 * @author wb-698356
 * @date 2023/12/14
 */
@Profile(value = {SystemConstants.P2P})
@RequiredArgsConstructor
@Slf4j
@Service
public class P2pDataInit implements CommandLineRunner {

    private final NodeService nodeService;

    @Override
    public void run(String... args) throws Exception {
        try {
            nodeService.initialNode();
        } catch (Exception e) {
            log.error("initialize node failed, error = {}", e);
        }
    }
}
