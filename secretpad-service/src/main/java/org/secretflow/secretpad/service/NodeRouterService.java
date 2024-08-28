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

import org.secretflow.secretpad.service.model.common.SecretPadPageResponse;
import org.secretflow.secretpad.service.model.noderoute.CreateNodeRouterRequest;
import org.secretflow.secretpad.service.model.noderoute.NodeRouterVO;
import org.secretflow.secretpad.service.model.noderoute.PageNodeRouteRequest;
import org.secretflow.secretpad.service.model.noderoute.UpdateNodeRouterRequest;

import org.springframework.data.domain.Pageable;

/**
 * @author yutu
 * @date 2023/08/04
 */
public interface NodeRouterService {
    void createNodeRouter(CreateNodeRouterRequest request);

    SecretPadPageResponse<NodeRouterVO> queryPage(PageNodeRouteRequest request, Pageable pageable);

    void updateNodeRouter(UpdateNodeRouterRequest request);

    NodeRouterVO getNodeRouter(String nodeId);

    NodeRouterVO refreshRouter(String routerId);

    void deleteNodeRouter(String routerId);
}